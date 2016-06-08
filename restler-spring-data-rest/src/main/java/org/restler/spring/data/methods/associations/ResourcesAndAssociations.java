package org.restler.spring.data.methods.associations;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializable;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import org.restler.client.RestlerException;
import org.restler.spring.data.proxy.ResourceProxy;
import org.restler.spring.data.util.Placeholder;
import org.restler.spring.data.util.Repositories;
import org.restler.spring.data.util.ResourceHelper;
import org.restler.util.Pair;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

public class ResourcesAndAssociations {

    //class for creating filter
    @JsonFilter("filter properties by name")
    private class PropertyFilterMixIn {}

    private static final ObjectMapper objectMapper = new ObjectMapper();
    static {
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.addMixIn(Object.class, PropertyFilterMixIn.class);
    }

    private final Repositories repositories;
    private final String baseUri;

    private final List<AssociatedResource> resources = new ArrayList<>();
    private final List<Association> associations = new ArrayList<>();
    private final Map<AssociatedResource, List<Association>> associationsByResource = new HashMap<>();

    public ResourcesAndAssociations(Repositories repositories, String baseUri, Object resource) {
        this.repositories = repositories;
        this.baseUri = baseUri;
        fillResourcesAndAssociations(resource, new HashSet<>());
    }

    public List<AssociatedResource> getResources() {
        return resources;
    }

    public List<Association> getAssociations() {
        return associations;
    }

    public List<Association> getAssociationsByResource(AssociatedResource resource) {
        List<Association> associations = associationsByResource.get(resource);
        if(associations != null) {
            return associations;
        }

        return new ArrayList<>();
    }

    //recursive method, get child resources for some resource and associations between them
    private AssociatedResource fillResourcesAndAssociations(Object object, Set<Object> set) {
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

        AssociatedResourceState resourceState = (objectAtStart instanceof ResourceProxy) ? AssociatedResourceState.Update : AssociatedResourceState.Create;

        AssociatedResource currentResource = new AssociatedResource(objectAtStart, new ObjectNode(JsonNodeFactory.instance), associateFields, resourceState);

        resources.add(currentResource);

        for(Pair<Field, Object> child : children) {
            child.getFirstValue().setAccessible(true);

            if(!set.contains(child.getSecondValue())) {
                if (child.getSecondValue() instanceof Collection) {
                    ((Collection) child.getSecondValue()).stream().filter(item -> !set.contains(item)).forEach(item -> {
                        AssociatedResource childResource = fillResourcesAndAssociations(item, set);
                        List<Association> associateResult = associate(currentResource, child.getFirstValue(), childResource);
                        associateResult.forEach(associations::add);
                        associateResult.forEach(this::addAssociationByResource);
                    });
                } else {
                    AssociatedResource childResource = fillResourcesAndAssociations(child.getSecondValue(), set);
                    List<Association> associateResult = associate(currentResource, child.getFirstValue(), childResource);
                    associateResult.forEach(associations::add);
                    associateResult.forEach(this::addAssociationByResource);
                }
            }

            ignorableFields.add(child.getFirstValue().getName());
            associateFields.add(child.getFirstValue());

            child.getFirstValue().setAccessible(false);
        }

        //filtering associations fields
        FilterProvider filters = new SimpleFilterProvider()
                .addFilter("filter properties by name", SimpleBeanPropertyFilter.serializeAllExcept(ignorableFields.toArray(new String[ignorableFields.size()])));
        ObjectWriter writer = objectMapper.writer(filters);

        try {
            //creates base json body without associations
            ObjectNode node = (ObjectNode) objectMapper.readTree(writer.writeValueAsString(object));
            currentResource.getObjectNode().setAll(node);
        } catch (IOException e) {
            throw new RestlerException("Can't convert object to json", e);
        }

        associations.forEach(a -> a.getSecondResource().addIdPlaceholder(a.getIdPlaceholder()));

        return currentResource;
    }

    //create associations for parent and child resources
    private List<Association> associate(AssociatedResource parent, Field childField, AssociatedResource resource) {

        Object parentResource = parent.getResource();

        if(parentResource instanceof ResourceProxy) {
            parentResource = ((ResourceProxy)parentResource).getObject();
        }

        Object childResource = resource.getResource();

        if(childResource instanceof ResourceProxy) {
            childResource = ((ResourceProxy) childResource).getObject();
        }

        List<Association> result = new ArrayList<>();

        for(Field nullField : resource.getAssociateFields()) {
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

                    Optional<Object> id = Optional.ofNullable(ResourceHelper.getId(parent.getResource()));

                    Placeholder<Object> idPlaceholder = new Placeholder<>(id.orElse("{missing id}").toString());
                    result.add(new Association(resource, parent, new Pair<>(nullField.getName(),
                            ResourceHelper.getUri(repositories, baseUri, parent.getResource(), idPlaceholder)),
                            AssociationType.ManyToOne, idPlaceholder));

                    if(ResourceHelper.getId(resource.getResource()) != null) {
                        result.add(new Association(parent, resource, new Pair<>(childField.getName(), ResourceHelper.getUri(repositories, baseUri, resource.getResource())), AssociationType.OneToMany));
                    }

                    return result;
                }
            }

            if(oneToManyChild != null) {
                if(oneToManyChild.mappedBy().equals(childField.getName()) ||
                        childField.getName().equals(resource.getClass().getSimpleName().toLowerCase())) {

                    if(manyToOneParent != null) {

                        Optional<Object> id = Optional.ofNullable(ResourceHelper.getId(parent.getResource()));

                        Placeholder<Object> idPlaceholder = new Placeholder<>(id.orElse("{missing id}").toString());
                        result.add(new Association(parent, resource, new Pair<>(childField.getName(),
                                ResourceHelper.getUri(repositories, baseUri, resource.getResource(), idPlaceholder)),
                                AssociationType.ManyToOne, idPlaceholder));
                    }

                    if(ResourceHelper.getId(parent.getResource()) != null) {
                        result.add(new Association(resource, parent, new Pair<>(childField.getName(), ResourceHelper.getUri(repositories, baseUri, parent.getResource())), AssociationType.OneToMany));
                    }

                    return result;
                }
            }

            if(manyToManyChild != null) {
                if(manyToManyChild.mappedBy().equals(childField.getName()) ||
                        (childResource.getClass().getSimpleName().toLowerCase() + "s").equals(childField.getName()) ||
                        (manyToManyParent != null && manyToManyParent.mappedBy().equals(nullField.getName())) ||
                        (parentResource.getClass().getSimpleName().toLowerCase() + "s").equals(nullField.getName())) {

                    if(manyToManyParent != null) {

                        Optional<Object> id = Optional.ofNullable(ResourceHelper.getId(parent.getResource()));

                        Placeholder<Object> idPlaceholder = new Placeholder<>(id.orElse("{missing id}").toString());
                        result.add(new Association(parent, resource, new Pair<>(childField.getName(),
                                ResourceHelper.getUri(repositories, baseUri, resource.getResource(), idPlaceholder)),
                                AssociationType.ManyToMany, idPlaceholder));
                    }

                    Optional<Object> id = Optional.ofNullable(ResourceHelper.getId(parent.getResource()));

                    Placeholder<Object> idPlaceholder = new Placeholder<>(id.orElse("{missing id}").toString());
                    result.add(new Association(resource, parent, new Pair<>(nullField.getName(),
                            ResourceHelper.getUri(repositories, baseUri, parent.getResource(), idPlaceholder)),
                            AssociationType.ManyToMany, idPlaceholder));

                    return result;
                }
            }
        }

        throw new RestlerException("Can't make association.");
    }

    private void addAssociationByResource(Association association) {
        List<Association> resourceAssociations = associationsByResource.get(association.getFirstResource());
        if(resourceAssociations != null) {
            resourceAssociations.add(association);
        } else {
            resourceAssociations = new ArrayList<>();
            resourceAssociations.add(association);
            associationsByResource.put(association.getFirstResource(), resourceAssociations);
        }
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
        return value.getClass().isAnnotationPresent(Entity.class) ||
                value instanceof Collection;
    }

    private boolean takeNullFields(List<Field> nullFields, Pair<Field, Object> object) {
        if(object.getSecondValue() == null) {
            nullFields.add(object.getFirstValue());
            return false;
        }
        return true;
    }
}
