package org.restler.spring.data.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializable;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import org.restler.client.RestlerException;
import org.restler.spring.data.proxy.ResourceProxy;
import org.springframework.data.repository.Repository;

import javax.persistence.EmbeddedId;
import javax.persistence.Id;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class ResourceHelper {
    public static String getRepositoryUri(Repositories repositories, String baseUri, Object resource) {
        Repository repository = repositories.getByResourceClass(resource.getClass()).orElse(null);

        if(repository == null) {
            throw new RestlerException("Can't find repository " + resource.getClass() + ".");
        }

        while(baseUri.endsWith("/") || baseUri.endsWith("\\")) {
            baseUri = baseUri.substring(0, baseUri.length()-1);
        }

        return baseUri + "/" + RepositoryUtils.getRepositoryPath(repository.getClass().getInterfaces()[0]);
    }

    public static String getUri(Repositories repositories, String baseUri, Object resource) {
        if(resource instanceof ResourceProxy) {
            return ((ResourceProxy) resource).getSelfUri();
        } else {
            Object id = getId(resource);
            if(id == null) {
                throw new RestlerException("Id can't be null.");
            }
            return  getRepositoryUri(repositories, baseUri, resource) + "/" + id;
        }
    }

    public static UriWithPlaceholder getUri(Repositories repositories, String baseUri, Object resource, Placeholder<Object> idPlaceholder) {
        if(resource instanceof ResourceProxy) {
            return new UriWithPlaceholder(((ResourceProxy) resource).getSelfUri(), null);
        }
        return new UriWithPlaceholder(getRepositoryUri(repositories, baseUri, resource) + "/", idPlaceholder);
    }

    public static Object getId(Object object) {
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

    private static class UriWithPlaceholder implements JsonSerializable {
        private final String baseUri;
        private final Placeholder<Object> placeholderId;

        public UriWithPlaceholder(String baseUri, Placeholder<Object> placeholderId) {
            this.baseUri = baseUri;
            this.placeholderId = placeholderId;
        }

        @Override
        public void serialize(JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeString(this.toString());
        }

        @Override
        public void serializeWithType(JsonGenerator gen, SerializerProvider serializers, TypeSerializer typeSer) throws IOException {

        }

        @Override
        public String toString() {
            if(placeholderId == null) {
                return baseUri;
            }
            if(placeholderId.isValue()) {
                return baseUri + placeholderId.toString();
            }
            return "";
        }
    }
}
