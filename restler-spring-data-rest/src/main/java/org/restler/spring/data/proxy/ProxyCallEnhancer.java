package org.restler.spring.data.proxy;

import org.restler.RestlerConfig;
import org.restler.client.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ProxyCallEnhancer implements CallEnhancer {
    private RestlerConfig config;

    public ProxyCallEnhancer(RestlerConfig config) {
        this.config = config;
    }


    @Override
    public Object apply(Call call, CallExecutor callExecutor) {
        Object object = callExecutor.execute(call);

        if(object instanceof Collection) {
            Collection<Object> collection = (Collection<Object>)object;

            for(Object item : collection) {
                initProxyObject(item, callExecutor);
            }

        } else {
            initProxyObject(object, callExecutor);
        }


        return object;
    }

    private void initProxyObject(Object object, CallExecutor callExecutor) {
        if(object instanceof ResourceProxy) {
            List<CallEnhancer> proxyEnhancer = new ArrayList<>();
            proxyEnhancer.add(this);
            CallExecutionChain chain = new CallExecutionChain(callExecutor, proxyEnhancer);
            ResourceProxy resourceProxy = (ResourceProxy)object;
            resourceProxy.setExecutor(chain);
            resourceProxy.setRestlerConfig(config);
        }
    }


}
