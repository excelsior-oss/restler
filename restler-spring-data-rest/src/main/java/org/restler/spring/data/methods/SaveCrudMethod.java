package org.restler.spring.data.methods;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMultimap;
import org.restler.client.*;
import org.restler.http.HttpCall;
import org.restler.http.HttpMethod;
import org.restler.spring.data.chain.ChainCall;
import org.restler.spring.data.proxy.ResourceProxy;
import org.restler.util.UriBuilder;

import javax.persistence.OneToMany;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;

public class SaveCrudMethod implements CrudMethod
{
    @Override
    public boolean isCrudMethod(Method method) {
        return "save".equals(method.getName());
    }

    @Override
    public Call getCall(Object[] args) {
        return makeCall(args[0], new HashSet<>());
    }

    @Override
    public Object getRequestBody(Object[] args) {
        Object arg = args[0];

        if(arg instanceof ResourceProxy) {
            ResourceProxy resourceProxy = (ResourceProxy)arg;
            arg = resourceProxy.getObject();
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
        return HttpMethod.POST;
    }

    @Override
    public String getPathPart(Object[] args) {
        Object arg = args[0];
        if(arg instanceof ResourceProxy) {
            ResourceProxy resourceProxy = (ResourceProxy)arg;

            return resourceProxy.getResourceId().toString();
        }

        return "{id}";
    }

    @Override
    public ImmutableMultimap<String, String> getHeader() {
        return ImmutableMultimap.of("Content-Type", "application/json");
    }

    private String getFullPath(Object object) {
        if(object instanceof ResourceProxy) {
            ResourceProxy resourceProxy = (ResourceProxy)object;
            return resourceProxy.getRepositoryUri();
        }
        return null;
    }

    private Call makeCall(Object object, Set<Object> set) {
        List<AbstractMap.SimpleEntry<Field, Object>> childs = getChilds(object);

        List<Call> calls = new ArrayList<>();
        List<Function< Object, Object>> functions = new ArrayList<>();

        List<String> childHrefs = new ArrayList<>();

        try {
            for(AbstractMap.SimpleEntry<Field, Object> child : childs) {

                child.getKey().setAccessible(true);

                if(set.contains(child.getValue()) && object instanceof ResourceProxy) {
                    child.getKey().set(((ResourceProxy) object).getObject(), null);
                    continue;
                }

                set.add(child.getValue());

                Object value = child.getValue();

                String fieldName = null;

                if(child.getKey().isAnnotationPresent(OneToMany.class)) {
                    OneToMany oneToMany = child.getKey().getAnnotation(OneToMany.class);
                    fieldName = oneToMany.mappedBy();
                }

                if(value instanceof Collection) {
                    Collection collection = (Collection) value;

                    for(Object item : collection) {
                        if(fieldName != null && item instanceof ResourceProxy) {
                            childHrefs.add(((ResourceProxy) item).getSelfUri() + "/" + fieldName);
                        }
                        calls.add(makeCall(item, set));
                    }
                } else if(value instanceof ResourceProxy) {
                    if(fieldName != null) {
                        childHrefs.add(((ResourceProxy) value).getSelfUri() + "/" + fieldName);
                    }
                    calls.add(makeCall(value, set));
                }

                if(object instanceof ResourceProxy) {
                    child.getKey().set(((ResourceProxy) object).getObject(), null);
                }
            }
        } catch (IllegalAccessException e) {
            throw new RestlerException("Can't set value to field.", e);
        }

        String fullPath  = getFullPath(object);

        if(fullPath != null) {
            Object callObject = object;
            if(object instanceof ResourceProxy) {
                callObject = ((ResourceProxy)object).getObject();
            }
            calls.add(getHttpCall(callObject, fullPath, callObject.getClass()));
        }

        if(object instanceof ResourceProxy) {
            for (String childHref : childHrefs) {
                calls.add(new HttpCall(new UriBuilder(childHref).build(), HttpMethod.PUT, ((ResourceProxy) object).getSelfUri(), ImmutableMultimap.of("Content-Type", "text/uri-list"), String.class));
            }
        }
        return new ChainCall(calls, functions);
    }

    private List<AbstractMap.SimpleEntry<Field, Object>> getChilds(Object object) {
        List<AbstractMap.SimpleEntry<Field, Object>> result = new ArrayList<>();

        if(object instanceof ResourceProxy) {
            object = ((ResourceProxy) object).getObject();
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
                    } else if(fieldValue instanceof ResourceProxy) {
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
