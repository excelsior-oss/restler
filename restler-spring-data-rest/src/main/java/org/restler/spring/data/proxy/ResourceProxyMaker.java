package org.restler.spring.data.proxy;

import com.google.common.collect.ImmutableMultimap;
import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Enhancer;
import org.objenesis.ObjenesisStd;
import org.restler.RestlerConfig;
import org.restler.client.Call;
import org.restler.client.CallExecutor;
import org.restler.client.RestlerException;
import org.restler.http.HttpCall;
import org.restler.http.HttpMethod;
import org.restler.util.UriBuilder;
import org.springframework.beans.BeanUtils;

import javax.persistence.EmbeddedId;
import javax.persistence.Id;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

public class ResourceProxyMaker {
    private final ObjenesisStd objenesis = new ObjenesisStd();

    public Object make(Class<?> aClass, Object object, HashMap<String, String> hrefs) {
        class ProxyObjectData {
            public CallExecutor executor = null;
        }

        ProxyObjectData proxyObjectData = new ProxyObjectData();

        net.sf.cglib.proxy.InvocationHandler handler = (Object o, Method method, Object[] args)->{

            if(method.equals(ResourceProxy.class.getMethod("getResourceId"))) {
                String self = hrefs.get("self");
                return getWrappedId(object, self.substring(self.lastIndexOf("/")+1));
            }else if(method.equals(ResourceProxy.class.getMethod("getRepositoryUri"))) {
                String self = hrefs.get("self");
                return self.substring(0, self.lastIndexOf("/"));
            }else if(method.equals(ResourceProxy.class.getMethod("getSelfUri"))) {
                return hrefs.get("self");
            }else if(method.equals(ResourceProxy.class.getMethod("getObject"))) {
                return object;
            }else if(method.equals(ResourceProxy.class.getMethod("getHrefs"))) {
                return hrefs;
            }else if(method.equals(ResourceProxy.class.getMethod("setExecutor", CallExecutor.class))) {
                proxyObjectData.executor = (CallExecutor) args[0];
                return null;
            }

            if(proxyObjectData.executor != null) {
                String uri = getHrefByMethod(method, hrefs);

                if(uri != null) {
                    String fieldName = BeanUtils.findPropertyForMethod(method).getName();
                    Field field = object.getClass().getDeclaredField(fieldName);

                    field.setAccessible(true);


                    Call httpCall = new HttpCall(new UriBuilder(uri).build(), HttpMethod.GET, null, ImmutableMultimap.of(), method.getGenericReturnType());

                    Object newValue = proxyObjectData.executor.execute(httpCall);
                    field.set(object, newValue);
                    field.set(o, newValue);

                    return field.get(object);
                }
            }

            return method.invoke(object, args);
        };

        Enhancer enhancer = new Enhancer();
        enhancer.setUseCache(false);
        enhancer.setSuperclass(aClass);
        enhancer.setInterfaces(new Class<?>[]{ResourceProxy.class});
        enhancer.setCallbackType(handler.getClass());

        Class proxyClass = enhancer.createClass();
        Enhancer.registerCallbacks(proxyClass, new Callback[] { handler });

        Object proxy = objenesis.newInstance(proxyClass);

        try {
            for(Field objectField : aClass.getDeclaredFields()) {
                objectField.setAccessible(true);
                objectField.set(proxy, objectField.get(object));
                objectField.setAccessible(false);
            }
        } catch (IllegalAccessException e) {
            throw new RestlerException("Illegal access to field.", e);
        }

        return proxy;
    }

    private Object getWrappedId(Object object, String id) {
        Field[] fields = object.getClass().getDeclaredFields();
        Class fieldClass;

        for (Field field : fields) {
            if (field.getDeclaredAnnotation(Id.class) != null || field.getDeclaredAnnotation(EmbeddedId.class) != null) {
                fieldClass = field.getType();

                field.setAccessible(true);

                try {
                    return fieldClass.getConstructor(String.class).newInstance(id);
                } catch (IllegalAccessException e) {
                    throw new RestlerException("Access denied to change id", e);
                } catch (InvocationTargetException e) {
                    throw new RestlerException("Can't create id wrapper", e);
                } catch (NoSuchMethodException | InstantiationException e) {
                    throw new RestlerException("Could not instantiate id object", e);
                }
            }
        }

        return null;
    }

    private String getHrefByMethod(Method method, HashMap<String, String> hrefs) {
        String methodName = method.getName();

        if(methodName.startsWith("get")) {
            String hrefName = methodName.substring(3).toLowerCase();
            return hrefs.get(hrefName);
        }

        return null;
    }
}
