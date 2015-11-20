package org.restler.client;

import java.lang.reflect.Method;

/**
 * MethodInvocationMapper it is a function, that able to transform representation of java call (receiver, method and arguments)
 * into Restler's {@code Call}
 */
public interface MethodInvocationMapper {

    Call map(Object receiver, Method method, Object[] args);

}
