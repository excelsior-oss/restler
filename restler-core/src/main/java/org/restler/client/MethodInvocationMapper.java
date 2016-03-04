package org.restler.client;

import java.lang.reflect.Method;

/**
 * MethodInvocationMapper is a function able to transform a representation of a Java call (receiver, method and arguments)
 * into a Restler {@code Call}.
 */
public interface MethodInvocationMapper {

    Call map(Object receiver, Method method, Object[] args);

}
