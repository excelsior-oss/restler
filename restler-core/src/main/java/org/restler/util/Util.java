package org.restler.util;

import java.io.IOException;
import java.io.InputStream;

public final class Util {

    private Util() {
    }

    public static String toString(InputStream is) throws IOException {
        StringBuilder buffer = new StringBuilder();
        byte[] buf = new byte[1024];
        int len;
        while ((len = is.read(buf)) != -1) {
            buffer.append(new String(buf, 0, len));
        }
        return buffer.toString();
    }

}
