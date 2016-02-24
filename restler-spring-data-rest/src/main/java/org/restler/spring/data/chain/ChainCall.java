package org.restler.spring.data.chain;

import org.restler.client.Call;

import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

public class ChainCall implements Call, Iterable<Call> {
    private final List<Call> calls;
    private final Type returnType;

    public ChainCall(List<Call> calls, Type returnType) {
        this.calls = calls;
        this.returnType = returnType;
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
    public Iterator iterator() {
        return calls.iterator();
    }
}
