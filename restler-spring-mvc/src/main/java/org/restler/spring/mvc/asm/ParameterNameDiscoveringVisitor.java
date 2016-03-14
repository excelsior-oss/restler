package org.restler.spring.mvc.asm;


import org.objectweb.asm.*;

import java.lang.reflect.Member;
import java.util.Map;

public class ParameterNameDiscoveringVisitor implements ClassVisitor {

    private final Class<?> clazz;
    private final Map<Member, String[]> map;

    private static final String STATIC_CLASS_INIT = "<clinit>";


    public ParameterNameDiscoveringVisitor(Class<?> clazz, Map<Member, String[]> map) {
        this.clazz = clazz;
        this.map = map;
    }

    @Override
    public void visit(int i, int i1, String s, String s1, String s2, String[] strings) {

    }

    @Override
    public void visitSource(String s, String s1) {

    }

    @Override
    public void visitOuterClass(String s, String s1, String s2) {

    }

    @Override
    public AnnotationVisitor visitAnnotation(String s, boolean b) {
        return null;
    }

    @Override
    public void visitAttribute(Attribute attribute) {

    }

    @Override
    public void visitInnerClass(String s, String s1, String s2, int i) {

    }

    @Override
    public FieldVisitor visitField(int i, String s, String s1, String s2, Object o) {
        return null;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        if(!(((access & AsmConstants.ACCESS_SYNTHETIC) | (access & AsmConstants.ACCESS_BRIDGE)) > 0) && !STATIC_CLASS_INIT.equals(name)) {
            return new LocalVariableTableVisitor(clazz, name, desc, map, (access & AsmConstants.ACCESS_STATIC) > 0);
        }

        return null;
    }

    @Override
    public void visitEnd() {

    }
}
