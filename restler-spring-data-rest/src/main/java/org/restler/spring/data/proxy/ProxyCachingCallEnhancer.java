package org.restler.spring.data.proxy;

import org.restler.client.Call;
import org.restler.client.CallEnhancer;
import org.restler.client.CallExecutor;
import org.restler.spring.data.Pair;

import java.util.HashMap;
import java.util.LinkedHashMap;

public class ProxyCachingCallEnhancer implements CallEnhancer {
    private HashMap<Pair<Class<?>, Object>, Object> cache = new LinkedHashMap<>();

    @Override
    public Object apply(Call call, CallExecutor callExecutor) {

        Object object = callExecutor.execute(call);

        if(object instanceof ResourceProxy) {

            Class<?> clazz = ((ResourceProxy) object).getObject().getClass();
            Object id = ((ResourceProxy) object).getResourceId();

            Object value = cache.get(new Pair<Class<?>, Object>(clazz, id));
            if (value != null) {
                return value;
            } else {
                cache.put(new Pair<Class<?>, Object>(clazz, id), object);
                return object;
            }
        }

        return object;
    }
}
