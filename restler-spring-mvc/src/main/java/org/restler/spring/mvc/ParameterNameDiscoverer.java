package org.restler.spring.mvc;

import jdk.internal.org.objectweb.asm.ClassReader;
import jdk.internal.org.objectweb.asm.ClassVisitor;
import jdk.internal.org.objectweb.asm.Label;
import jdk.internal.org.objectweb.asm.MethodVisitor;
import org.restler.client.RestlerException;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ParameterNameDiscoverer {

    public String[] getParameterNames(Method method) {
        String[] result = getParameterNamesDefault(method);

        if(result == null) {
            result = getParameterNamesFromLocalVariableTable(method);
        }

        return result;
    }

    private String[] getParameterNamesDefault(Method method) {
        Parameter[] parameters = method.getParameters();
        String[] parameterNames = new String[parameters.length];

        for(int i = 0; i < parameters.length; ++i) {
            Parameter parameter = parameters[i];
            if(!parameter.isNamePresent()) {
                return null;
            }

            parameterNames[i] = parameter.getName();
        }

        if(parameterNames.length == 0) {
            return getParameterNamesFromLocalVariableTable(method);
        }

        return parameterNames;
    }

    private String[] getParameterNamesFromLocalVariableTable(Method method) {
//        Method originalMethod = findBridgedMethod(method);
        Method originalMethod = method;
        Class<?> declaringClass = originalMethod.getDeclaringClass();

        Map<Member, String[]> map = inspectClass(declaringClass);

        if (map != null) {
            return map.get(originalMethod);
        }
        return null;
    }

//    private Method findBridgedMethod(Method bridgeMethod) {
//        if(bridgeMethod == null || !bridgeMethod.isBridge()) {
//            return bridgeMethod;
//        }
//
//        List<Method> methods = new ArrayList<>();
//
//        getAllMethods(bridgeMethod.getDeclaringClass(), methods);
//
//        List<Method> candidates = methods.stream().filter(method -> isBridgedCandidate(method, bridgeMethod)).collect(Collectors.toList());
//
//
//
//
//        return candidates.get(0);
//    }
//
//    private void getAllMethods(Class<?> clazz, List<Method> result) {
//        for(Method method : clazz.getDeclaredMethods()) {
//            result.add(method);
//        }
//
//        if(clazz.getSuperclass() != null) {
//            getAllMethods(clazz.getSuperclass(), result);
//        } else if(clazz.isInterface()) {
//            for(Class<?> interf : clazz.getInterfaces()) {
//                getAllMethods(interf, result);
//            }
//        }
//    }
//
//    private boolean isBridgedCandidate(Method candidateMethod, Method bridgeMethod) {
//        return (!candidateMethod.isBridge() && !candidateMethod.equals(bridgeMethod) &&
//                candidateMethod.getName().equals(bridgeMethod.getName()) &&
//                candidateMethod.getParameterTypes().length == bridgeMethod.getParameterTypes().length);
//    }

    private Map<Member, String[]> inspectClass(Class<?> clazz) {
        InputStream inputStream = clazz.getResourceAsStream(getClassFileName(clazz));
        if (inputStream == null) {
            return null;
        }
        try {
            ClassReader classReader = new ClassReader(inputStream);
            Map<Member, String[]> map = new ConcurrentHashMap<>(32);
            classReader.accept(new ParameterNameDiscoveringVisitor(clazz, map), 0);
            return map;
        }
        catch (IOException | IllegalArgumentException ex) {
            // ignore
        } finally {
            try {
                inputStream.close();
            }
            catch (IOException ex) {
                // ignore
            }
        }
        return null;
    }

    private String getClassFileName(Class<?> clazz) {
        String name = clazz.getName();
        return name.substring(name.lastIndexOf('.')+1) + ".class";
    }


    private static final int ASM5 = 5 << 16;

    private class ParameterNameDiscoveringVisitor extends ClassVisitor {

        private final Class<?> clazz;
        private final Map<Member, String[]> map;


        int ACC_SYNTHETIC = 0x1000;
        int ACC_BRIDGE = 0x0040;
        int ACC_STATIC = 0x0008;
        private static final String STATIC_CLASS_INIT = "<clinit>";


        public ParameterNameDiscoveringVisitor(Class<?> clazz, Map<Member, String[]> map) {
            super(ASM5);
            this.clazz = clazz;
            this.map = map;
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            if(!(((access & ACC_SYNTHETIC) | (access & ACC_BRIDGE)) > 0) && !STATIC_CLASS_INIT.equals(name)) {
                return new LocalVariableTableVisitor(clazz, name, desc, map, (access & ACC_STATIC) > 0);
            }

            return null;
        }
    }

    private class LocalVariableTableVisitor extends MethodVisitor {

        private static final String CONSTRUCTOR = "<init>";

        private final Class<?> clazz;
        private final String name;
        private final List<String> arguments;
        private Map<Member, String[]> map;
        private boolean isStatic;
        private final int[] lvtSlotIndex;

        private boolean hasLocalVariableTableInfo;
        private final String[] parameterNames;

        public LocalVariableTableVisitor(Class<?> clazz, String name, String desc, Map<Member, String[]> map, boolean isStatic) {
            super(ASM5);

            this.clazz = clazz;
            this.name = name;
            this.arguments = getArgumentsTypeNames(desc);
            this.map = map;
            this.isStatic = isStatic;
            lvtSlotIndex = computeLvtSlotIndices(isStatic, arguments);

            this.parameterNames = new String[arguments.size()];
        }

        @Override
        public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
            hasLocalVariableTableInfo = true;
            for (int i = 0; i < lvtSlotIndex.length; i++) {
                if (lvtSlotIndex[i] == index) {
                    parameterNames[i] = name;
                }
            }
        }

        @Override
        public void visitEnd() {
            if (hasLocalVariableTableInfo || (isStatic && parameterNames.length == 0)) {
                map.put(resolveMember(), parameterNames);
            }
        }

        private Member resolveMember() {
            ClassLoader loader = clazz.getClassLoader();
            Class<?>[] argTypes = new Class<?>[arguments.size()];
            for (int i = 0; i < arguments.size(); i++) {
                argTypes[i] = resolveClassName(arguments.get(i), loader);
            }
            try {
                if (CONSTRUCTOR.equals(name)) {
                    return clazz.getDeclaredConstructor(argTypes);
                }
                return clazz.getDeclaredMethod(name, argTypes);
            }
            catch (NoSuchMethodException e) {
                throw new RestlerException("Can't resolve method in the class object.", e);
            }
        }

        private int[] computeLvtSlotIndices(boolean isStatic, List<String> paramTypes) {
            int[] lvtIndex = new int[paramTypes.size()];
            int nextIndex = (isStatic ? 0 : 1);
            for (int i = 0; i < paramTypes.size(); i++) {
                lvtIndex[i] = nextIndex;
                if (isWideType(paramTypes.get(i))) {
                    nextIndex += 2;
                }
                else {
                    nextIndex++;
                }
            }
            return lvtIndex;
        }

        private boolean isWideType(String typeName) {
            // long or double type
            return (typeName.equals("J") || typeName.equals("D"));
        }

        private Class<?> resolveClassName(String className, ClassLoader classLoader) {
            try {
                return Class.forName(className, true, classLoader);
            } catch (ClassNotFoundException e) {
                throw new RestlerException("Can't found class " + className, e);
            }
        }

        private List<String> getArgumentsTypeNames(String desc) {
            List<String> result = new ArrayList<>();

            for(int i = 0; i < desc.length(); ) {
                if(desc.charAt(i) == '(' || desc.charAt(i) == ';') {
                    i++;
                    continue;
                } else if(desc.charAt(i) == ')') {
                    break;
                } else if(desc.charAt(i) == 'L') {
                    String typeName = "";

                    for(++i; i < desc.length(); ++i) {
                        if(desc.charAt(i) == ')' || desc.charAt(i) == ';') {
                            break;
                        }

                        typeName += desc.charAt(i);
                    }

                    result.add(typeName.replace('/', '.'));
                }
            }

            return result;
        }
    }


}
