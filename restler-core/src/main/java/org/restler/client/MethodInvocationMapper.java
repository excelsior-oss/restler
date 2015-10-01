package org.restler.client;

import java.lang.reflect.Method;

public interface MethodInvocationMapper {

    Call map(Object receiver, Method method, Object[] args);

}
