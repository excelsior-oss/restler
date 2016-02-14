package org.restler.spring.data.proxy;

import com.google.common.collect.ImmutableMultimap;
import org.restler.RestlerConfig;
import org.restler.client.*;
import org.restler.http.HttpCall;
import org.restler.http.HttpMethod;
import org.restler.util.UriBuilder;
import org.springframework.beans.BeanUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * Created by rudenko on 08.02.2016.
 */
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
        if(object instanceof Resource) {
            List<CallEnhancer> proxyEnhancer = new ArrayList<>();
            proxyEnhancer.add(this);
            CallExecutionChain chain = new CallExecutionChain(callExecutor, proxyEnhancer);
            Resource resource = (Resource)object;
            resource.setExecutor(chain);
            resource.setRestlerConfig(config);

            Object realObject = resource.getObject();
            Class<?> aClass = realObject.getClass();

            try {
                for(Field objectField : aClass.getDeclaredFields()) {
                    objectField.setAccessible(true);
                    Object fieldValue = objectField.get(realObject);

                    if(fieldValue == null) {
                        String fieldName = objectField.getName();
                        PropertyDescriptor property = BeanUtils.getPropertyDescriptor(aClass, fieldName);

                        Method getMethod = property.getReadMethod();


                        String uri = getHrefByMethod(getMethod, resource.getHrefs());

                        if (uri != null) {
                            Call httpCall = new HttpCall(new UriBuilder(uri).build(), HttpMethod.GET, null, ImmutableMultimap.of(), getMethod.getGenericReturnType());
                            objectField.set(realObject, callExecutor.execute(httpCall));
                        }

                    }

                    objectField.setAccessible(false);
                }
            } catch (IllegalAccessException e) {
                throw new RestlerException("Illegal access to field.", e);
            }
        }
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
