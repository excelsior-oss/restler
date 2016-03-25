package org.restler.spring.data.chain;

import org.restler.client.Call;
import org.restler.client.CallEnhancer;
import org.restler.client.CallExecutionChain;
import org.restler.client.CallExecutor;

import java.util.ArrayList;
import java.util.List;

public class ChainCallEnhancer implements CallEnhancer {
    @Override
    public Object apply(Call call, CallExecutor callExecutor) {

        List<CallEnhancer> chainEnhancer = new ArrayList<>();
        chainEnhancer.add(this);
        CallExecutionChain chain = new CallExecutionChain(callExecutor, chainEnhancer);

        if(call instanceof ChainCall) {
            ChainCall chainCall = (ChainCall)call;

            Object result = null;

            for(Call callFromChain : chainCall) {
                result = chainCall.apply(chain.execute(callFromChain));
            }

            return result;
        } else {
            return callExecutor.execute(call);
        }
    }
}
