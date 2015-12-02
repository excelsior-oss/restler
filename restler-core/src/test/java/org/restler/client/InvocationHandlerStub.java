package org.restler.client;

import net.sf.cglib.proxy.InvocationHandler;

import java.lang.reflect.Method;

public class InvocationHandlerStub implements InvocationHandler {

    int callsCount;

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        callsCount++;
        return null;
    }

}
