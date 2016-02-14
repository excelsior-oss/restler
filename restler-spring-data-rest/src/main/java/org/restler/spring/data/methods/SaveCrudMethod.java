package org.restler.spring.data.methods;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.PropertyFilter;
import com.fasterxml.jackson.databind.ser.PropertyWriter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.reflect.TypeToken;
import org.restler.client.Call;
import org.restler.client.CallExecutor;
import org.restler.client.RestlerException;
import org.restler.http.HttpCall;
import org.restler.http.HttpMethod;
import org.restler.spring.data.proxy.ProxyObject;
import org.restler.util.UriBuilder;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.*;

/**
 * Created by rudenko on 11.02.2016.
 */
public class SaveCrudMethod implements CrudMethod
{
    @Override
    public boolean isCrudMethod(Method method) {
        return "save".equals(method.getName());
    }

    @Override
    public Object getRequestBody(Object[] args) {
        Object arg = args[0];

        if(arg instanceof ProxyObject) {
            ProxyObject proxyObject = (ProxyObject)arg;
            arg = proxyObject.getObject();
            saveChilds(arg, proxyObject.getCallExecutor());
        }


        ObjectMapper objectMapper = new ObjectMapper();

        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        String json;
        try {
            json = objectMapper.writeValueAsString(arg);
        } catch (JsonProcessingException e) {
            throw new RestlerException("Can't create json from object", e);
        }

        return json;
    }

    @Override
    public HttpMethod getHttpMethod() {
        return HttpMethod.PUT;
    }

    @Override
    public String getPathSegment(Object[] args) {
        Object arg = args[0];

        if(arg instanceof ProxyObject) {
            ProxyObject proxyObject = (ProxyObject)arg;
            HashMap<String, String> hrefs = proxyObject.getHrefs();
            String self = hrefs.get("self");

            return self.substring(self.lastIndexOf('/'));
        }

        return "{id}";
    }

    @Override
    public ImmutableMultimap<String, String> getHeader() {
        return ImmutableMultimap.of("Content-Type", "application/json");
    }

    private Object saveChilds(Object object, CallExecutor executor) {
        Class<?> argClass = object.getClass();

        Field[] fields = argClass.getDeclaredFields();

        for(Field field : fields) {
            try {
                field.setAccessible(true);
                Object fieldValue = field.get(object);


                if(fieldValue != null) {
                    if(fieldValue instanceof Collection) {
                        Collection collection = (Collection) fieldValue;


                        Collection resultCollection = collection.getClass().getConstructor().newInstance(null);

                        for(Object item : collection) {
                            if(item instanceof ProxyObject) {
                                saveProxyObject((ProxyObject)item, executor);
                                //resultCollection.add(((ProxyObject) item).getHrefs().get("self"));
                            }
                            else {
                                resultCollection.add(item);
                            }
                        }

                        field.set(object, null);

                    } else if(fieldValue instanceof ProxyObject) {
                        saveProxyObject((ProxyObject) fieldValue, executor);
                        field.set(object, null);
                        //field.set(object, ((ProxyObject) fieldValue).getHrefs().get("self"));
                    }
                }

                field.setAccessible(false);
            } catch (IllegalAccessException e) {
                throw new RestlerException("Can't use field", e);
            } catch (NoSuchMethodException e) {
                throw new RestlerException("Can't get collection constructor", e);
            } catch (InstantiationException e) {
                throw new RestlerException("Can't get collection constructor", e);
            } catch (InvocationTargetException e) {
                throw new RestlerException("Can't get collection constructor", e);
            }
        }

        return object;
    }

    private Object saveProxyObject(ProxyObject proxyObject, CallExecutor executor) {
        Object[] args = {proxyObject};
        Object body = getRequestBody(args);
        ImmutableMultimap<String, String> header = getHeader();

        Call httpCall = new HttpCall(new UriBuilder(proxyObject.getHrefs().get("self")).build(), HttpMethod.PUT, body, header, proxyObject.getObject().getClass());
        Object result = executor.execute(httpCall);

        if(result instanceof ProxyObject) {
            return ((ProxyObject)result).getObject();
        }

        return result;
    }
}
