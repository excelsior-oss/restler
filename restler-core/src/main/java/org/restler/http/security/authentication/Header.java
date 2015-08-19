package org.restler.http.security.authentication;

import java.util.Arrays;

class Header {

    public final String name;
    private final String[] values;

    public Header(String name, String ... values) {
        this.name = name;
        this.values = values;
    }

    public String[] values() {
        return Arrays.copyOf(values, values.length);
    }

}
