package org.restler.spring.data.calls;

import org.restler.client.Call;
import org.restler.client.CallExecutor;

import java.lang.reflect.Type;
import java.util.List;
import java.util.function.BinaryOperator;

/**
 * Call is used for executing chain of calls.
 */
public class ChainCall implements Call {

    /**
     * {@code} accumulator is used to combine multiple results of nested calls into single result of chain call.
     * Probably it's should be replaced by full-blown reduce framework (with identity, accumulator and combiner)
     */
    private final BinaryOperator<Object> accumulator;
    private final List<Call> calls;
    private final Type returnType;

    public ChainCall(BinaryOperator<Object> modifierFunction, List<Call> calls, Type returnType) {
        this.accumulator = modifierFunction;
        this.calls = calls;
        this.returnType = returnType;
    }

    public ChainCall(List<Call> calls, Type returnType) {
        this(null, calls, returnType);
    }

    /**
     * Executes chain of calls using {@link CallExecutor}.
     */
    Object execute(CallExecutor executor) {
        Object result = null;

        if (accumulator != null) {
            result = calls.stream().
                    map(executor::execute).
                    reduce(null, accumulator);
        } else {
            calls.forEach(executor::execute);
        }

        return result;
    }

    @Override
    public Type getReturnType() {
        return returnType;
    }

    @Override
    public Call withReturnType(Type type) {
        return new ChainCall(calls, type);
    }
}
