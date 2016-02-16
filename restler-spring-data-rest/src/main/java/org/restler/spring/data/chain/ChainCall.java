package org.restler.spring.data.chain;

import org.restler.client.Call;

import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.List;
import java.util.function.Function;

public class ChainCall implements Call {


    private int index = 0;

    private List<Call> calls;

    public ChainCall(List<Call> calls) {
        this.calls = calls;
    }

    public Call getCall() {
        if(index < calls.size()) {
            return calls.get(index++);
        }
        return null;
    }

    @Override
    public Type getReturnType() {
        if(index >= calls.size()) {
            return null;
        }
        return calls.get(index).getReturnType();
    }

    @Override
    public Call withReturnType(Type type) {
        if(index >= calls.size()) {
            return null;
        }
        return calls.get(index).withReturnType(type);
    }
}
