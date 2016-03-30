package org.restler.spring.data;

import com.google.common.collect.ImmutableMultimap;
import org.restler.client.Call;
import org.restler.client.MethodInvocationMapper;
import org.restler.client.RestlerException;
import org.restler.spring.data.methods.*;
import org.restler.spring.data.util.Repositories;
import org.restler.spring.data.util.RepositoryUtils;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.data.repository.query.Param;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.*;

public class SpringDataMethodInvocationMapper implements MethodInvocationMapper {

    private static final ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    private final URI baseUrl;

    private final Repositories repositories;

    public SpringDataMethodInvocationMapper(URI baseUrl, Repositories repositories) {
        this.baseUrl = baseUrl;
        this.repositories = repositories;
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
        String repositoryUri = RepositoryUtils.getRepositoryPath(declaringClass.getInterfaces()[0]);

        RepositoryMethod repositoryMethod = getRepositoryMethod(method, baseUrl + "/" + repositoryUri);

        return repositoryMethod.getDescription(baseUrl, declaringClass, requestParams, pathVariables, unmappedArgs);
    }

    private RepositoryMethod getRepositoryMethod(Method method, String repositoryUri) {
        RepositoryMethod[] repositoryMethods = {
                new FindOneRepositoryMethod(),
                new SaveRepositoryMethod(baseUrl.toString(), repositoryUri, repositories),
                new SaveSeveralRepositoryMethod(repositories),
                new DeleteRepositoryMethod(),
                new QueryRepositoryMethod(method),
                new FindAllRepositoryMethod(),
                new FindAllByIdRepositoryMethod(),
                new DeleteAllRepositoryMethod(repositories),
                new FindAllSortingRepositoryMethod(),
                new FindAllPageableRepositoryMethod()
        };

        for(RepositoryMethod repositoryMethod : repositoryMethods) {
            if(repositoryMethod.isRepositoryMethod(method)) {
                return repositoryMethod;
            }
        }

        throw new RestlerException("Method " + method + " is not supported");
    }


}
