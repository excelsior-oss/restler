package org.restler.spring.data.chain;

import org.restler.client.Call;

import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

public class ChainCall implements Call, Iterable<Call> {
    private final Function<Object, Object> modifierFunction;
    private final List<Call> calls;
    private final Type returnType;

    public ChainCall(Function<Object, Object> modifierFunction, List<Call> calls, Type returnType) {
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

    public Object apply(Object object) {
        if(modifierFunction != null) {
            return modifierFunction.apply(object);
        }

        return object;
    }
}
