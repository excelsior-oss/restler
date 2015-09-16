package org.restler.client;

import java.util.Arrays;
import java.util.List;

public class CallExecutionChain implements CallExecutor {

    private final CallExecutor callExecutor;
    private final CallExecutionAdvice<?>[] advices;
    private final int index;

    public CallExecutionChain(CallExecutor callExecutor, List<CallExecutionAdvice<?>> advices) {
        this(callExecutor, advices.toArray(new CallExecutionAdvice[advices.size()]), 0);
    }

    private CallExecutionChain(CallExecutor callExecutor, CallExecutionAdvice<?>[] advices, int index) {

        if (Arrays.asList(advices).contains(null))
            throw new NullPointerException("Null advice is not allowed in the chain.");

        this.callExecutor = callExecutor;
        this.advices = advices;
        this.index = index;
    }

    @Override
    public Object execute(Call call) {

        if (index >= advices.length) {
            return callExecutor.execute(call);
        } else {
            CallExecutionAdvice<?> advice = advices[index];
            return advice.advice(call, tail());
        }
    }

    private CallExecutionChain tail() {
        return new CallExecutionChain(callExecutor, advices, index + 1);
    }
}
