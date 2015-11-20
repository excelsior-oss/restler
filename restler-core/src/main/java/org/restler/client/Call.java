package org.restler.client;

import java.lang.reflect.Type;

/**
 * Call it is common representation of call, that should be executed.
 * Usually plugins are provides {@code org.restler.client.MethodInvocationMapper} that transforms
 * java call representation into Restler call representation and
 * {@code CallExecutor} that able to actually execute the call created by method invocation mapper.
 */
public interface Call {

    /**
     * Should return the type of call execution.
     */
    Type getReturnType();

    /**
     * Should return new instance of call with changed return type.
     */
    Call withReturnType(Type type);

}
