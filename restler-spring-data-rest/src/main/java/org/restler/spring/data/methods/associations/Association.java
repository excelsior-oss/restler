package org.restler.spring.data.methods.associations;

import org.restler.util.Pair;

public class Association {
    private final AssociatedResource firstResource;
    private final AssociatedResource secondResource;

    private final Pair<String, String> jsonField;

    private boolean resolved = false;

    private final AssociationType associationType;

    public Association(AssociatedResource firstResource, AssociatedResource secondResource, Pair<String, String> jsonField, AssociationType associationType) {
        this.firstResource = firstResource;
        this.secondResource = secondResource;
        this.jsonField = jsonField;
        this.associationType = associationType;
    }

    public AssociatedResource getFirstResource() {
        return firstResource;
    }

    public AssociatedResource getSecondResource() {
        return secondResource;
    }

    public Pair<String, String> getJsonField() {
        return jsonField;
    }

    public AssociationType getAssociationType() {
        return associationType;
    }

    public boolean isResolved() {
        return resolved;
    }

    public void markAsResolved() {
        resolved = true;
    }
}
