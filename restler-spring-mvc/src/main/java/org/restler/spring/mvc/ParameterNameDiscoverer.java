package org.restler.spring.mvc;

import org.objectweb.asm.ClassReader;
import org.restler.client.RestlerException;
import org.restler.spring.mvc.asm.ClassUtils;
import org.restler.spring.mvc.asm.ParameterNameDiscoveringVisitor;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

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
        for (int i = 0; i < parameters.length; i++) {
            Parameter param = parameters[i];
            if (!param.isNamePresent()) {
                return null;
            }
            parameterNames[i] = param.getName();
        }
        return parameterNames;
    }

    private String[] getParameterNamesFromLocalVariableTable(Method method) {
        Class<?> declaringClass = method.getDeclaringClass();

        Map<Member, String[]> map = inspectClass(declaringClass);

        if (map != null) {
            return map.get(method);
        }
        return null;
    }

    private Map<Member, String[]> inspectClass(Class<?> clazz) {
        try(InputStream inputStream = clazz.getResourceAsStream(ClassUtils.getClassFileName(clazz))) {
            if (inputStream == null) {
                return null;
            }

            ClassReader classReader = new ClassReader(inputStream);
            Map<Member, String[]> map = new HashMap<>(32);
            classReader.accept(new ParameterNameDiscoveringVisitor(clazz, map), 0);
            return map;
        }
        catch (IOException e) {
            throw new RestlerException("Can't read class from stream.", e);
        }
        catch(IllegalArgumentException e) {
            throw new RestlerException("Illegal argument.", e);
        }
    }
}
