package org.restler.spring.data.calls;

import org.restler.client.CallExecutor;

/**
 * ChainCallEnhancer is used for catching {@link ChainCall} and correctly applying it.
 */
public class ChainCallEnhancer extends CustomCallEnhancer<ChainCall> {

    public ChainCallEnhancer() {
        super(ChainCall.class);
    }

    @Override
    protected Object enhance(ChainCall call, CallExecutor callExecutor) {
        // Recursion for processing included chain calls
        return call.execute(nestedCall -> apply(nestedCall, callExecutor));
    }

}
