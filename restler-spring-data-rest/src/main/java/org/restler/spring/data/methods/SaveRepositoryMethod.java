package org.restler.spring.data.methods;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.google.common.collect.ImmutableMultimap;
import org.restler.client.Call;
import org.restler.client.RestlerException;
import org.restler.http.HttpCall;
import org.restler.http.HttpMethod;
import org.restler.spring.data.calls.ChainCall;
import org.restler.spring.data.proxy.ResourceProxy;
import org.restler.spring.data.util.Repositories;
import org.restler.spring.data.util.RepositoryUtils;
import org.restler.util.Pair;
import org.restler.util.UriBuilder;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.Repository;

import javax.persistence.*;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

/**
 * CrudRepository save method implementation.
 */
public class SaveRepositoryMethod extends DefaultRepositoryMethod {

    private static final Method saveMethod;

    static {
        try {
            saveMethod = CrudRepository.class.getMethod("save", Object.class);
        } catch (NoSuchMethodException e) {
            throw new RestlerException("Can't find CrudRepository.save method.", e);
        }
    }

    private final String baseUri;
    private final Repositories repositories;

    private static final ObjectMapper objectMapper = new ObjectMapper();
    static {
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.addMixIn(Object.class, PropertyFilterMixIn.class);
    }

    public SaveRepositoryMethod(String baseUri, Repositories repositories) {
        this.baseUri = baseUri;
        this.repositories = repositories;
    }

    @Override
    public boolean isRepositoryMethod(Method method) {
        return saveMethod.equals(method);
    }

    @Override
    public Call getCall(URI uri, Class<?> declaringClass, Object[] args) {

        Object resource = args[0];

        List<Call> calls = new ArrayList<>();

        ResourcesAndAssociations resourcesAndAssociations = new ResourcesAndAssociations(resource);
        createResources(resourcesAndAssociations).forEach(calls::add);
        updateResources(resourcesAndAssociations).forEach(calls::add);

        Class<?> returnType = (resource instanceof ResourceProxy) ? ((ResourceProxy) resource).getObject().getClass() : resource.getClass();

        return new ChainCall((Object prevResult, Object object) -> takeResult(prevResult, object, getId(resource), returnType), calls, returnType);
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

    //need for filtering results are returned by associate call
    private Object takeResult(Object prevResult, Object object, Object id, Class<?> type) {
        if(object != null && object instanceof ResourceProxy && ((ResourceProxy) object).getObject().getClass().equals(type) && getId(object).equals(id)) {
            return object;
        } else {
            return prevResult;
        }
    }

    private Object getRequestBody(ObjectNode node) {
        String json;
        try {
            json = objectMapper.writeValueAsString(node);
        } catch (JsonProcessingException e) {
            throw new RestlerException("Can't create json from object", e);
        }

        return json;
    }



    @JsonFilter("filter properties by name")
    private class PropertyFilterMixIn {}

    private boolean takeNullFields(List<Field> nullFields, Pair<Field, Object> object) {
        if(object.getSecondValue() == null) {
            nullFields.add(object.getFirstValue());
            return false;
        }
        return true;
    }

    private List<Association> associate(Resource parent, Field childField, Resource resource) {

        Object parentResource = parent.resource;

        if(parentResource instanceof ResourceProxy) {
            parentResource = ((ResourceProxy)parentResource).getObject();
        }

        Object childResource = resource.resource;

        if(childResource instanceof ResourceProxy) {
            childResource = ((ResourceProxy) childResource).getObject();
        }

        List<Association> result = new ArrayList<>();

        for(Field nullField : resource.associateFields) {
            //OneToOne oneToOneChild = nullField.getAnnotation(OneToOne.class);
            ManyToOne manyToOneChild = nullField.getAnnotation(ManyToOne.class);
            OneToMany oneToManyChild = nullField.getAnnotation(OneToMany.class);
            ManyToMany manyToManyChild = nullField.getAnnotation(ManyToMany.class);

            //OneToOne oneToOneParent = childField.getAnnotation(OneToOne.class);
            ManyToOne manyToOneParent = childField.getAnnotation(ManyToOne.class);
            OneToMany oneToManyParent = childField.getAnnotation(OneToMany.class);
            ManyToMany manyToManyParent = childField.getAnnotation(ManyToMany.class);

            if(manyToOneChild != null) {

                if(oneToManyParent != null &&
                        (oneToManyParent.mappedBy().equals(nullField.getName()) ||
                                nullField.getName().equals(parentResource.getClass().getSimpleName().toLowerCase()))) {

                    result.add(new Association(parent, resource, new Pair<>(childField.getName(), getUri(resource.resource)), AssociationType.OneToMany));
                    result.add(new Association(resource, parent, new Pair<>(nullField.getName(), getUri(parent.resource)), AssociationType.ManyToOne));
                    return result;
                }

            }

            if(oneToManyChild != null) {
                if(oneToManyChild.mappedBy().equals(childField.getName()) ||
                        childField.getName().equals(resource.getClass().getSimpleName().toLowerCase())) {

                    if(manyToOneParent != null) {
                        result.add(new Association(parent, resource, new Pair<>(childField.getName(), getUri(resource.resource)), AssociationType.ManyToOne));
                    }

                    result.add(new Association(resource, parent, new Pair<>(childField.getName(), getUri(parent.resource)), AssociationType.OneToMany));

                    return result;
                }
            }

            if(manyToManyChild != null) {
                if(manyToManyChild.mappedBy().equals(childField.getName()) ||
                        (childResource.getClass().getSimpleName().toLowerCase() + "s").equals(childField.getName()) ||
                        (manyToManyParent != null && manyToManyParent.mappedBy().equals(nullField.getName())) ||
                        (parentResource.getClass().getSimpleName().toLowerCase() + "s").equals(nullField.getName())) {

                    if(manyToManyParent != null) {
                        result.add(new Association(parent, resource, new Pair<>(childField.getName(), getUri(resource.resource)), AssociationType.ManyToMany));
                    }
                    result.add(new Association(resource, parent, new Pair<>(childField.getName(), getUri(parent.resource)), AssociationType.ManyToMany));

                    return result;
                }
            }
        }

        throw new RestlerException("Can't make association.");
    }

    private enum ResourceStatus {
        Update,
        Create,
        Done
    }

    private class Resource {
        final Object resource;
        final ObjectNode objectNode;

        final List<Field> associateFields;

        ResourceStatus status;

        public Resource(Object resource, ObjectNode objectNode, List<Field> associateFields, ResourceStatus status) {
            this.resource = resource;
            this.objectNode = objectNode;
            this.associateFields = associateFields;
            this.status = status;
        }
    }

    private enum AssociationType {
        OneToOne,
        OneToMany,
        ManyToOne,
        ManyToMany
    }

    private class Association {
        final Resource firstResource;
        final Resource secondResource;

        final Pair<String, String> jsonField;

        boolean resolved = false;

        final AssociationType associationType;

        public Association(Resource firstResource, Resource secondResource, Pair<String, String> jsonField, AssociationType associationType) {
            this.firstResource = firstResource;
            this.secondResource = secondResource;
            this.jsonField = jsonField;
            this.associationType = associationType;
        }
    }

    private class ResourcesAndAssociations {
        private final List<Resource> resources = new ArrayList<>();
        private final List<Association> associations = new ArrayList<>();

        public ResourcesAndAssociations(Object resource) {
            fillResourcesAndAssociations(resource, new HashSet<>());
        }

        public List<Resource> getResources() {
            return resources;
        }

        public List<Association> getAssociations() {
            return associations;
        }

        private Resource fillResourcesAndAssociations(Object object, Set<Object> set) {
            Object objectAtStart = object;
            set.add(object);

            if(object instanceof ResourceProxy) {
                object = ((ResourceProxy) object).getObject();
            }

            List<Field> associateFields = new ArrayList<>();

            List<Pair<Field, Object>> children = getChildren(object).
                    stream().
                    filter(o -> takeNullFields(associateFields, o)).
                    filter(this::isResourceOrCollection).
                    collect(Collectors.toList());

            List<String> ignorableFields = new ArrayList<>();

            ResourceStatus resourceStatus = (objectAtStart instanceof ResourceProxy) ? ResourceStatus.Update : ResourceStatus.Create;

            Resource currentResource = new Resource(objectAtStart, new ObjectNode(JsonNodeFactory.instance), associateFields, resourceStatus);

            resources.add(currentResource);

            for(Pair<Field, Object> child : children) {
                child.getFirstValue().setAccessible(true);

                if(!set.contains(child.getSecondValue())) {
                    if (child.getSecondValue() instanceof Collection) {
                        for (Object item : (Collection) child.getSecondValue()) {
                            Resource childResource = fillResourcesAndAssociations(item, set);
                            associate(currentResource, child.getFirstValue(), childResource).forEach(associations::add);
                        }
                    } else {
                        Resource childResource = fillResourcesAndAssociations(child.getSecondValue(), set);
                        associate(currentResource, child.getFirstValue(), childResource).forEach(associations::add);
                    }
                }

                ignorableFields.add(child.getFirstValue().getName());
                associateFields.add(child.getFirstValue());

                child.getFirstValue().setAccessible(false);
            }

            FilterProvider filters = new SimpleFilterProvider()
                    .addFilter("filter properties by name", SimpleBeanPropertyFilter.serializeAllExcept(ignorableFields.toArray(new String[ignorableFields.size()])));
            ObjectWriter writer = objectMapper.writer(filters);

            try {
                ObjectNode node = (ObjectNode) objectMapper.readTree(writer.writeValueAsString(object));
                currentResource.objectNode.setAll(node);
            } catch (IOException e) {
                throw new RestlerException("Can't convert object to json", e);
            }

            return currentResource;
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

                    result.add(new Pair<>(field, fieldValue));

                    field.setAccessible(false);
                } catch (IllegalAccessException e) {
                    throw new RestlerException("Can't get value from field", e);
                }
            }

            return result;
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
    }

    private List<Call> createResources(ResourcesAndAssociations resourcesAndAssociations) {
        List<Call> calls = new ArrayList<>();

        List<Resource> resources = resourcesAndAssociations.getResources().stream().
                filter(r -> r.status.equals(ResourceStatus.Create)).
                collect(Collectors.toList());
        List<Association> associations = resourcesAndAssociations.getAssociations();


        while(resources.size() > 0 && associations.stream().
                filter(a->a.firstResource.status.equals(ResourceStatus.Create)
                && !a.resolved).count() > 0) {
            long resourcesCreated = 0;

            for (Resource resource : resources) {
                List<Association> associationsForCurrentResource = associations.stream().
                        filter(a -> a.firstResource.equals(resource)).
                        collect(Collectors.toList());

                associationsForCurrentResource.stream().filter(a -> resolveAssociationCreate(associations, a)).
                        forEach(a ->a.resolved = true);

                List<Association> resolvedManyToOne = associationsForCurrentResource.stream().
                        filter(a -> a.associationType.equals(AssociationType.ManyToOne) && a.resolved).
                        collect(Collectors.toList());

                long manyToOneCount =
                        associationsForCurrentResource.stream().
                                filter(a -> a.associationType.equals(AssociationType.ManyToOne) && a.secondResource.status.equals(ResourceStatus.Create) && !a.resolved).
                                count();

                if (manyToOneCount == 0) {
                    resolvedManyToOne.forEach(a -> resource.objectNode.put(a.jsonField.getFirstValue(), a.jsonField.getSecondValue()));
                    calls.add(add(resource.resource, resource.objectNode));
                    resource.status = ResourceStatus.Done;

                    resourcesCreated++;
                }
            }

            if(resourcesCreated == 0) {
                //add associations calls
            }
        }

        resources.stream().
                filter(r -> r.status.equals(ResourceStatus.Create)).
                forEach(r -> calls.add(add(r.resource, r.objectNode)));

        return calls;
    }

    private boolean resolveAssociationCreate(List<Association> associations, Association association) {
        if(association == null) {
            return false;
        }
        if(association.associationType.equals(AssociationType.ManyToOne)) {
            return !association.secondResource.status.equals(ResourceStatus.Create);
        }

        if(association.associationType.equals(AssociationType.OneToMany)) {
            associations.stream().
                    filter(a -> a.secondResource.equals(association.firstResource) && a.secondResource.status.equals(ResourceStatus.Create)).
                    forEach(a -> a.resolved = true);
        }

        return resolveAssociation(association);
    }

    private boolean resolveAssociation(Association association) {
        ObjectNode objectNode = association.firstResource.objectNode;
        String fieldName = association.jsonField.getFirstValue();
        String fieldValue = association.jsonField.getSecondValue();
        AssociationType associationType = association.associationType;

        JsonNode jsonNode = objectNode.get(fieldName);

        switch(associationType) {
            case OneToMany:
            case ManyToMany:
                if(jsonNode != null && jsonNode instanceof ArrayNode) {
                    ArrayNode arrayNode = (ArrayNode)jsonNode;
                    arrayNode.add(fieldValue);
                } else {
                    objectNode.putArray(fieldName).add(fieldValue);
                }

                return true;

            case ManyToOne:
                objectNode.put(fieldName, fieldValue);
                return true;

            case OneToOne:
                //resolve one to one association
                return true;
        }

        return false;
    }

    private List<Call> updateResources(ResourcesAndAssociations resourcesAndAssociations) {
        List<Call> calls = new ArrayList<>();

        List<Resource> resources = resourcesAndAssociations.getResources().stream().
                filter(r -> r.status.equals(ResourceStatus.Update)).
                collect(Collectors.toList());
        List<Association> associations = resourcesAndAssociations.getAssociations();

        for(Resource resource : resources) {
            List<Association> associationsForCurrentResource = associations.stream().
                    filter(a -> a.firstResource.equals(resource)).
                    collect(Collectors.toList());

            associationsForCurrentResource.stream().
                    filter(this::resolveAssociation).
                    forEach(a->calls.add(update((ResourceProxy) resource.resource, resource.objectNode)));
        }

        return calls;
    }

    private String getRepositoryUri(Object resource) {
        Repository repository = repositories.getByResourceClass(resource.getClass()).orElse(null);

        if(repository == null) {
            throw new RestlerException("Can't find repository " + resource.getClass() + ".");
        }

        return baseUri + "/" + RepositoryUtils.getRepositoryPath(repository.getClass().getInterfaces()[0]);
    }

    private String getUri(Object resource) {
        if(resource instanceof ResourceProxy) {
            return ((ResourceProxy) resource).getSelfUri();
        } else {
            Object id = getId(resource);
            if(id == null) {
                throw new RestlerException("Id can't be null.");
            }
            return  getRepositoryUri(resource) + "/" + id;
        }
    }

    private Call add(Object object, ObjectNode node) {
        if(getId(object) == null) {
            return null;
        }

        Object body = getRequestBody(node);
        ImmutableMultimap<String, String> header = ImmutableMultimap.of("Content-Type", "application/json");

        //POST uses for creating new entity
        return new HttpCall(new UriBuilder(getRepositoryUri(object)).build(), HttpMethod.POST, body, header, object.getClass());
    }

    private Call update(ResourceProxy resource, ObjectNode node) {
        Object body = getRequestBody(node);
        ImmutableMultimap<String, String> header = ImmutableMultimap.of("Content-Type", "application/json");

        //PUT uses for replacing values
        return new HttpCall(new UriBuilder(resource.getSelfUri()).build(), HttpMethod.PUT, body, header, resource.getObject().getClass());
    }

    private Object getId(Object object) {

        if(object instanceof ResourceProxy) {
            return ((ResourceProxy) object).getResourceId();
        }

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
}
