package org.restler.spring.data.methods;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMultimap;
import org.restler.client.*;
import org.restler.http.HttpCall;
import org.restler.http.HttpMethod;
import org.restler.spring.data.Repositories;
import org.restler.spring.data.chain.ChainCall;
import org.restler.spring.data.proxy.ResourceProxy;
import org.restler.util.UriBuilder;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.Repository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import javax.persistence.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SaveCrudMethod implements CrudMethod
{

    private String repositoryUri;
    private Repositories repositories;

    public SaveCrudMethod(String repositoryUri, Repositories repositories) {
        this.repositoryUri = repositoryUri;
        this.repositories = repositories;
    }

    @Override
    public boolean isCrudMethod(Method method) {
        return "save".equals(method.getName());
    }

    @Override
    public Call getCall(Object[] args) {

        List<Pair<Field, Object>> childs = getChilds(args[0]);
        ResourceTree resourceTree = makeTree(args[0], new HashSet<>());

        resourceTree.forEach(resource-> {
            if(resource != args[0]) {
                saveResource(resource);
            }
        });

        List<Call> calls = new ArrayList<>();

        if(args[0] instanceof ResourceProxy) {
            calls.add(update((ResourceProxy) args[0]));
        } else {
            calls.add(add(args[0]));
        }

        calls.add(makeLinks(args[0], childs));

        return new ChainCall(calls);
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

    private List<Pair<Field, Object>> getChilds(Object object) {
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
        List<Pair<Field, Object>> childs = getChilds(object);

        set.add(object);

        childs = childs.stream().filter(
                item-> {
                        Object value = item.secondValue;
                        if(value instanceof ResourceProxy) {
                            value = ((ResourceProxy)item.secondValue).getObject();
                        }
                        return value.getClass().isAnnotationPresent(Entity.class) &&
                                getId(value) != null ||
                                value instanceof Collection;
                }
        ).collect(Collectors.toList());

        List<ResourceTree> resourceChilds = new ArrayList<>();

        if(childs.isEmpty()) {
            return new ResourceTree(object);
        }

        try {
            for(Pair<Field, Object> child : childs) {
                child.firstValue.setAccessible(true);

                if(!set.contains(child.secondValue)) {
                    if (child.secondValue instanceof Collection) {
                        for (Object item : (Collection) child.secondValue) {
                            resourceChilds.add(makeTree(item, set));
                        }
                    } else {
                        resourceChilds.add(makeTree(child.secondValue, set));
                    }
                }

                if(object instanceof ResourceProxy) {
                    child.firstValue.set(((ResourceProxy) object).getObject(), null);
                } else {
                    child.firstValue.set(object, null);
                }
            }
        } catch (IllegalAccessException e) {
            throw new RestlerException("Can't set value to field.", e);
        }

        return new ResourceTree(resourceChilds, object);
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

    private ChainCall makeLinks(Object parent, List<Pair<Field, Object>> childs) {
        List<Call> calls = new ArrayList<>();

        String fieldName;

        for(Pair<Field, Object> child : childs) {
            if(child.firstValue.isAnnotationPresent(OneToMany.class)) {
                OneToMany annotation = child.firstValue.getAnnotation(OneToMany.class);
                fieldName = annotation.mappedBy();

                if(fieldName != null) {
                    if (child.secondValue instanceof Collection) {
                        for (Object item : (Collection) child.secondValue) {
                            calls.add(link(parent, item, fieldName));
                        }
                    } else {
                        calls.add(link(parent, child.secondValue, fieldName));
                    }
                }
            } else if(child.firstValue.isAnnotationPresent(ManyToOne.class)) {

                calls.add(link(child.secondValue, parent, child.firstValue.getName()));

            }
        }

        return new ChainCall(calls);
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
            result = repositories.getRepositoryUri(repository.getClass().getInterfaces()[0]) + "/" + id;
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

        Object[] objects = {object};

        Object body = getRequestBody(objects);
        ImmutableMultimap<String, String> header = ImmutableMultimap.of("Content-Type", "application/json");

        return new HttpCall(new UriBuilder(repositoryUri).build(), HttpMethod.POST, body, header, object.getClass());
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

    private class ResourceTree {
        private Object resource;
        private List<ResourceTree> childs = null;

        ResourceTree(Object resource) {
            this.resource = resource;
        }

        ResourceTree(List<ResourceTree> childs, Object resource) {
            this(resource);
            this.childs = childs;
        }

        void forEach(Consumer<Object> consumer) {
            if(childs == null) {
                consumer.accept(resource);
                return;
            }

            for(ResourceTree child : childs) {
                child.forEach(consumer);
            }

            consumer.accept(resource);
        }
    }

    private class Pair<T1, T2> {
        Pair(T1 firstValue, T2 secondValue) {
            this.firstValue = firstValue;
            this.secondValue = secondValue;
        }
        T1 firstValue;
        T2 secondValue;
    }


}
