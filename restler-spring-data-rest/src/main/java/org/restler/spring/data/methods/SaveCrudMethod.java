package org.restler.spring.data.methods;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMultimap;
import org.restler.client.*;
import org.restler.http.HttpCall;
import org.restler.http.HttpMethod;
import org.restler.spring.data.chain.ChainCall;
import org.restler.spring.data.proxy.Resource;
import org.restler.util.UriBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;

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
    public Call getCall(Object[] args) {
        return makeCall(args[0]);
    }

    @Override
    public Object getRequestBody(Object[] args) {
        Object arg = args[0];

        if(arg instanceof Resource) {
            Resource resource = (Resource)arg;
            arg = resource.getObject();
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
    public String getPathPart(Object[] args) {
        Object arg = args[0];
        if(arg instanceof Resource) {
            Resource resource = (Resource)arg;

            return resource.getResourceId().toString();
        }

        return "{id}";
    }

    @Override
    public ImmutableMultimap<String, String> getHeader() {
        return ImmutableMultimap.of("Content-Type", "application/json");
    }

    private String getFullPath(Object object) {
        if(object instanceof Resource) {
            Resource resource = (Resource)object;
            return resource.getRepositoryUri() + "/" + resource.getResourceId().toString();
        }
        return null;
    }

    private Call makeCall(Object object) {
        List<AbstractMap.SimpleEntry<Field, Object>> childs = getChilds(object);

        List<Call> calls = new ArrayList<>();
        List<Function< Object, Object>> functions = new ArrayList<>();

        try {
            for(AbstractMap.SimpleEntry<Field, Object> child : childs) {
                child.getKey().setAccessible(true);
                Object value = child.getValue();

                if(value instanceof Collection) {
                    Collection collection = (Collection) value;

                    for(Object item : collection) {
                        calls.add(makeCall(item));
                    }
                } else if(value instanceof Resource) {
                    calls.add(makeCall(value));
                }

                if(object instanceof Resource) {
                    child.getKey().set(((Resource) object).getObject(), null);
                }
            }
        } catch (IllegalAccessException e) {
            throw new RestlerException("Can't set value to field.", e);
        }

        String fullPath  = getFullPath(object);

        if(object instanceof Resource) {
            object = ((Resource)object).getObject();
        }

        //if resource have repository and id
        if(fullPath != null) {
            calls.add(getHttpCall(object, fullPath, object.getClass()));
        }


        return new ChainCall(calls, functions);
    }

    private List<AbstractMap.SimpleEntry<Field, Object>> getChilds(Object object) {
        List<AbstractMap.SimpleEntry<Field, Object>> result = new ArrayList<>();

        if(object instanceof Resource) {
            object = ((Resource) object).getObject();
        }

        Class<?> argClass = object.getClass();

        Field[] fields = argClass.getDeclaredFields();

        for(Field field : fields) {
            try {
                field.setAccessible(true);
                Object fieldValue = field.get(object);

                if(fieldValue != null) {
                    if(fieldValue instanceof Collection) {
                        result.add(new AbstractMap.SimpleEntry<>(field, fieldValue));
                    } else if(fieldValue instanceof Resource) {
                        result.add(new AbstractMap.SimpleEntry<>(field, fieldValue));
                    }
                }

                field.setAccessible(false);
            } catch (IllegalAccessException e) {
                throw new RestlerException("Can't get value from field", e);
            }
        }

        return result;
    }

    private HttpCall getHttpCall(Object object, String path, Class<?> returnType) {
        Object[] objects = {object};

        Object body = getRequestBody(objects);
        ImmutableMultimap<String, String> header = getHeader();

        return new HttpCall(new UriBuilder(path).build(), getHttpMethod(), body, header, returnType);
    }
}
