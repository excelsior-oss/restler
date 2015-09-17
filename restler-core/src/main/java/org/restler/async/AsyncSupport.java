package org.restler.async;

import org.restler.client.CallEnhancer;
import org.restler.client.RestlerConfig;

import java.util.List;
import java.util.function.Function;

import static java.util.Arrays.asList;

public class AsyncSupport implements Function<RestlerConfig, List<CallEnhancer>> {

    @Override
    public List<CallEnhancer> apply(RestlerConfig restlerConfig) {
        return asList(new CallableSupport(),
                new FutureSupport(restlerConfig.getRestlerThreadPool()));
    }

}
