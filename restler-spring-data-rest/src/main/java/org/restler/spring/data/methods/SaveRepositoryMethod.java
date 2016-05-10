package org.restler.spring.data.methods;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableMultimap;
import org.restler.client.Call;
import org.restler.client.RestlerException;
import org.restler.http.HttpCall;
import org.restler.http.HttpMethod;
import org.restler.spring.data.calls.ChainCall;
import org.restler.spring.data.methods.associations.*;
import org.restler.spring.data.proxy.ResourceProxy;
import org.restler.spring.data.util.Repositories;
import org.restler.spring.data.util.ResourceHelper;
import org.restler.util.UriBuilder;
import org.springframework.data.repository.CrudRepository;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
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

        ResourcesAndAssociations resourcesAndAssociations = new ResourcesAndAssociations(repositories, baseUri, resource);
        createResources(resourcesAndAssociations).forEach(calls::add);
        updateResources(resourcesAndAssociations).forEach(calls::add);

        Class<?> returnType = (resource instanceof ResourceProxy) ? ((ResourceProxy) resource).getObject().getClass() : resource.getClass();

        return new ChainCall((Object prevResult, Object object) ->
                takeResult(prevResult, object, ResourceHelper.getId(resource), returnType), calls, returnType);
    }

    @Override
    public String getPathPart(Object[] args) {
        Object arg = args[0];
        if(arg instanceof ResourceProxy) {
            ResourceProxy resourceProxy = (ResourceProxy)arg;

            return resourceProxy.getResourceId().toString();
        }

        return ResourceHelper.getId(arg).toString();
    }

    //need for filtering results are returned by associate call
    private Object takeResult(Object prevResult, Object object, Object id, Class<?> type) {
        if(object != null && object instanceof ResourceProxy &&
                ((ResourceProxy) object).getObject().getClass().equals(type) && ResourceHelper.getId(object).equals(id)) {
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

    private List<Call> createResources(ResourcesAndAssociations resourcesAndAssociations) {
        List<Call> calls = new ArrayList<>();
        List<Call> associateCalls = new ArrayList<>();

        List<AssociatedResource> resources = resourcesAndAssociations.getResources().stream().
                filter(r -> r.getState().equals(AssociatedResourceState.Create)).
                collect(Collectors.toList());
        List<Association> associations = resourcesAndAssociations.getAssociations();


        while(resources.size() > 0 && associations.stream().
                filter(a->a.getFirstResource().getState().equals(AssociatedResourceState.Create)
                && !a.isResolved()).count() > 0) {
            long resourcesCreated = 0;

            for (AssociatedResource resource : resources) {
                List<Association> associationsForCurrentResource = resourcesAndAssociations.getAssociationsByResource(resource);

                associationsForCurrentResource.stream().
                        forEach(a -> resolveAssociationCreate(associations, a));

                List<Association> resolvedManyToOne = associationsForCurrentResource.stream().
                        filter(a -> a.getAssociationType().equals(AssociationType.ManyToOne) && a.isResolved()).
                        collect(Collectors.toList());

                long manyToOneCount =
                        associationsForCurrentResource.stream().
                                filter(a -> a.getAssociationType().equals(AssociationType.ManyToOne) &&
                                        a.getSecondResource().getState().equals(AssociatedResourceState.Create) && !a.isResolved()).
                                count();

                if (manyToOneCount == 0) {
                    resolvedManyToOne.forEach(a -> resource.getObjectNode().put(a.getJsonField().getFirstValue(), a.getJsonField().getSecondValue()));
                    calls.add(add(resource.getResource(), resource.getObjectNode()));
                    resource.changeState(AssociatedResourceState.Done);
                    resourcesCreated++;
                }
            }

            //if cant resolve associations and create resources
            if(resourcesCreated == 0) {
                Association firstManyToOneAssociation = associations.stream().
                        filter(a->!a.isResolved()&&a.getAssociationType().equals(AssociationType.ManyToOne)).
                        findFirst().orElse(null);

                if(firstManyToOneAssociation != null) {
                    URI uri = new UriBuilder(ResourceHelper.getUri(repositories, baseUri,
                            firstManyToOneAssociation.getFirstResource())+"/"+firstManyToOneAssociation.getJsonField().getFirstValue()).build();
                    ImmutableMultimap<String, String> header = ImmutableMultimap.of("Content-Type", "text/uri-list");

                    /**
                     * The call creates request for associating parent and child.
                     * PUT uses for adding new associations between resources
                     * {@link http://docs.spring.io/spring-data/rest/docs/current/reference/html/#_put_2}
                     * */
                    associateCalls.add(new HttpCall(uri, HttpMethod.PUT,
                            firstManyToOneAssociation.getJsonField().getSecondValue(), header, void.class));

                    firstManyToOneAssociation.markAsResolved();
                } else {
                    throw new RestlerException("Can't resolve associations.");
                }
            }
        }

        resources.stream().
                filter(r -> r.getState().equals(AssociatedResourceState.Create)).
                forEach(r -> calls.add(add(r.getResource(), r.getObjectNode())));

        associateCalls.forEach(calls::add);

        return calls;
    }

    private boolean resolveAssociationCreate(List<Association> associations, Association association) {
        if(association == null) {
            return false;
        }
        if(association.getAssociationType().equals(AssociationType.ManyToOne)) {
            boolean resolved = !association.getSecondResource().getState().equals(AssociatedResourceState.Create);
            if(resolved) {
                association.markAsResolved();
            }
            return resolved;
        }

        if(association.getAssociationType().equals(AssociationType.OneToMany)) {
            associations.stream().
                    filter(a -> a.getSecondResource().equals(association.getFirstResource()) &&
                            a.getSecondResource().getState().equals(AssociatedResourceState.Create)).
                    forEach(Association::markAsResolved);
        }

        return resolveAssociation(association);
    }

    private boolean resolveAssociation(Association association) {
        ObjectNode objectNode = association.getFirstResource().getObjectNode();
        String fieldName = association.getJsonField().getFirstValue();
        String fieldValue = association.getJsonField().getSecondValue();
        AssociationType associationType = association.getAssociationType();

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

                association.markAsResolved();
                return true;

            case ManyToOne:
                objectNode.put(fieldName, fieldValue);
                association.markAsResolved();
                return true;

            case OneToOne:
                //resolve one to one association
                return true;
        }

        return false;
    }

    private List<Call> updateResources(ResourcesAndAssociations resourcesAndAssociations) {
        List<Call> calls = new ArrayList<>();

        List<AssociatedResource> resources = resourcesAndAssociations.getResources().stream().
                filter(r -> r.getState().equals(AssociatedResourceState.Update)).
                collect(Collectors.toList());
        List<Association> associations = resourcesAndAssociations.getAssociations();

        for(AssociatedResource resource : resources) {
            List<Association> associationsForCurrentResource = associations.stream().
                    filter(a -> a.getFirstResource().equals(resource)).
                    collect(Collectors.toList());

            associationsForCurrentResource.stream().
                    forEach(this::resolveAssociation);

            calls.add(update((ResourceProxy) resource.getResource(), resource.getObjectNode()));
            resource.changeState(AssociatedResourceState.Done);
        }

        return calls;
    }

    private Call add(Object object, ObjectNode node) {
        if(ResourceHelper.getId(object) == null) {
            return null;
        }

        Object body = getRequestBody(node);
        ImmutableMultimap<String, String> header = ImmutableMultimap.of("Content-Type", "application/json");

        //POST uses for creating new entity
        return new HttpCall(new UriBuilder(ResourceHelper.getRepositoryUri(repositories, baseUri, object)).build(),
                HttpMethod.POST, body, header, object.getClass());
    }

    private Call update(ResourceProxy resource, ObjectNode node) {
        Object body = getRequestBody(node);
        ImmutableMultimap<String, String> header = ImmutableMultimap.of("Content-Type", "application/json");

        //PUT uses for replacing values
        return new HttpCall(new UriBuilder(resource.getSelfUri()).build(), HttpMethod.PUT, body,
                header, resource.getObject().getClass());
    }
}
