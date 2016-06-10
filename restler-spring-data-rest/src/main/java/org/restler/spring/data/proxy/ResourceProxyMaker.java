package org.restler.spring.data.proxy;

import com.google.common.collect.ImmutableMultimap;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.InvocationHandler;
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
    public Object make(Class<?> aClass, Object object, HashMap<String, String> hrefs) {
        net.sf.cglib.proxy.InvocationHandler handler = new ResourceInvocationHandler(object, hrefs);

        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(aClass);
        enhancer.setInterfaces(new Class<?>[]{ResourceProxy.class});
        enhancer.setCallback(handler);

        Object proxy = enhancer.create();

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
            String hrefName = "";
            if(methodName.length() > 3) {
                hrefName = methodName.substring(3, 4).toLowerCase() + methodName.substring(4);
            }
            return hrefs.get(hrefName);
        }

        return null;
    }

    private class ResourceInvocationHandler implements InvocationHandler {
        private Object object;
        private HashMap<String, String> hrefs;
        private CallExecutor executor;

        public ResourceInvocationHandler(Object object, HashMap<String, String> hrefs) {
            this.object = object;
            this.hrefs = hrefs;
            this.executor = null;
        }

        @Override
        public Object invoke(Object o, Method method, Object[] args) throws Throwable {
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
                executor = (CallExecutor) args[0];
                return null;
            }

            if(executor == null) {
                throw new IllegalStateException("Executor must be initialized. For initialize executor use ProxyCallEnhancer.");
            }

            String uri = getHrefByMethod(method, hrefs);

            if(uri != null) {
                String fieldName = BeanUtils.findPropertyForMethod(method).getName();
                Field field = object.getClass().getDeclaredField(fieldName);

                field.setAccessible(true);


                Call httpCall = new HttpCall(new UriBuilder(uri).build(), HttpMethod.GET, null, ImmutableMultimap.of(), method.getGenericReturnType());

                Object newValue = executor.execute(httpCall);
                field.set(object, newValue);
                field.set(o, newValue);

                return field.get(object);
            }

            return method.invoke(object, args);
        }
    }
}
