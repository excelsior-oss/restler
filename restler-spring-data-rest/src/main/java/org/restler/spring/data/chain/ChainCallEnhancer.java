package org.restler.spring.data.chain;

import org.restler.client.Call;
import org.restler.client.CallEnhancer;
import org.restler.client.CallExecutionChain;
import org.restler.client.CallExecutor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class ChainCallEnhancer implements CallEnhancer {
    @Override
    public Object apply(Call call, CallExecutor callExecutor) {

        List<CallEnhancer> chainEnhancer = new ArrayList<>();
        chainEnhancer.add(this);
        CallExecutionChain chain = new CallExecutionChain(callExecutor, chainEnhancer);

        if(call instanceof ChainCall) {
            ChainCall chainCall = (ChainCall)call;

            Call callFromChain;

            Object result = null;
            while((callFromChain = chainCall.getCall()) != null) {
                result = chain.execute(callFromChain);
            }

            return result;
        } else {
            return callExecutor.execute(call);
        }
    }
}
