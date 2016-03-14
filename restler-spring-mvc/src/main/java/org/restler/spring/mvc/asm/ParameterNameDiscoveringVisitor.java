package org.restler.spring.mvc.asm;

import jdk.internal.org.objectweb.asm.ClassVisitor;
import jdk.internal.org.objectweb.asm.MethodVisitor;

import java.lang.reflect.Member;
import java.util.Map;

public class ParameterNameDiscoveringVisitor extends ClassVisitor {

    private final Class<?> clazz;
    private final Map<Member, String[]> map;

    private static final String STATIC_CLASS_INIT = "<clinit>";


    public ParameterNameDiscoveringVisitor(Class<?> clazz, Map<Member, String[]> map) {
        super(AsmConstants.ASM5);
        this.clazz = clazz;
        this.map = map;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        if(!(((access & AsmConstants.ACCESS_SYNTHETIC) | (access & AsmConstants.ACCESS_BRIDGE)) > 0) && !STATIC_CLASS_INIT.equals(name)) {
            return new LocalVariableTableVisitor(clazz, name, desc, map, (access & AsmConstants.ACCESS_STATIC) > 0);
        }

        return null;
    }
}
