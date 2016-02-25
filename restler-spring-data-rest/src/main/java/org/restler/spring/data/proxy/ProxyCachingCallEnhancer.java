package org.restler.spring.data.proxy;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.restler.client.Call;
import org.restler.client.CallEnhancer;
import org.restler.client.CallExecutor;
import org.restler.spring.data.Pair;

import java.util.Collection;

public class ProxyCachingCallEnhancer implements CallEnhancer {
    private Cache<Pair<Class<?>, Object>, Object> cache = CacheBuilder.newBuilder().
            maximumSize(1000).
            weakValues().
            build();

    @Override
    public Object apply(Call call, CallExecutor callExecutor) {

        Object object = callExecutor.execute(call);

        Object result = object;

        if(object instanceof Object[]) {
            Object[] array = (Object[])object;

            for(Object item : array) {
                if(item instanceof ResourceProxy) {
                    result = getResourceFromCache((ResourceProxy)item );
                }
            }
        } else if(object instanceof Collection) {
            Collection collection = (Collection)object;

            for(Object item : collection) {
                if(object instanceof ResourceProxy) {
                    result = getResourceFromCache((ResourceProxy)item );
                }
            }
        } if(object instanceof ResourceProxy) {
            result = getResourceFromCache((ResourceProxy)object );
        }

        return result;
    }

    private Object getResourceFromCache(ResourceProxy resource) {
        Class<?> clazz = resource.getObject().getClass();
        Object id = resource.getResourceId();
        Object value = cache.getIfPresent(new Pair<Class<?>, Object>(clazz, id));
        if (value != null) {
            return value;
        } else {
            cache.put(new Pair<Class<?>, Object>(clazz, id), resource);
            return resource;
        }
    }


}
