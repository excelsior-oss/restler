package org.restler.spring.data.methods;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMultimap;
import org.restler.client.Call;
import org.restler.client.RestlerException;
import org.restler.http.HttpCall;
import org.restler.http.HttpMethod;
import org.restler.spring.data.Pair;
import org.restler.spring.data.Repositories;
import org.restler.spring.data.RepositoryUtils;
import org.restler.spring.data.chain.ChainCall;
import org.restler.spring.data.proxy.ResourceProxy;
import org.restler.util.UriBuilder;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.Repository;

import javax.persistence.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class SaveRepositoryMethod extends DefaultRepositoryMethod {

    private final String baseUri;
    private final String repositoryUri;
    private final Repositories repositories;

    public SaveRepositoryMethod(String baseUri, String repositoryUri, Repositories repositories) {
        this.baseUri = baseUri;
        this.repositoryUri = repositoryUri;
        this.repositories = repositories;
    }

    @Override
    public boolean isRepositoryMethod(Method method) {
        return "save".equals(method.getName());
    }

    @Override
    public Call getCall(URI uri, Class<?> declaringClass, Object[] args) {

        List<Pair<Field, Object>> children = getChildren(args[0]);
        ResourceTree resourceTree = makeTree(args[0], new HashSet<>());

        resourceTree.forEach(resource-> {
            if(resource != args[0]) {
                saveResource(resource);
            }
        });

        List<Call> calls = new ArrayList<>();

        Type returnType;

        if(args[0] instanceof ResourceProxy) {
            ResourceProxy resourceProxy = (ResourceProxy) args[0];
            returnType = resourceProxy.getObject().getClass();
            calls.add(update(resourceProxy));
        } else {
            Object object = args[0];
            returnType = object.getClass();
            calls.add(add(object));
        }

        calls.add(makeLinks(args[0], children));

        return new ChainCall(calls, returnType);
    }

    @Override
    public String getPathPart(Object[] args) {
        Object arg = args[0];
        if(arg instanceof ResourceProxy) {
            ResourceProxy resourceProxy = (ResourceProxy)arg;

            return resourceProxy.getResourceId().toString();
        }

        return getId(arg).toString();
    }


    private Object getRequestBody(Object arg) {
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

    private List<Pair<Field, Object>> getChildren(Object object) {
        List<Pair<Field, Object>> result = new ArrayList<>();

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
                    result.add(new Pair<>(field, fieldValue));
                }

                field.setAccessible(false);
            } catch (IllegalAccessException e) {
                throw new RestlerException("Can't get value from field", e);
            }
        }

        return result;
    }

    private ResourceTree makeTree(Object object, Set<Object> set) {

        List<Pair<Field, Object>> children = getChildren(object);

        set.add(object);

        children = children.stream().filter(
                item-> {
                        Object value = item.getSecondValue();
                        if(value instanceof ResourceProxy) {
                            value = ((ResourceProxy)item.getSecondValue()).getObject();
                        }
                        return value.getClass().isAnnotationPresent(Entity.class) &&
                                getId(value) != null ||
                                value instanceof Collection;
                }
        ).collect(Collectors.toList());

        List<ResourceTree> resourceChildren = new ArrayList<>();

        if(children.isEmpty()) {
            return new ResourceTree(object);
        }

        try {
            for(Pair<Field, Object> child : children) {
                child.getFirstValue().setAccessible(true);

                if(!set.contains(child.getSecondValue())) {
                    if (child.getSecondValue() instanceof Collection) {
                        for (Object item : (Collection) child.getSecondValue()) {
                            resourceChildren.add(makeTree(item, set));
                        }
                    } else {
                        resourceChildren.add(makeTree(child.getSecondValue(), set));
                    }
                }

                if(object instanceof ResourceProxy) {
                    child.getFirstValue().set(((ResourceProxy) object).getObject(), null);
                } else {
                    child.getFirstValue().set(object, null);
                }
            }
        } catch (IllegalAccessException e) {
            throw new RestlerException("Can't set value to field.", e);
        }

        return new ResourceTree(resourceChildren, object);
    }

    private Object saveResource(Object resource) {
        if(resource instanceof ResourceProxy) {
            resource = ((ResourceProxy)resource).getObject();
        }

        Repository repository = repositories.getByResourceClass(resource.getClass());

        if(repository == null) {
            return null;
        }

        if(repository instanceof CrudRepository) {
            return ((CrudRepository)repository).save(resource);
        }

        return null;
    }

    private ChainCall makeLinks(Object parent, List<Pair<Field, Object>> children) {
        List<Call> calls = new ArrayList<>();

        String fieldName;

        for(Pair<Field, Object> child : children) {
            if(child.getFirstValue().isAnnotationPresent(OneToMany.class)) {
                OneToMany annotation = child.getFirstValue().getAnnotation(OneToMany.class);
                fieldName = annotation.mappedBy();

                if(!fieldName.isEmpty()) {
                    if (child.getSecondValue() instanceof Collection) {
                        for (Object item : (Collection) child.getSecondValue()) {
                            calls.add(link(parent, item, fieldName));
                        }
                    } else {
                        calls.add(link(parent, child.getSecondValue(), fieldName));
                    }
                }
            } else if(child.getFirstValue().isAnnotationPresent(ManyToOne.class)) {

                calls.add(link(child.getSecondValue(), parent, child.getFirstValue().getName()));

            }
        }

        return new ChainCall(calls, String.class);
    }

    private String getUri(Object object) {
        String result;
        if(object instanceof ResourceProxy) {
            result = ((ResourceProxy) object).getSelfUri();
        } else {
            Repository repository = repositories.getByResourceClass(object.getClass());
            Object id = getId(object);
            if(id == null) {
                return null;
            }
            result = baseUri + "/" + RepositoryUtils.getRepositoryPath(repository.getClass().getInterfaces()[0]) + "/" + id;
        }

        return result;
    }

    private Call link(Object parent, Object child, String fieldName) {
        String parentUri;
        String childUri;

        parentUri = getUri(parent);
        childUri = getUri(child);

        if(parentUri == null || childUri == null) {
            return null;
        }

        ImmutableMultimap<String, String> header = ImmutableMultimap.of("Content-Type", "text/uri-list");

        return new HttpCall(new UriBuilder(childUri + "/" + fieldName).build(), HttpMethod.PUT, parentUri, header, String.class);
    }

    private Call add(Object object) {
        if(getId(object) == null) {
            return null;
        }

        Object body = getRequestBody(object);
        ImmutableMultimap<String, String> header = ImmutableMultimap.of("Content-Type", "application/json");

        return new HttpCall(new UriBuilder(repositoryUri).build(), HttpMethod.POST, body, header, object.getClass());
    }

    private Call update(ResourceProxy resource) {
        Object body = getRequestBody(resource);
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

    private class ResourceTree {
        private Object resource;
        private List<ResourceTree> children = null;

        ResourceTree(Object resource) {
            this.resource = resource;
        }

        ResourceTree(List<ResourceTree> children, Object resource) {
            this(resource);
            this.children = children;
        }

        void forEach(Consumer<Object> consumer) {
            if(children == null) {
                consumer.accept(resource);
                return;
            }

            for(ResourceTree child : children) {
                child.forEach(consumer);
            }

            consumer.accept(resource);
        }
    }
}
