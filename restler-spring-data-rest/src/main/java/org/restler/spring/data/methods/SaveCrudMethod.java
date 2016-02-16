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

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SaveCrudMethod implements CrudMethod
{

    private String baseUri;

    public SaveCrudMethod(String baseUri) {
        this.baseUri = baseUri;
    }

    @Override
    public boolean isCrudMethod(Method method) {
        return "save".equals(method.getName());
    }

    @Override
    public Call getCall(Object[] args) {
        return save(args[0], new HashSet<>());
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
        return HttpMethod.PUT;
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
                    result.add(new AbstractMap.SimpleEntry<>(field, fieldValue));
                }

                field.setAccessible(false);
            } catch (IllegalAccessException e) {
                throw new RestlerException("Can't get value from field", e);
            }
        }

        return result;
    }

    private ChainCall save(Object object, Set<Object> set) {
        List<AbstractMap.SimpleEntry<Field, Object>> childs = getChilds(object);

        set.add(object);

        childs = childs.stream().filter(
                item-> {
                        Object value = item.getValue();
                        if(value instanceof ResourceProxy) {
                            value = ((ResourceProxy)item.getValue()).getObject();
                        }
                        return value.getClass().isAnnotationPresent(Entity.class) &&
                                getId(value) != null ||
                                value instanceof Collection;
                }
        ).collect(Collectors.toList());

        List<Call> calls = new ArrayList<>();

        if(childs.isEmpty()) {
            if(object instanceof ResourceProxy) {
                calls.add(update((ResourceProxy) object));
            } else {
                calls.add(add(object));
            }

            return new ChainCall(calls);
        }

        try {
            for(AbstractMap.SimpleEntry<Field, Object> child : childs) {
                child.getKey().setAccessible(true);

                if(set.contains(child.getValue()) && object instanceof ResourceProxy) {
                    child.getKey().set(((ResourceProxy) object).getObject(), null);
                    continue;
                }

                if(child.getValue() instanceof Collection) {
                    for(Object item : (Collection)child.getValue()) {
                        calls.add(save(item, set));
                    }
                } else {
                    calls.add(save(child.getValue(), set));
                }

                if(object instanceof ResourceProxy) {
                    child.getKey().set(((ResourceProxy) object).getObject(), null);
                } else {
                    child.getKey().set(object, null);
                }
            }
        } catch (IllegalAccessException e) {
            throw new RestlerException("Can't set value to field.", e);
        }

        if(object instanceof ResourceProxy) {
            calls.add(update((ResourceProxy) object));
            List<AbstractMap.SimpleEntry<Field, Object>> linkedChilds = childs.stream().filter(child->!(child instanceof ResourceProxy)).collect(Collectors.toList());
            calls.add(makeLinks(object, linkedChilds));

        } else {
            calls.add(add(object));
            calls.add(makeLinks(object, childs));
        }

        calls = calls.stream().filter(call->call!=null).collect(Collectors.toList());

        return new ChainCall(calls);
    }

    private ChainCall makeLinks(Object parent, List<AbstractMap.SimpleEntry<Field, Object>> childs) {
        List<Call> calls = new ArrayList<>();

        String fieldName;

        for(AbstractMap.SimpleEntry<Field, Object> child : childs) {
            if(child.getKey().isAnnotationPresent(OneToMany.class)) {
                OneToMany annotation = child.getKey().getAnnotation(OneToMany.class);
                fieldName = annotation.mappedBy();

                if(fieldName != null) {
                    if (child.getValue() instanceof Collection) {
                        for (Object item : (Collection) child.getValue()) {
                            calls.add(link(parent, item, fieldName));
                        }
                    } else {
                        calls.add(link(parent, child.getValue(), fieldName));
                    }
                }
            }
        }

        return new ChainCall(calls);
    }

    private Call link(Object parent, Object child, String fieldName) {
        String parentUri;
        String childUri;
        if(parent instanceof ResourceProxy) {
            parentUri = ((ResourceProxy) parent).getSelfUri();
        } else {
            Object id = getId(parent);


            if(id == null) {
                return null;
            }

            parentUri = baseUri + "/" + parent.getClass().getName().toLowerCase() + "s" + "/" + id;
        }

        if(child instanceof ResourceProxy) {
            childUri = ((ResourceProxy) child).getSelfUri();
        } else {
            Object id = getId(child);

            if(id == null) {
                return null;
            }

            childUri = baseUri + "/" + child.getClass().getName().toLowerCase() + "s" + "/" + id;
        }

        ImmutableMultimap<String, String> header = ImmutableMultimap.of("Content-Type", "text/uri-list");

        return new HttpCall(new UriBuilder(childUri + "/" + fieldName).build(), HttpMethod.PUT, parentUri, header, String.class);
    }

    private Call add(Object object) {

        if(getId(object) == null) {
            return null;
        }

        Object[] objects = {object};

        String repositoryPath = baseUri + "/" + object.getClass().getName().toLowerCase() + "s";

        Object body = getRequestBody(objects);
        ImmutableMultimap<String, String> header = ImmutableMultimap.of("Content-Type", "application/json");

        return new HttpCall(new UriBuilder(repositoryPath).build(), HttpMethod.PUT, body, header, object.getClass());
    }

    private Call update(ResourceProxy resource) {
        Object[] objects = {resource};

        Object body = getRequestBody(objects);
        ImmutableMultimap<String, String> header = ImmutableMultimap.of("Content-Type", "application/json");

        return new HttpCall(new UriBuilder(resource.getSelfUri()).build(), HttpMethod.PUT, body, header, resource.getObject().getClass());
    }

    private Object getId(Object object) {
        Field[] fields = object.getClass().getDeclaredFields();

        for (Field field : fields) {
            if (field.getDeclaredAnnotation(Id.class) != null || field.getDeclaredAnnotation(EmbeddedId.class) != null) {
                field.setAccessible(true);

                try {
                    return field.get(object);
                } catch (IllegalAccessException e) {
                    throw new RestlerException("Can't get value from id field.", e);
                }
            }
        }

        throw new RestlerException("Can't get id.");
    }
}
