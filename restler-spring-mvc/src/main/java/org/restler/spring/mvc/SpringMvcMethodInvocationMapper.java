package org.restler.spring.mvc;

import com.google.common.collect.ImmutableMultimap;
import org.restler.client.Call;
import org.restler.client.MethodInvocationMapper;
import org.restler.client.RestlerException;
import org.restler.http.HttpCall;
import org.restler.http.HttpMethod;
import org.restler.util.UriBuilder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Maps a properly annotated Java method invocation to invocation of a service method.
 */
public class SpringMvcMethodInvocationMapper implements MethodInvocationMapper {

    private static final ParameterNameDiscoverer parameterNameDiscoverer = new ParameterNameDiscoverer();
    private static final Pattern pathVariablesPattern = Pattern.compile("\\{([-a-zA-Z0-9@:%_\\+.~#?&/=]*)\\}");

    private final URI baseUrl;
    private final ParameterResolver paramResolver;

    public SpringMvcMethodInvocationMapper(URI baseUrl, ParameterResolver paramResolver) {
        this.baseUrl = baseUrl;
        this.paramResolver = paramResolver;
    }

    @Override
    public Call map(Object receiver, Method method, Object[] args) {
        boolean receiverResponseBodyAnnotation = AnnotationUtils.isAnnotated(receiver.getClass(), ResponseBody.class);
        boolean methodResponseBodyAnnotation = AnnotationUtils.isAnnotated(method, ResponseBody.class);
        boolean classResponseBodyAnnotation = AnnotationUtils.isAnnotated(method.getDeclaringClass(), ResponseBody.class);
        if (!receiverResponseBodyAnnotation && !methodResponseBodyAnnotation && !classResponseBodyAnnotation) {
            throw new RuntimeException("The method " + method + " does not return response body");
        }

        Object requestBody = null;
        Map<String, Object> pathVariables = new HashMap<>();
        ImmutableMultimap.Builder<String, String> requestParams = new ImmutableMultimap.Builder<>();

        Annotation[][] parametersAnnotations = method.getParameterAnnotations();
        String[] parameterNames = parameterNameDiscoverer.getParameterNames(method);

        InvocationParamResolver resolver = new InvocationParamResolver(method, args, parametersAnnotations, parameterNames, paramResolver);
        for (int pi = 0; pi < parametersAnnotations.length; pi++) {
            for (int ai = 0; ai < parametersAnnotations[pi].length; ai++) {
                Annotation annotation = parametersAnnotations[pi][ai];
                if (annotation instanceof PathVariable) {

                    String pathVariableName = ((PathVariable) annotation).value();
                    if (StringUtils.isEmpty(pathVariableName) && parameterNames != null)
                        pathVariableName = parameterNames[pi];
                    if (StringUtils.isEmpty(pathVariableName))
                        throw new RuntimeException("Name of a path variable can't be resolved during the method " + method + " call");

                    pathVariables.put(pathVariableName, resolver.resolve(pi).orElseGet(() -> null));

                } else if (annotation instanceof RequestParam) {

                    String parameterVariableName;
                    if (!StringUtils.isEmpty(((RequestParam) annotation).value())) {
                        parameterVariableName = ((RequestParam) annotation).value();
                    } else if (parameterNames != null && parameterNames[pi] != null) {
                        parameterVariableName = parameterNames[pi];
                    } else {
                        throw new RuntimeException("Name of a request parameter can't be resolved during the method " + method + " call");
                    }

                    resolver.resolve(pi).
                            ifPresent(param -> requestParams.put(parameterVariableName, param));

                } else if (annotation instanceof RequestBody) {
                    requestBody = args[pi];
                }
            }
        }

        RequestMapping controllerMapping = AnnotationUtils.getAnnotation(receiver.getClass(), RequestMapping.class);
        RequestMapping methodMapping = method.getDeclaredAnnotation(RequestMapping.class);
        if (methodMapping == null) {
            throw new RuntimeException("The method " + method + " is not mapped");
        }

        String pathTemplate = pathTemplate(controllerMapping, methodMapping);
        List<String> unboundPathVariables = unusedPathVariables(pathVariables, pathTemplate);
        if (unboundPathVariables.size() > 0) {
            throw new RestlerException("You should introduce method parameter with @PathVariable annotation for each url template variable. Unbound variables: " + unboundPathVariables);
        }

        ImmutableMultimap<String, String> headers = ImmutableMultimap.of();

        if(Arrays.stream(args).filter(o -> o instanceof MultipartFile).count() > 0) {
            String boundary = "--------Asrf456BGe4h";
            String multipartBody = "";

            for (int i = 0; i < args.length; ++i) {
                if (args[i] instanceof MultipartFile) {
                    RequestParam requestParam = findAnnotation(parametersAnnotations[i], RequestParam.class);
                    if (requestParam != null) {
                        multipartBody += partBodyForMultipart(requestParam.value(), (MultipartFile) args[i], boundary);
                    }
                }
            }

            multipartBody += "--" + boundary + "--\r\n";

            headers = ImmutableMultimap.of("Content-Type", "multipart/form-data; boundary=" + boundary);
            requestBody = multipartBody;
        }

        URI url = url(baseUrl, pathTemplate, requestParams.build(), pathVariables);

        return new HttpCall(url, getHttpMethod(methodMapping), requestBody, headers, getReturnType(method));
    }

    private <T extends Annotation> T findAnnotation(Annotation[] annotations, Class<T> annotation) {
        return Arrays.stream(annotations).
                filter(a -> a.annotationType().equals(annotation)).
                map(a -> (T)a).
                findFirst().
                orElse(null);
    }

    private String partBodyForMultipart(String name, MultipartFile multipartFile, String boundary) {
        String resultBody = "--" + boundary + "\r\n";

        resultBody += "Content-Disposition: form-data; name=\"" + name + "\"; filename=\"" + multipartFile.getOriginalFilename() + "\"\r\n";
        resultBody += "Content-Type: " + multipartFile.getContentType() + "\r\n\r\n";

        byte[] fileData;
        try {
            fileData = multipartFile.getBytes();
        } catch (IOException e) {
            throw new RestlerException("Can't get bytes from multipart file.", e);
        }

        resultBody += new String(fileData) + "\r\n";

        return resultBody;
    }

    private String pathTemplate(RequestMapping controllerMapping, RequestMapping methodMapping) {
        String controllerPath = getMappedUriString(controllerMapping);
        String methodPath = getMappedUriString(methodMapping);
        if (!controllerPath.startsWith("/")) {
            controllerPath = "/" + controllerPath;
        }
        if (controllerPath.endsWith("/") && methodPath.startsWith("/")) {
            controllerPath = controllerPath.substring(0, controllerPath.length() - 1);
        }
        return controllerPath + methodPath;
    }

    private HttpMethod getHttpMethod(RequestMapping methodMapping) {
        RequestMethod declaredMethod;
        if (methodMapping.method() == null || methodMapping.method().length == 0) {
            declaredMethod = RequestMethod.GET;
        } else {
            declaredMethod = methodMapping.method()[0];
        }
        return HttpMethod.valueOf(declaredMethod.toString());
    }

    private Type getReturnType(Method method) {
        return method.getGenericReturnType();
    }

    private URI url(URI baseUrl, String pathTemplate, ImmutableMultimap<String, String> queryParams, Map<String, Object> pathVariables) {
        return new UriBuilder(baseUrl).
                path(pathTemplate).
                queryParams(queryParams).
                pathVariables(pathVariables).build();
    }

    private List<String> unusedPathVariables(Map<String, Object> pathVariables, String uriTemplate) {
        List<String> res = new ArrayList<>();
        Matcher matcher = pathVariablesPattern.matcher(uriTemplate);
        while (matcher.find()) {
            if (!pathVariables.containsKey(matcher.group(1))) {
                res.add(matcher.group(1));
            }
        }
        return res;
    }

    private String getMappedUriString(RequestMapping mapping) {
        if (mapping == null) {
            return "";
        } else {
            String uriString = getFirstOrEmpty(mapping.value());
            return uriString.startsWith("/") ?  uriString : "/" + uriString;
        }
    }

    private String getFirstOrEmpty(String[] strings) {
        if (strings == null || strings.length == 0) {
            return "";
        } else {
            return strings[0];
        }
    }

    private class InvocationParamResolver {

        private final Method method;
        private final Object[] args;
        private final Annotation[][] annotations;
        private final String[] paramNames;

        private final ParameterResolver paramResolver;

        public InvocationParamResolver(Method method, Object[] args, Annotation[][] annotations, String[] paramNames, ParameterResolver paramResolver) {
            this.paramResolver = paramResolver;
            this.paramNames = paramNames;
            this.annotations = annotations;
            this.args = args;
            this.method = method;
        }

        public Optional<String> resolve(int paramIdx) {
            if(args[paramIdx] instanceof MultipartFile) {
                return Optional.empty();
            }
            return paramResolver.resolve(method, args, annotations, paramNames, paramIdx);
        }
    }
}
