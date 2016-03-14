package org.restler.spring.mvc.asm;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.restler.client.RestlerException;

import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LocalVariableTableVisitor implements MethodVisitor {

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
        this.clazz = clazz;
        this.name = name;
        this.arguments = getArgumentsTypeNames(desc);
        this.map = map;
        this.isStatic = isStatic;
        lvtSlotIndex = computeLvtSlotIndices(isStatic, arguments);

        this.parameterNames = new String[arguments.size()];
    }

    @Override
    public AnnotationVisitor visitAnnotationDefault() {
        return null;
    }

    @Override
    public AnnotationVisitor visitAnnotation(String s, boolean b) {
        return null;
    }

    @Override
    public AnnotationVisitor visitParameterAnnotation(int i, String s, boolean b) {
        return null;
    }

    @Override
    public void visitAttribute(Attribute attribute) {

    }

    @Override
    public void visitCode() {

    }

    @Override
    public void visitFrame(int i, int i1, Object[] objects, int i2, Object[] objects1) {

    }

    @Override
    public void visitInsn(int i) {

    }

    @Override
    public void visitIntInsn(int i, int i1) {

    }

    @Override
    public void visitVarInsn(int i, int i1) {

    }

    @Override
    public void visitTypeInsn(int i, String s) {

    }

    @Override
    public void visitFieldInsn(int i, String s, String s1, String s2) {

    }

    @Override
    public void visitMethodInsn(int i, String s, String s1, String s2) {

    }

    @Override
    public void visitJumpInsn(int i, Label label) {

    }

    @Override
    public void visitLabel(Label label) {

    }

    @Override
    public void visitLdcInsn(Object o) {

    }

    @Override
    public void visitIincInsn(int i, int i1) {

    }

    @Override
    public void visitTableSwitchInsn(int i, int i1, Label label, Label[] labels) {

    }

    @Override
    public void visitLookupSwitchInsn(Label label, int[] ints, Label[] labels) {

    }

    @Override
    public void visitMultiANewArrayInsn(String s, int i) {

    }

    @Override
    public void visitTryCatchBlock(Label label, Label label1, Label label2, String s) {

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
    public void visitLineNumber(int i, Label label) {

    }

    @Override
    public void visitMaxs(int i, int i1) {

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
            argTypes[i] = ClassUtils.resolveClassName(arguments.get(i), loader);
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

    private List<String> getArgumentsTypeNames(String desc) {
        List<String> result = new ArrayList<>();

        for(int i = 0; i < desc.length(); ) {
            if(desc.charAt(i) == '(' || desc.charAt(i) == ';') {
                i++;
                continue;
            } else if(desc.charAt(i) == ')') {
                break;
            } else if(desc.charAt(i) == '[') {
                String typeName = getArrayClassName(desc, i);
                result.add(typeName);
                i += typeName.length();
            } else if(desc.charAt(i) == 'L') {
                String typeName = getObjectClassName(desc, i);
                result.add(typeName);
                i += typeName.length();
            } else if(ClassUtils.isPrimitiveType(""+desc.charAt(i))) {
                result.add(""+desc.charAt(i));
                i++;
            }
        }

        return result;
    }

    private String getArrayClassName(String desc, int i) {
        i += 1;

        if(i < desc.length() && ClassUtils.isPrimitiveType("" + desc.charAt(i))) {
            return "[" + desc.charAt(i);
        }

        return "["+getObjectClassName(desc, i);
    }

    private String getObjectClassName(String desc, int i) {
        String typeName = "";

        for(; i < desc.length(); ++i) {
            if(desc.charAt(i) == ')' || desc.charAt(i) == ';') {
                break;
            }

            typeName += desc.charAt(i);
        }

        return typeName;
    }
}