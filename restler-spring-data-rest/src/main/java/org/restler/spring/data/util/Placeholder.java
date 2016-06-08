package org.restler.spring.data.util;

public class Placeholder<T> {

    private T value = null;
    private final String alternativeText;

    public Placeholder() {
        alternativeText = "{placeholder}";
    }

    public Placeholder(String alternativeText) {
        this.alternativeText = alternativeText;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public boolean isValue() {
        return value != null;
    }

    @Override
    public String toString() {
        if(value == null) {
            return alternativeText;
        }

        return value.toString();
    }
}
