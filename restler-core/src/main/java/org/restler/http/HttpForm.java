package org.restler.http;

import com.google.common.collect.ImmutableMultimap;

public class HttpForm {

    private final ImmutableMultimap<String, String> fields;

    public HttpForm() {
        fields = ImmutableMultimap.of();
    }

    public HttpForm(ImmutableMultimap<String, String> fields) {
        this.fields = fields;
    }

    public HttpForm add(String name, String value) {
        ImmutableMultimap.Builder<String, String> builder = new ImmutableMultimap.Builder<>();
        builder.putAll(fields).
                put(name, value);
        return new HttpForm(builder.build());
    }

    public ImmutableMultimap<String, String> getFields() {
        return fields;
    }
}
