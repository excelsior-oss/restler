package org.restler.spring.data;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.reflect.TypeToken;
import org.restler.client.RestlerException;
import org.restler.spring.data.proxy.ResourceProxyMaker;
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
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

class SpringDataRestMessageConverter implements GenericHttpMessageConverter<Object> {

    private final ResourceProxyMaker resourceProxyMaker = new ResourceProxyMaker();

    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public boolean canRead(Type type, Class<?> aClass, MediaType mediaType) {
        if (!(type instanceof ParameterizedType)) {
            return false;
        }
        Class<?> resultClass = TypeToken.of( ((ParameterizedType) type).getRawType() ).getRawType();
        return isList(resultClass);
    }

    @Override
    public Object read(Type type, Class<?> aClass, HttpInputMessage httpInputMessage) throws IOException, HttpMessageNotReadableException {

        JsonNode rootNode = objectMapper.readTree(httpInputMessage.getBody());

        Class<?> resultClass = TypeToken.of( ((ParameterizedType) type).getRawType() ).getRawType();

        Class<?> elementClass;
        try {
            elementClass = Class.forName(((ParameterizedType) type).getActualTypeArguments()[0].getTypeName());
        } catch (ClassNotFoundException e) {
            throw new RestlerException(e);
        }
        if (isList(resultClass)) {

            JsonNode embedded = rootNode.get("_embedded");

            Optional<ArrayNode> first = StreamSupport.stream(embedded.spliterator(), false).
                    filter(e -> e instanceof ArrayNode).
                    map(e -> (ArrayNode) e).
                    findFirst();

            return first.map(objects -> mapToProxies(objects, elementClass)).
                    orElse(new ArrayList());
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
        JsonNode rootNode = objectMapper.readTree(httpInputMessage.getBody());
        return makeObject(aClass, rootNode);
    }

    private Object makeObject(Class<?> aClass, JsonNode rootNode) {
        try {
            HashMap<String, String> hrefs = getObjectHrefs(rootNode);
            Object entity = objectMapper.treeToValue(rootNode, aClass);
            setId(entity, aClass, getId(rootNode));

            return resourceProxyMaker.make(aClass, entity, hrefs);
        }
        catch(JsonProcessingException e) {
            throw new RestlerException("Can't parse json to object.", e);
        }
    }

    @Override
    public void write(Object o, MediaType mediaType, HttpOutputMessage httpOutputMessage) throws IOException, HttpMessageNotWritableException {
    }

    private List<Object> mapToProxies(JsonNode objects, Class<?> elementClass) {
        return StreamSupport.stream(objects.spliterator(), false).
                map(o -> makeObject(elementClass, o)).
                collect(Collectors.toList());
    }

    private HashMap<String, String> getObjectHrefs(JsonNode objectNode) {
        HashMap<String, String> result = new LinkedHashMap<>();

        JsonNode linksNode = objectNode.get("_links");
        Iterator<String> names = linksNode.fieldNames();

        linksNode.forEach(node ->
                result.put(names.next(), node.get("href").toString().replace("\"", "")));

       return result;
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
                } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | InstantiationException e) {
                    throw new RestlerException("Id setting failed", e);
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
