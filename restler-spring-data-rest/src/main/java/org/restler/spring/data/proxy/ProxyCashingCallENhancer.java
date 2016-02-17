package org.restler.spring.data.proxy;

import org.restler.client.Call;
import org.restler.client.CallEnhancer;
import org.restler.client.CallExecutor;

import java.util.HashMap;
import java.util.LinkedHashMap;

public class ProxyCashingCallEnhancer implements CallEnhancer {
    private HashMap<Object, Object> hash = new LinkedHashMap<>();

    @Override
    public Object apply(Call call, CallExecutor callExecutor) {
        Object object = callExecutor.execute(call);

        if(object instanceof ResourceProxy) {
            ResourceProxy resourceProxy = (ResourceProxy) object;
            Object id = resourceProxy.getResourceId();

            Object value;
            if((value = hash.get(id)) != null) {
                return value;
            } else {
                hash.put(id, object);
                return object;
            }
        }

        return object;
    }
}
