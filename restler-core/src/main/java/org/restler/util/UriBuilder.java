package org.restler.util;

import org.restler.client.RestlerException;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

public class UriBuilder {

    private static final Map<String, Integer> defaultSchemePort;

    static {
        defaultSchemePort = new HashMap<String, Integer>() {{
            put("http", 80);
            put("https", 443);
        }};
    }

    private String host;
    private int port;
    private String path;
    private String scheme;

    public UriBuilder(String baseUrl) {
        this(toUri(baseUrl));
    }

    public UriBuilder(URI baseUrl) {
        scheme = baseUrl.getScheme();
        host = baseUrl.getHost();
        port = validPort(baseUrl);
        path = baseUrl.getPath();
    }

    public void host(String host) {
        this.host = host;
    }

    public void port(int port) {
        this.port = port;
    }

    public void path(String path) {
        this.path = path.startsWith("/")
                ? path
                : "/" + path;
    }

    public void scheme(String scheme) {
        this.scheme = scheme;
    }

    public URI build() {
        try {
            return new URI(scheme + "://" + host + ":" + port + path);
        } catch (URISyntaxException e) {
            throw new RestlerException(e);
        }
    }

    private static int validPort(URI baseUrl) {
        return baseUrl.getPort() != -1
                ? baseUrl.getPort()
                : defaultSchemePort.computeIfAbsent(baseUrl.getScheme(), UriBuilder::throwError);
    }

    private static Integer throwError(String scheme) {
        throw new RestlerException("Unsupported scheme: " + scheme);
    }

    private static URI toUri(String baseUrl) {
        try {
            return new URI(baseUrl);
        } catch (URISyntaxException e) {
            throw new RestlerException(e);
        }
    }
}
