package org.restler.client;

import java.lang.reflect.Type;

public interface Call {

    Type getReturnType();

    Call withReturnType(Type type);

}
