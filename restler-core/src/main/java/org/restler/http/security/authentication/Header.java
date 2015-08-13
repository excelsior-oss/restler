package org.restler.http.security.authentication;

import java.util.Arrays;

public class Header {

    public String name;
    private String[] values;

    public Header(String name, String ... values) {
        this.name = name;
        this.values = values;
    }

    public String[] values() {
        return Arrays.copyOf(values, values.length);
    }

}
