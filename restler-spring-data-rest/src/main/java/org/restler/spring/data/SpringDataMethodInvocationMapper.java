package org.restler.spring.data;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.reflect.TypeToken;
import org.restler.client.Call;
import org.restler.client.MethodInvocationMapper;
import org.restler.client.RestlerException;
import org.restler.http.HttpCall;
import org.restler.http.HttpMethod;
import org.restler.spring.data.methods.CrudMethod;
import org.restler.spring.data.methods.FindOneCrudMethod;
import org.restler.spring.data.methods.SaveCrudMethod;
import org.restler.util.UriBuilder;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.*;

public class SpringDataMethodInvocationMapper implements MethodInvocationMapper {

    private static final ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    private final URI baseUrl;

    public SpringDataMethodInvocationMapper(URI baseUrl) {
        this.baseUrl = baseUrl;
    }

    @Override
    public Call map(Object receiver, Method method, Object[] args) {
        Map<String, Object> pathVariables = new HashMap<>();
        ImmutableMultimap.Builder<String, String> requestParams = new ImmutableMultimap.Builder<>();

        Annotation[][] parametersAnnotations = method.getParameterAnnotations();
        String[] parameterNames = parameterNameDiscoverer.getParameterNames(method);

        Set<Object> unmappedArgs = new HashSet<>(Arrays.asList(args));
        for (int pi = 0; pi < parametersAnnotations.length; pi++) {
            for (int ai = 0; ai < parametersAnnotations[pi].length; ai++) {
                Annotation annotation = parametersAnnotations[pi][ai];
                if (annotation instanceof Param) {
                    String pathVariableName = ((Param) annotation).value();
                    if (StringUtils.isEmpty(pathVariableName) && parameterNames != null)
                        pathVariableName = parameterNames[pi];
                    if (StringUtils.isEmpty(pathVariableName))
                        throw new RuntimeException("Name of a path variable can't be resolved during the method " + method + " call");

                    requestParams.put(pathVariableName, args[pi].toString());
                    unmappedArgs.remove(args[pi]);
                }
            }
        }

        return getDescription(receiver.getClass(), method, requestParams.build(), pathVariables, unmappedArgs);
    }

    private Call getDescription(Class<?> declaringClass, Method method, ImmutableMultimap<String, String> requestParams, Map<String, Object> pathVariables, Set<Object> unmappedArgs) {

        RepositoryRestResource repositoryAnnotation = declaringClass.getInterfaces()[0].getDeclaredAnnotation(RepositoryRestResource.class);
        RestResource methodAnnotation = method.getDeclaredAnnotation(RestResource.class);

        String methodMappedUriString;
        HttpMethod httpMethod;
        Object requestBody = null;
        ImmutableMultimap<String, String> header = ImmutableMultimap.of();

        Class repositoryType = (Class) declaringClass.getMethods()[0].getDeclaringClass().getGenericInterfaces()[0];
        ParameterizedTypeImpl crudRepositoryType = (ParameterizedTypeImpl) repositoryType.getGenericInterfaces()[0];
        Class<?> idClass = TypeToken.of(crudRepositoryType.getActualTypeArguments()[1]).getRawType();

        Type genericReturnType;
        if (isCrudMethod(method)) {
            CrudMethod crudMethod = getCrudMethod(method);
            methodMappedUriString = crudMethod.getPathSegment(unmappedArgs.toArray());
            httpMethod = crudMethod.getHttpMethod();
            requestBody = crudMethod.getRequestBody(unmappedArgs.toArray());
            header = crudMethod.getHeader();

            genericReturnType = crudRepositoryType.getActualTypeArguments()[0];
        } else {
            methodMappedUriString = getQueryMethodUri(method, methodAnnotation);
            httpMethod = HttpMethod.GET;
            genericReturnType = method.getGenericReturnType();
        }

        String repositoryUri = getRepositoryUri(declaringClass, repositoryAnnotation);

        String uriTemplate = UriComponentsBuilder.fromUriString("/").pathSegment(repositoryUri, methodMappedUriString).build().toUriString();

        // TODO: implement more generic solution
        unmappedArgs.stream().
                filter(unmappedArg -> idClass.isAssignableFrom(unmappedArg.getClass())).
                forEach(unmappedArg -> pathVariables.put("id", unmappedArg));

        return new HttpCall(url(baseUrl, uriTemplate, requestParams, pathVariables), httpMethod, requestBody, header, genericReturnType);
    }

    private URI url(URI baseUrl, String pathTemplate, ImmutableMultimap<String, String> queryParams, Map<String, Object> pathVariables) {
        return new UriBuilder(baseUrl).
                path(pathTemplate).
                queryParams(queryParams).
                pathVariables(pathVariables).build();
    }

    private String getRepositoryUri(Class<?> repositoryClass, RepositoryRestResource repositoryAnnotation) {

        String repositoryUriString;
        if (repositoryAnnotation == null || repositoryAnnotation.path().isEmpty()) {
            Type entityType = ((ParameterizedType) repositoryClass.getClass().getInterfaces()[0].getGenericInterfaces()[0]).getActualTypeArguments()[0];
            try {
                repositoryUriString = Class.forName(entityType.getTypeName()).getSimpleName().toLowerCase() + "s";
            } catch (ClassNotFoundException e) {
                throw new RestlerException("Could not find class for repository's entity type", e);
            }
        } else {
            repositoryUriString = repositoryAnnotation.path();
        }

        return repositoryUriString;
    }

    private CrudMethod getCrudMethod(Method method) {
        CrudMethod[] crudMethods = {new FindOneCrudMethod(), new SaveCrudMethod()};

        for(CrudMethod crudMethod : crudMethods) {
            if(crudMethod.isCrudMethod(method)) {
                return crudMethod;
            }
        }

        throw new RestlerException("Method " + method + " is not supported");
    }

    private String getQueryMethodUri(Method method, RestResource methodAnnotation) {
        String methodName = method.getName();

        if (methodAnnotation != null && !methodAnnotation.path().isEmpty()) {
            methodName = methodAnnotation.path();
        }

        return "search/" + methodName;
    }

    private boolean isCrudMethod(Method method) {
        Method[] crudMethods = CrudRepository.class.getMethods();

        for (Method crudMethod : crudMethods) {
            if (crudMethod.equals(method)) {
                return true;
            }
        }

        return false;
    }
}
