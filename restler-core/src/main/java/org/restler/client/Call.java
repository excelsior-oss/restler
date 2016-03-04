package org.restler.client;

import java.lang.reflect.Type;

/**
 * Common representation of a call that should be executed.
 * Usually plugins provide a {@code org.restler.client.MethodInvocationMapper} that transforms
 * Java call representation into Restler call representation and
 * a {@code CallExecutor} that is able to actually execute the call created by the method invocation mapper.
 */
public interface Call {

    /**
     * Should return the type of call execution.
     */
    Type getReturnType();

    /**
     * Should return a new instance of call with changed return type.
     */
    Call withReturnType(Type type);

}
