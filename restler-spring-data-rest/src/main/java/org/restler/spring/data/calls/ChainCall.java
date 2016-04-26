package org.restler.spring.data.calls;

import org.restler.client.Call;
import org.restler.client.CallExecutor;

import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;

/**
 * Call is used for executing chain of calls.
 */
public class ChainCall implements Call, Iterable<Call> {
    private final BiFunction<Object, Object, Object> modifierFunction;
    private final List<Call> calls;
    private final Type returnType;

    /**
     * @param modifierFunction provides control results that returns from call execution. It receive previous result and execution call result
     * and returns modified result.
     */
    public ChainCall(BiFunction<Object, Object, Object> modifierFunction, List<Call> calls, Type returnType) {
        this.modifierFunction = modifierFunction;
        this.calls = calls;
        this.returnType = returnType;
    }

    public ChainCall(List<Call> calls, Type returnType) {
        this(null, calls, returnType);
    }

    @Override
    public Type getReturnType() {
        return returnType;
    }

    @Override
    public Call withReturnType(Type type) {
        return new ChainCall(calls, type);
    }

    @Override
    public Iterator<Call> iterator() {
        return calls.iterator();
    }

    /**
     * Executes chain of calls using {@link CallExecutor}.
     */
    public Object apply(CallExecutor executor) {
        Object result = null;

        for(Call callFromChain : calls) {
            Object object = null;

            if(callFromChain != null) {
                object = executor.execute(callFromChain);
            }

            if(modifierFunction != null) {
                result = modifierFunction.apply(result, object);
            }
        }

        return result;
    }
}
