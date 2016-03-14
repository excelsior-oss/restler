package org.restler.spring.mvc;

import jdk.internal.org.objectweb.asm.ClassReader;
import org.restler.spring.mvc.asm.ParameterNameDiscoveringVisitor;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
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
}
