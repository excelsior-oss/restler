package org.restler.spring.data.proxy;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.restler.client.*;
import org.restler.util.Pair;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ProxyCallEnhancer implements CallEnhancer {
    private final Cache<Pair<Class<?>, Object>, Object> cache;

    public ProxyCallEnhancer(long maxCacheSize) {
        cache = CacheBuilder.newBuilder().
                maximumSize(maxCacheSize).
                weakValues().
                build();
    }

    @Override
    public Object apply(Call call, CallExecutor callExecutor) {
        Object object = callExecutor.execute(call);
        Object result = object;

        if(object instanceof Object[]) {
            Object[] array = (Object[])object;

            for(int i = 0; i < array.length; ++i) {
                initProxyObject(array[i], callExecutor);
                array[i] = getResourceFromCache(array[i]);
            }
        } else if(object instanceof Collection) {
            Collection collection = (Collection) object;

            Collection resultCollection = null;
            try {
                resultCollection = collection.getClass().getConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                new RestlerException("Can't get default constructor for collection. " + collection, e);
            }

            for(Object item : collection) {
                initProxyObject(item, callExecutor);
                resultCollection.add(getResourceFromCache(item));
            }

            result = resultCollection;
        } else {
            initProxyObject(object, callExecutor);
            result = getResourceFromCache(object);
        }

        return result;
    }

    private void initProxyObject(Object object, CallExecutor callExecutor) {
        if(object instanceof ResourceProxy) {
            List<CallEnhancer> proxyEnhancer = new ArrayList<>();
            proxyEnhancer.add(this);
            CallExecutionChain chain = new CallExecutionChain(callExecutor, proxyEnhancer);
            ResourceProxy resourceProxy = (ResourceProxy)object;
            resourceProxy.setExecutor(chain);
        }
    }

    private Object getResourceFromCache(Object object) {

        if(object instanceof ResourceProxy) {
            ResourceProxy resource = (ResourceProxy)object;

            Class<?> clazz = resource.getObject().getClass();
            Object id = resource.getResourceId();
            Object value = cache.getIfPresent(new Pair<Class<?>, Object>(clazz, id));
            if (value != null) {
                return value;
            } else {
                cache.put(new Pair<>(clazz, id), resource);
                return resource;
            }
        }

        return object;
    }

}
