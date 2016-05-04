package org.restler.spring.data.util;

import org.restler.client.RestlerException;
import org.restler.spring.data.proxy.ResourceProxy;
import org.springframework.data.repository.Repository;

import javax.persistence.EmbeddedId;
import javax.persistence.Id;
import java.lang.reflect.Field;

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
}
