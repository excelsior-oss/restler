package org.restler.spring.data.methods.associations;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.restler.spring.data.util.Placeholder;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class AssociatedResource {
    private final Object resource;
    private final ObjectNode objectNode;

    private final List<Field> associateFields;

    private AssociatedResourceState state;

    private final List<Placeholder<Object>> idPlaceholders = new ArrayList<>();

    public AssociatedResource(Object resource, ObjectNode objectNode, List<Field> associateFields, AssociatedResourceState state) {
        this.resource = resource;
        this.objectNode = objectNode;
        this.associateFields = associateFields;
        this.state = state;
    }

    public Object getResource() {
        return resource;
    }

    public ObjectNode getObjectNode() {
        return objectNode;
    }

    public List<Field> getAssociateFields() {
        return associateFields;
    }

    public AssociatedResourceState getState() {
        return state;
    }

    public void addIdPlaceholder(Placeholder<Object> idPlaceholder) {
        idPlaceholders.add(idPlaceholder);
    }

    public List<Placeholder<Object>> getIdPlaceholders() {
        return idPlaceholders;
    }

    public void changeState(AssociatedResourceState state) {
        this.state = state;
    }
}
