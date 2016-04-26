package org.restler.spring.data.calls;

import org.restler.client.Call;
import org.restler.client.CallEnhancer;
import org.restler.client.CallExecutionChain;
import org.restler.client.CallExecutor;

import java.util.ArrayList;
import java.util.List;

/**
 * ChainCallEnhancer is used for catching {@link ChainCall} and correctly applying it.
 */
public class ChainCallEnhancer implements CallEnhancer {
    @Override
    public Object apply(Call call, CallExecutor callExecutor) {
        if(call instanceof ChainCall) {
            List<CallEnhancer> chainEnhancer = new ArrayList<>();
            chainEnhancer.add(this);
            //recursion for processing included chain calls
            CallExecutionChain chain = new CallExecutionChain(callExecutor, chainEnhancer);
            return ((ChainCall)call).apply(chain);
        } else {
            return callExecutor.execute(call);
        }
    }
}
