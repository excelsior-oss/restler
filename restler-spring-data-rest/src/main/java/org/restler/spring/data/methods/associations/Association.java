package org.restler.spring.data.methods.associations;

import org.restler.spring.data.util.Placeholder;
import org.restler.util.Pair;

public class Association {
    private final AssociatedResource firstResource;
    private final AssociatedResource secondResource;

    private final Pair<String, Object> jsonField;

    private boolean resolved = false;

    private final AssociationType associationType;

    private final Placeholder<Object> idPlaceholder;

    public Association(AssociatedResource firstResource, AssociatedResource secondResource, Pair<String, Object> jsonField, AssociationType associationType, Placeholder<Object> idPlaceholder) {
        this.firstResource = firstResource;
        this.secondResource = secondResource;
        this.jsonField = jsonField;
        this.associationType = associationType;
        this.idPlaceholder = idPlaceholder;
    }

    public Association(AssociatedResource firstResource, AssociatedResource secondResource, Pair<String, Object> jsonField, AssociationType associationType) {
        this(firstResource, secondResource, jsonField, associationType, null);
    }

    public AssociatedResource getFirstResource() {
        return firstResource;
    }

    public AssociatedResource getSecondResource() {
        return secondResource;
    }

    public Pair<String, Object> getJsonField() {
        return jsonField;
    }

    public AssociationType getAssociationType() {
        return associationType;
    }

    public Placeholder<Object> getIdPlaceholder() {
        return idPlaceholder;
    }

    public boolean isResolved() {
        return resolved;
    }

    public void markAsResolved() {
        resolved = true;
    }
}
