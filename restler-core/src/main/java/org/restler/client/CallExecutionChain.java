package org.restler.client;

import java.util.ArrayList;
import java.util.List;

public class CallExecutionChain implements CallExecutor {

    private final CallExecutor callExecutor;
    private final List<CallEnhancer> enhancers;
    private final int index;

    public CallExecutionChain(CallExecutor callExecutor, List<CallEnhancer> enhancers) {
        this(callExecutor, new ArrayList<>(enhancers), 0);
    }

    private CallExecutionChain(CallExecutor callExecutor, List<CallEnhancer> enhancers, int index) {

        if (enhancers.contains(null))
            throw new NullPointerException("Null advice is not allowed in the chain.");

        this.callExecutor = callExecutor;
        this.enhancers = enhancers;
        this.index = index;
    }

    @Override
    public Object execute(Call call) {

        if (index >= enhancers.size()) {
            return callExecutor.execute(call);
        } else {
            CallEnhancer enhance = enhancers.get(index);
            return enhance.apply(call, tail());
        }
    }

    private CallExecutionChain tail() {
        return new CallExecutionChain(callExecutor, enhancers, index + 1);
    }
}
