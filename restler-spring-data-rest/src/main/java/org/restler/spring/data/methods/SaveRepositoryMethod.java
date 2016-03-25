package org.restler.spring.data.methods;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMultimap;
import org.restler.client.Call;
import org.restler.client.RestlerException;
import org.restler.http.HttpCall;
import org.restler.http.HttpMethod;
import org.restler.spring.data.chain.ChainCall;
import org.restler.spring.data.proxy.ResourceProxy;
import org.restler.spring.data.util.CloneMaker;
import org.restler.util.Pair;
import org.restler.spring.data.util.Repositories;
import org.restler.spring.data.util.RepositoryUtils;
import org.restler.util.UriBuilder;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.Repository;

import javax.persistence.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

/**
 * CrudRepository save method implementation.
 */
public class SaveRepositoryMethod extends DefaultRepositoryMethod {

    private final String baseUri;
    private final String repositoryUri;
    private final Repositories repositories;

    private static final ObjectMapper objectMapper = new ObjectMapper();
    static {
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    public SaveRepositoryMethod(String baseUri, String repositoryUri, Repositories repositories) {
        this.baseUri = baseUri;
        this.repositoryUri = repositoryUri;
        this.repositories = repositories;
    }

    @Override
    public boolean isRepositoryMethod(Method method) {
        try {
            return CrudRepository.class.getMethod("save", Object.class).equals(method);
        } catch (NoSuchMethodException e) {
            throw new RestlerException("Can't find CrudRepository.save method.", e);
        }
    }

    @Override
    public Call getCall(URI uri, Class<?> declaringClass, Object[] args) {
        ResourceTree resourceTree = makeTree(args[0], new HashSet<>());
        Object currentObject = resourceTree.getTopResource();

        resourceTree.forEach(resource-> {
            if(resource != currentObject) {
                saveResource(resource);
            }
        });

        List<Call> calls = new ArrayList<>();

        Type returnType;

        if(args[0] instanceof ResourceProxy) {
            ResourceProxy resourceProxy = (ResourceProxy) args[0];
            returnType = resourceProxy.getObject().getClass();
            calls.add(update(resourceProxy, currentObject));
        } else {
            Object object = args[0];
            returnType = object.getClass();
            calls.add(add(currentObject));
        }

        calls.add(makeAssociations(args[0], getChildren(args[0])));

        return new ChainCall((new FilterNullResults())::filter, calls, returnType);
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

    //need for filter result that return associate call
    private class FilterNullResults {
        private Object temp = null;

        Object filter(Object object) {
            if(object != null) {
                temp = object;
            }
            return temp;
        }
    }


    private Object getRequestBody(Object arg) {
        if(arg instanceof ResourceProxy) {
            ResourceProxy resourceProxy = (ResourceProxy)arg;
            arg = resourceProxy.getObject();
        }

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

    /**
     * Builds resource tree for some object.
     * It is recursive method that passes each resource and add it to ResourceTree.
     * Also the method removes all references between resources, as result
     * ResourceTree will contain resources without references to other resources.
     * @param object that used for building resource tree.
     * @param set it is set of references that had visited already.
     */
    private ResourceTree makeTree(Object object, Set<Object> set) {

        set.add(object);

        if(object instanceof ResourceProxy) {
            object = ((ResourceProxy) object).getObject();
        }

        object = CloneMaker.shallowClone(object);

        List<Pair<Field, Object>> children = getChildren(object).
                stream().
                filter(this::isResourceOrCollection).
                collect(Collectors.toList());

        List<ResourceTree> resourceChildren = new ArrayList<>();

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

                child.getFirstValue().set(object, null);

                child.getFirstValue().setAccessible(false);
            }
        } catch (IllegalAccessException e) {
            throw new RestlerException("Can't set value to field.", e);
        }

        return new ResourceTree(resourceChildren, object);
    }

    private boolean isResourceOrCollection(Pair<Field, Object> item) {
        Object value = item.getSecondValue();
        if(value instanceof ResourceProxy) {
            value = ((ResourceProxy)item.getSecondValue()).getObject();
        }
        return value.getClass().isAnnotationPresent(Entity.class) &&
                getId(value) != null ||
                value instanceof Collection;
    }

    private Object saveResource(Object resource) {
        if(resource instanceof ResourceProxy) {
            resource = ((ResourceProxy)resource).getObject();
        }

        Repository repository = repositories.getByResourceClass(resource.getClass()).orElse(null);

        if(repository == null) {
            return null;
        }

        if(repository instanceof CrudRepository) {
            return ((CrudRepository)repository).save(resource);
        }

        return null;
    }

    /**
     * Make calls that associated parent and children using OneToMany and ManyToOne associations.
     * It uses OneToMany and ManyToOne annotations for building associations.
     */
    private ChainCall makeAssociations(Object parent, List<Pair<Field, Object>> children) {
        List<Call> calls = new ArrayList<>();

        String fieldName;

        for(Pair<Field, Object> child : children) {
            if(child.getFirstValue().isAnnotationPresent(OneToMany.class)) {
                OneToMany annotation = child.getFirstValue().getAnnotation(OneToMany.class);
                fieldName = annotation.mappedBy();

                if(!fieldName.isEmpty()) {
                    if (child.getSecondValue() instanceof Collection) {
                        for (Object item : (Collection) child.getSecondValue()) {
                            calls.add(associate(parent, item, fieldName));
                        }
                    } else {
                        calls.add(associate(parent, child.getSecondValue(), fieldName));
                    }
                }
            } else if(child.getFirstValue().isAnnotationPresent(ManyToOne.class)) {
                calls.add(associate(child.getSecondValue(), parent, child.getFirstValue().getName()));
            }
        }

        return new ChainCall(calls, String.class);
    }

    private String getUri(Object object) {
        String result;
        if(object instanceof ResourceProxy) {
            result = ((ResourceProxy) object).getSelfUri();
        } else {
            Repository repository = repositories.getByResourceClass(object.getClass()).orElse(null);

            if(repository == null) {
                throw new RestlerException("Can't find repository " + object.getClass() + ".");
            }

            Object id = getId(object);
            if(id == null) {
                return null;
            }
            result = baseUri + "/" + RepositoryUtils.getRepositoryPath(repository.getClass().getInterfaces()[0]) + "/" + id;
        }

        return result;
    }

    private Call associate(Object parent, Object child, String fieldName) {
        String parentUri;
        String childUri;

        parentUri = getUri(parent);
        childUri = getUri(child);

        if(parentUri == null || childUri == null) {
            return null;
        }

        ImmutableMultimap<String, String> header = ImmutableMultimap.of("Content-Type", "text/uri-list");

        /**
         * The call creates request for associating parent and child.
         * PUT uses for adding new associations between resources
         * {@link http://docs.spring.io/spring-data/rest/docs/current/reference/html/#_put_2}
         * */
        return new HttpCall(new UriBuilder(childUri + "/" + fieldName).build(), HttpMethod.PUT, parentUri, header, String.class);
    }

    private Call add(Object object) {
        if(getId(object) == null) {
            return null;
        }

        Object body = getRequestBody(object);
        ImmutableMultimap<String, String> header = ImmutableMultimap.of("Content-Type", "application/json");

        //POST uses for creating new entity
        return new HttpCall(new UriBuilder(repositoryUri).build(), HttpMethod.POST, body, header, object.getClass());
    }

    private Call update(ResourceProxy resource, Object objectWithoutCycle) {
        Object body = getRequestBody(objectWithoutCycle);
        ImmutableMultimap<String, String> header = ImmutableMultimap.of("Content-Type", "application/json");

        //PUT uses for replacing values
        return new HttpCall(new UriBuilder(resource.getSelfUri()).build(), HttpMethod.PUT, body, header, resource.getObject().getClass());
    }

    private Object getId(Object object) {
        Field[] fields = object.getClass().getDeclaredFields();

        for (Field field : fields) {
            if (field.getDeclaredAnnotation(Id.class) != null || field.getDeclaredAnnotation(EmbeddedId.class) != null) {
                field.setAccessible(true);

                try {
                    Object id = field.get(object);
                    field.setAccessible(false);
                    return id;
                } catch (IllegalAccessException e) {
                    throw new RestlerException("Can't get value from id field.", e);
                }
            }
        }

        throw new RestlerException("Can't get id.");
    }

    private class ResourceTree implements Iterable<Object> {
        private final Object resource;
        private final List<ResourceTree> children;

        ResourceTree(List<ResourceTree> children, Object resource) {
            this.resource = resource;
            this.children = children;
        }

        ResourceTree(Object resource) {
            this(new ArrayList<>(), resource);
        }

        public Object getTopResource() {
            return resource;
        }

        @Override
        public Iterator<Object> iterator() {
            return new TreeIterator(this);
        }

        private class TreeIterator implements Iterator<Object> {

            private final Stack<Pair<ResourceTree, Iterator<ResourceTree>>> greyNodes = new Stack<>();

            public TreeIterator(ResourceTree firstNode) {
                Iterator<ResourceTree> childrenIterator = firstNode.children.iterator();
                greyNodes.push(new Pair<>(firstNode, childrenIterator));
            }

            @Override
            public boolean hasNext() {
                return !greyNodes.empty();
            }

            @Override
            public Object next() {
                Pair<ResourceTree, Iterator<ResourceTree>> pair = goToBottom(greyNodes.peek());
                Object resource = pair.getFirstValue().resource;
                greyNodes.pop();

                return resource;
            }

            private Pair<ResourceTree, Iterator<ResourceTree>> goToBottom(Pair<ResourceTree, Iterator<ResourceTree>> node) {
                Iterator<ResourceTree> childrenIterator = node.getSecondValue();

                while(childrenIterator.hasNext()) {
                    ResourceTree newNode = childrenIterator.next();
                    childrenIterator = newNode.children.iterator();
                    greyNodes.add(new Pair<>(newNode, childrenIterator));
                }

                return greyNodes.peek();
            }
        }
    }
}
