package org.restler.spring.data.proxy;

import org.restler.client.Call;
import org.restler.client.CallEnhancer;
import org.restler.client.CallExecutor;

import java.util.HashMap;
import java.util.LinkedHashMap;

public class ProxyCachingCallEnhancer implements CallEnhancer {
    private HashMap<Object, Object> cache = new LinkedHashMap<>();

    @Override
    public Object apply(Call call, CallExecutor callExecutor) {

        Object object = callExecutor.execute(call);

        if(object instanceof ResourceProxy) {
            Object value = cache.get(((ResourceProxy) object).getResourceId());
            if (value != null) {
                return value;
            } else {
                cache.put(((ResourceProxy) object).getResourceId(), object);
                return object;
            }
        }

        return object;
    }
}
