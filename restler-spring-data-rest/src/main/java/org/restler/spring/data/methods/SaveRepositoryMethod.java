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
import org.restler.spring.data.util.Placeholder;
import org.restler.spring.data.util.Repositories;
import org.restler.spring.data.util.ResourceHelper;
import org.restler.util.UriBuilder;
import org.springframework.data.repository.CrudRepository;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

/**
 * CrudRepository save method implementation.
 */
public class SaveRepositoryMethod extends DefaultRepositoryMethod {

    private static final Method saveMethod;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        try {
            saveMethod = CrudRepository.class.getMethod("save", Object.class);
        } catch (NoSuchMethodException e) {
            throw new RestlerException("Can't find CrudRepository.save method.", e);
        }
    }

    private final String baseUri;
    private final Repositories repositories;

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

        SaveCalls saveCalls = new SaveCalls(resource);

        Class<?> returnType = (resource instanceof ResourceProxy) ? ((ResourceProxy) resource).getObject().getClass() : resource.getClass();

        return new ChainCall(new ResultAnalyzer(saveCalls.getCurrentCallNumber(), saveCalls.getCallNumberToPlaceholder()), saveCalls.getCalls(), returnType);
    }

    @Override
    public String getPathPart(Object[] args) {
        Object arg = args[0];
        if(arg instanceof ResourceProxy) {
            ResourceProxy resourceProxy = (ResourceProxy)arg;

            return resourceProxy.getResourceId().toString();
        }

        return "";
    }

    private class ResultAnalyzer implements BinaryOperator<Object> {

        private long targetCallNumber;
        private final Map<Integer, List<Placeholder<Object>>> callNumberToPlaceholder;
        private int currentCallNumber = 0;

        public ResultAnalyzer(long targetCallNumber, Map<Integer, List<Placeholder<Object>>> callNumberToPlaceholder) {
            this.targetCallNumber = targetCallNumber;
            this.callNumberToPlaceholder = callNumberToPlaceholder;
        }

        @Override
        public Object apply(Object prevResult, Object result) {
            Object finalResult;

            List<Placeholder<Object>> placeholders = callNumberToPlaceholder.get(currentCallNumber);

            if(placeholders != null) {
                placeholders.forEach(p -> {
                    if(p != null) {
                        p.setValue(ResourceHelper.getId(result));
                    }
                });
            }

            if(currentCallNumber == targetCallNumber) {
                finalResult = result;
            } else {
                finalResult = prevResult;
            }

            currentCallNumber++;

            return finalResult;
        }
    }

    private LazyBody getRequestBody(ObjectNode node) {
        return new LazyBody(node);
    }

    private static class LazyBody {

        private final ObjectNode objectNode;

        public LazyBody(ObjectNode objectNode) {
            this.objectNode = objectNode;
        }

        @Override
        public String toString() {
            String json;
            try {
                json = objectMapper.writeValueAsString(objectNode);
            } catch (JsonProcessingException e) {
                throw new RestlerException("Can't create json from object", e);
            }

            return json;
        }
    }

    static public class LazyCall implements Call {
        private final URI url;
        private final HttpMethod method;
        private final LazyBody requestBody;
        private final ImmutableMultimap<String, String> headers;
        private final Type returnType;

        public LazyCall(URI url, HttpMethod method, LazyBody requestBody,
                        ImmutableMultimap<String, String> headers, Type returnType) {
            this.url = url;
            this.method = method;
            this.requestBody = requestBody;
            this.headers = headers;
            this.returnType = returnType;
        }

        public LazyCall(URI url, HttpMethod method, LazyBody requestBody) {
            this(url, method, requestBody, ImmutableMultimap.of(), Object.class);
        }

        @Override
        public Type getReturnType() {
            return returnType;
        }

        @Override
        public Call withReturnType(Type type) {
            return new LazyCall(url, method, requestBody, headers, type);
        }

        public HttpCall getCall() {
            return new HttpCall(url, method, requestBody.toString(), headers, returnType);
        }
    }


    private class SaveCalls {
        private final Object currentResource;
        private final List<Call> calls = new ArrayList<>();
        private final Map<Integer, List<Placeholder<Object>>> callNumberToPlaceholder = new HashMap<>();
        private Integer currentCallNumber;

        public SaveCalls(Object resource) {
            currentResource = resource;

            ResourcesAndAssociations resourcesAndAssociations = new ResourcesAndAssociations(repositories, baseUri, resource);
            createResources(resourcesAndAssociations).forEach(calls::add);
            updateResources(resourcesAndAssociations).forEach(calls::add);
        }

        public List<Call> getCalls() {
            return calls;
        }

        public Map<Integer, List<Placeholder<Object>>> getCallNumberToPlaceholder() {
            return callNumberToPlaceholder;
        }

        public Integer getCurrentCallNumber() {
            return currentCallNumber;
        }


        //create calls for creating resources
        private List<Call> createResources(ResourcesAndAssociations resourcesAndAssociations) {
            List<Call> calls = new ArrayList<>();
            List<Call> associateCalls = new ArrayList<>();

            //get resources that must be created
            List<AssociatedResource> resources = resourcesAndAssociations.getResources().stream().
                    filter(r -> r.getState().equals(AssociatedResourceState.Create)).
                    collect(Collectors.toList());

            //get all associations
            List<Association> associations = resourcesAndAssociations.getAssociations();

            Integer callNumber = callNumberToPlaceholder.size();


            //while unresolved associations for creating resources not empty
            while(!resources.isEmpty() && associations.stream().
                    filter(a->a.getFirstResource().getState().equals(AssociatedResourceState.Create)
                            && !a.isResolved()).count() > 0) {
                long resourcesCreated = 0;

                for (AssociatedResource resource : resources) {
                    List<Association> associationsForCurrentResource = resourcesAndAssociations.getAssociationsByResource(resource);

                    //resolve associations for current resource
                    associationsForCurrentResource.stream().
                            forEach(this::resolveAssociationCreate);

                    //get resolved manyToOne associations
                    List<Association> resolvedManyToOne = associationsForCurrentResource.stream().
                            filter(a -> a.getAssociationType().equals(AssociationType.ManyToOne) && a.isResolved()).
                            collect(Collectors.toList());

                    //get count of manyToOne unresolved associations
                    long manyToOneCount =
                            associationsForCurrentResource.stream().
                                    filter(a -> a.getAssociationType().equals(AssociationType.ManyToOne) &&
                                            a.getSecondResource().getState().equals(AssociatedResourceState.Create) && !a.isResolved()).
                                    count();

                    //if count of manyToOne unresolved associations is zero then add call for creating current resource
                    if (manyToOneCount == 0) {
                        resolvedManyToOne.forEach(a -> resource.getObjectNode().putPOJO(a.getJsonField().getFirstValue(), a.getJsonField().getSecondValue()));
                        calls.add(add(resource.getResource(), resource.getObjectNode()));

                        if(resource.getResource() == currentResource) {
                            currentCallNumber = callNumber;
                        }

                        callNumberToPlaceholder.put(callNumber++, resource.getIdPlaceholders());

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
                                firstManyToOneAssociation.getJsonField().getSecondValue().toString(), header, void.class));

                        firstManyToOneAssociation.markAsResolved();
                    } else {
                        throw new RestlerException("Can't resolve associations.");
                    }
                }

                resources = resources.stream().
                        filter(r -> r.getState().equals(AssociatedResourceState.Create)).
                        collect(Collectors.toList());
            }


            class IntegerHandler {
                private Integer value;
            }

            IntegerHandler callNumberHandler = new IntegerHandler();
            callNumberHandler.value = callNumber;

            //if resources have not created yet
            resources.stream().
                    filter(r -> r.getState().equals(AssociatedResourceState.Create)).
                    forEach(r -> {
                                calls.add(add(r.getResource(), r.getObjectNode()));
                                r.changeState(AssociatedResourceState.Done);
                                if(r.getResource() == currentResource) {
                                    currentCallNumber = callNumberHandler.value;
                                }
                                callNumberToPlaceholder.put(callNumberHandler.value++, r.getIdPlaceholders());
                            }
                    );

            callNumber = callNumberHandler.value;

            associateCalls.forEach(calls::add);

            for(int i = 0; i < associateCalls.size(); ++i) {
                callNumberToPlaceholder.put(callNumber++, null);
            }

            return calls;
        }

        //resolve association for creating resource
        private boolean resolveAssociationCreate(Association association) {
            if(association == null) {
                return false;
            }

            //if second resource of association manyToOne is created then association cant be resolved yet
            if(association.getAssociationType().equals(AssociationType.ManyToOne)) {
                boolean resolved = !association.getSecondResource().getState().equals(AssociatedResourceState.Create);
                if(resolved) {
                    association.markAsResolved();
                }
                return resolved;
            }

            return resolveAssociation(association);
        }

        //resolve associations
        private boolean resolveAssociation(Association association) {
            ObjectNode objectNode = association.getFirstResource().getObjectNode();
            String fieldName = association.getJsonField().getFirstValue();
            Object fieldValue = association.getJsonField().getSecondValue();
            AssociationType associationType = association.getAssociationType();

            JsonNode jsonNode = objectNode.get(fieldName);

            switch(associationType) {
                case OneToMany:
                case ManyToMany:
                    //add to json associations
                    if(jsonNode != null && jsonNode instanceof ArrayNode) {
                        ArrayNode arrayNode = (ArrayNode)jsonNode;
                        arrayNode.addPOJO(fieldValue);
                    } else {
                        objectNode.putArray(fieldName).addPOJO(fieldValue);
                    }

                    association.markAsResolved();
                    return true;

                case ManyToOne:
                    //add to json association
                    objectNode.putPOJO(fieldName, fieldValue);

                    association.markAsResolved();
                    return true;

                case OneToOne:
                    throw new RestlerException("Unsupported association oneToOne.");
            }

            return false;
        }

        //create calls for updating resources
        private List<Call> updateResources(ResourcesAndAssociations resourcesAndAssociations) {
            List<Call> calls = new ArrayList<>();

            //get resources that must be created
            List<AssociatedResource> resources = resourcesAndAssociations.getResources().stream().
                    filter(r -> r.getState().equals(AssociatedResourceState.Update)).
                    collect(Collectors.toList());

            Integer callNumber = callNumberToPlaceholder.size();

            for(AssociatedResource resource : resources) {
                List<Association> associationsForCurrentResource = resourcesAndAssociations.getAssociationsByResource(resource);

                //resolve associations for updating resource
                associationsForCurrentResource.stream().
                        forEach(this::resolveAssociation);

                calls.add(update((ResourceProxy) resource.getResource(), resource.getObjectNode()));

                if(resource.getResource() == currentResource) {
                    currentCallNumber = callNumber;
                }

                callNumberToPlaceholder.put(callNumber++, resource.getIdPlaceholders());

                resource.changeState(AssociatedResourceState.Done);
            }

            return calls;
        }

        //create call for adding new resource to repository
        private Call add(Object object, ObjectNode node) {
            LazyBody body = getRequestBody(node);
            ImmutableMultimap<String, String> header = ImmutableMultimap.of("Content-Type", "application/json");

            //POST uses for creating new entity
            return new LazyCall(new UriBuilder(ResourceHelper.getRepositoryUri(repositories, baseUri, object)).build(),
                    HttpMethod.POST, body, header, object.getClass());
        }

        //create call for updating resource in repository
        private Call update(ResourceProxy resource, ObjectNode node) {
            LazyBody body = getRequestBody(node);
            ImmutableMultimap<String, String> header = ImmutableMultimap.of("Content-Type", "application/json");

            //PUT uses for replacing values
            return new LazyCall(new UriBuilder(resource.getSelfUri()).build(), HttpMethod.PUT, body,
                    header, resource.getObject().getClass());
        }

    }


}
