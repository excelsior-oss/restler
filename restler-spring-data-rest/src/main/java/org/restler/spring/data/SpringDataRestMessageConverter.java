package org.restler.spring.data;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Enhancer;
import org.objenesis.ObjenesisStd;
import org.restler.RestlerConfig;
import org.restler.client.CallExecutor;
import org.restler.client.RestlerException;
import org.restler.spring.data.proxy.Resource;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.GenericHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import javax.persistence.EmbeddedId;
import javax.persistence.Id;
import java.io.IOException;
import java.lang.reflect.*;
import java.util.*;

class SpringDataRestMessageConverter implements GenericHttpMessageConverter<Object> {
    private final ObjenesisStd objenesis = new ObjenesisStd();

    @Override
    public boolean canRead(Type type, Class<?> aClass, MediaType mediaType) {
        if (!(type instanceof ParameterizedType)) {
            return false;
        }
        Class<?> resultClass = ((ParameterizedTypeImpl) type).getRawType();
        return isList(resultClass);
    }

    @Override
    public Object read(Type type, Class<?> aClass, HttpInputMessage httpInputMessage) throws IOException, HttpMessageNotReadableException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        JsonNode rootNode = objectMapper.readTree(httpInputMessage.getBody());
        Class<?> resultClass = ((ParameterizedTypeImpl) type).getRawType();
        Class<?> elementClass;
        try {
            elementClass = Class.forName(((ParameterizedTypeImpl) type).getActualTypeArguments()[0].getTypeName());
        } catch (ClassNotFoundException e) {
            throw new RestlerException(e);
        }
        if (isList(resultClass)) {
            String containerName = elementClass.getSimpleName().toLowerCase() + "s";
            JsonNode objects = rootNode.get("_embedded").get(containerName);
            if (objects instanceof ArrayNode) {
                ArrayNode arr = ((ArrayNode) objects);
                List<Object> res = new ArrayList<>();
                for (int i = 0; i < arr.size(); i++) {
                    HashMap<String, String> hrefs = getObjectHrefs(arr.get(i));
                    Object object = mapObject(elementClass, objectMapper, arr.get(i));
                    res.add(makeProxy(elementClass, object, hrefs));
                }
                return res;
            }
        }
        throw new HttpMessageNotReadableException("Unexpected response format");
    }

    @Override
    public boolean canRead(Class<?> aClass, MediaType mediaType) {
        return true;
    }

    @Override
    public boolean canWrite(Class<?> aClass, MediaType mediaType) {
        return false;
    }

    @Override
    public List<MediaType> getSupportedMediaTypes() {
        List<MediaType> supportedMediaTypes = new ArrayList<>();
        supportedMediaTypes.add(MediaType.APPLICATION_JSON);
        supportedMediaTypes.add(MediaType.parseMediaType("application/x-spring-data-verbose+json"));
        return supportedMediaTypes;
    }

    @Override
    public Object read(Class<?> aClass, HttpInputMessage httpInputMessage) throws IOException, HttpMessageNotReadableException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        JsonNode rootNode = objectMapper.readTree(httpInputMessage.getBody());

        HashMap<String, String> hrefs = getObjectHrefs(rootNode);

        Object object = mapObject(aClass, objectMapper, rootNode);

        return makeProxy(aClass, object, hrefs);
    }

    private Object makeProxy(Class<?> aClass, Object object, HashMap<String, String> hrefs) {
        class ProxyObjectData {
            public CallExecutor executor = null;
            public RestlerConfig config = null;
        }

        ProxyObjectData proxyObjectData = new ProxyObjectData();

        net.sf.cglib.proxy.InvocationHandler handler = (Object o, Method method, Object[] args)->{

            if(method.equals(Resource.class.getMethod("getResourceId"))) {
                String self = hrefs.get("self");
                return getWrappedId(object, self.substring(self.lastIndexOf("/")+1));
            }else if(method.equals(Resource.class.getMethod("getRepositoryUri"))) {
                String self = hrefs.get("self");
                return self.substring(0, self.lastIndexOf("/"));
            }else if(method.equals(Resource.class.getMethod("getSelfUri"))) {
                return hrefs.get("self");
            }else if(method.equals(Resource.class.getMethod("getCallExecutor"))) {
                return proxyObjectData.executor;
            }else if(method.equals(Resource.class.getMethod("getObject"))) {
                return object;
            }else if(method.equals(Resource.class.getMethod("getHrefs"))) {
                return hrefs;
            }else if(method.equals(Resource.class.getMethod("getRestlerConfig"))) {
                return proxyObjectData.config;
            }else if(method.equals(Resource.class.getMethod("setExecutor", CallExecutor.class))) {
                proxyObjectData.executor = (CallExecutor) args[0];
                return null;
            } else if(method.equals(Resource.class.getMethod("setRestlerConfig", RestlerConfig.class))) {
                proxyObjectData.config = (RestlerConfig)args[0];
                return null;
            }

            /*if(proxyObjectData.executor != null) {
                String uri = getHrefByMethod(method, hrefs);

                if(uri != null) {
                    Call httpCall = new HttpCall(new UriBuilder(uri).build(), HttpMethod.GET, null, ImmutableMultimap.of(), method.getGenericReturnType());

                    return proxyObjectData.executor.execute(httpCall);
                }
            }*/

            return method.invoke(object, args);
        };

        Enhancer enhancer = new Enhancer();
        enhancer.setUseCache(false);
        enhancer.setSuperclass(aClass);
        enhancer.setInterfaces(new Class<?>[]{Resource.class});
        enhancer.setCallbackType(handler.getClass());

        Class proxyClass = enhancer.createClass();
        Enhancer.registerCallbacks(proxyClass, new Callback[] { handler });

        Object proxy = objenesis.newInstance(proxyClass);

        try {
            for(Field objectField : aClass.getDeclaredFields()) {
                objectField.setAccessible(true);
                objectField.set(proxy, objectField.get(object));
                objectField.setAccessible(false);
            }
        } catch (IllegalAccessException e) {
            throw new RestlerException("Illegal access to field.", e);
        }

        return proxy;
    }

    private Object mapObject(Class<?> aClass, ObjectMapper objectMapper, JsonNode rootNode) throws com.fasterxml.jackson.core.JsonProcessingException {
        Object entity = objectMapper.treeToValue(rootNode, aClass);
        setId(entity, aClass, getId(rootNode));
        return entity;
    }

    @Override
    public void write(Object o, MediaType mediaType, HttpOutputMessage httpOutputMessage) throws IOException, HttpMessageNotWritableException {

    }

    private HashMap<String, String> getObjectHrefs(JsonNode objectNode) {
        HashMap<String, String> result = new LinkedHashMap<>();

        JsonNode linksNode = objectNode.get("_links");
        Iterator<String> names = linksNode.fieldNames();

        linksNode.forEach((JsonNode node)->{
            String name = names.next();
            result.put(name, node.get("href").toString().replace("\"", ""));
        });

       return result;
    }

    private String getHrefByMethod(Method method, HashMap<String, String> hrefs) {
        String methodName = method.getName();

        if(methodName.startsWith("get")) {
            String hrefName = methodName.substring(3).toLowerCase();
            return hrefs.get(hrefName);
        }

        return null;
    }

    private Object getId(JsonNode objectNode) {
        String links = "_links";
        JsonNode linksNode = objectNode.get(links);
        String self = "self";
        JsonNode selfLink = linksNode.get(self);

        String selfLinkString = selfLink.toString();

        int leftOffset = selfLinkString.lastIndexOf("/") + 1;
        int rightOffset = selfLinkString.indexOf('"', leftOffset);
        return selfLinkString.substring(leftOffset, rightOffset);
    }

    private Object getWrappedId(Object object, String id) {
        Field[] fields = object.getClass().getDeclaredFields();
        Class fieldClass;

        for (Field field : fields) {
            if (field.getDeclaredAnnotation(Id.class) != null || field.getDeclaredAnnotation(EmbeddedId.class) != null) {
                fieldClass = field.getType();

                field.setAccessible(true);

                try {
                    return fieldClass.getConstructor(String.class).newInstance(id);
                } catch (IllegalAccessException e) {
                    throw new RestlerException("Access denied to change id", e);
                } catch (InvocationTargetException e) {
                    throw new RestlerException("Can't create id wrapper", e);
                } catch (NoSuchMethodException | InstantiationException e) {
                    throw new RestlerException("Could not instantiate id object", e);
                }
            }
        }

        return null;
    }

    private void setId(Object object, Class<?> aClass, Object id) {
        Field[] fields = aClass.getDeclaredFields();
        Class fieldClass;

        for (Field field : fields) {
            if (field.getDeclaredAnnotation(Id.class) != null || field.getDeclaredAnnotation(EmbeddedId.class) != null) {
                fieldClass = field.getType();

                field.setAccessible(true);

                try {
                    Object wrappedId = fieldClass.getConstructor(String.class).newInstance(id);
                    field.set(object, wrappedId);
                    field.setAccessible(false);
                } catch (IllegalAccessException e) {
                    throw new RestlerException("Access denied to change id", e);
                } catch (InvocationTargetException e) {
                    throw new RestlerException("Can't create id wrapper", e);
                } catch (NoSuchMethodException | InstantiationException e) {
                    throw new RestlerException("Could not instantiate id object", e);
                }
            }
        }
    }

    private boolean isList(Class<?> someClass) {
        if (someClass == null) {
            return false;
        }
        if (someClass.equals(List.class)) {
            return true;
        }
        for (Class<?> intrf : someClass.getInterfaces()) {
            if (isList(intrf)) {
                return true;
            }
        }

        return isList(someClass.getSuperclass());
    }
}
