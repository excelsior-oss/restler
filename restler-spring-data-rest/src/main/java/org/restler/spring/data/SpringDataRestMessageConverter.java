package org.restler.spring.data;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
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
import java.lang.reflect.*;
import java.util.*;

class SpringDataRestMessageConverter implements GenericHttpMessageConverter<Object> {


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
                    res.add(new ResourceProxyMaker().make(elementClass, object, hrefs));
                }
                return res;
            }

            return new ArrayList(); //if collection is empty
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

        return new ResourceProxyMaker().make(aClass, object, hrefs);
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
