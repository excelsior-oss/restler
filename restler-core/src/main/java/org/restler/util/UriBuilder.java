package org.restler.util;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.escape.Escaper;
import com.google.common.net.UrlEscapers;
import org.restler.client.RestlerException;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

public class UriBuilder {

    public static final Escaper urlPathSegmentEscaper = UrlEscapers.urlPathSegmentEscaper();
    public static final Escaper urlFormParameterEscaper = UrlEscapers.urlFormParameterEscaper();
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
    private ImmutableMultimap<String, String> queryParams = ImmutableMultimap.of();
    private Map<String, ?> pathVariables = ImmutableMap.of();

    public UriBuilder(String baseUrl) {
        this(toUri(baseUrl));
    }

    public UriBuilder(URI baseUrl) {
        scheme = baseUrl.getScheme();
        host = baseUrl.getHost();
        port = validPort(baseUrl);
        path = baseUrl.getPath();
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

    public UriBuilder host(String host) {
        this.host = host;
        return this;
    }

    public UriBuilder port(int port) {
        this.port = port;
        return this;
    }

    public UriBuilder path(String path) {
        this.path = path.startsWith("/")
                ? path
                : "/" + path;
        return this;
    }

    public UriBuilder scheme(String scheme) {
        this.scheme = scheme;
        return this;
    }

    public UriBuilder queryParams(ImmutableMultimap<String, String> queryParams) {
        this.queryParams = queryParams;
        return this;
    }

    public UriBuilder pathVariables(Map<String, ?> pathVariables) {
        this.pathVariables = Collections.unmodifiableMap(pathVariables);
        return this;
    }

    public URI build() {
        try {
            return new URI(scheme + "://" + host + ":" + port + substituteVariables(path) + queryParamsString());
        } catch (URISyntaxException e) {
            throw new RestlerException(e);
        }
    }

    private String substituteVariables(String path) {
        BiFunction<String, Map.Entry<String, ?>, String> accumulator = (String acc, Map.Entry<String, ?> e) ->
                acc.replaceAll("\\{" + e.getKey() + "\\}", urlPathSegmentEscaper.escape(String.valueOf(e.getValue())));
        BinaryOperator<String> combiner = (String __, String expandedPath) -> expandedPath;
        return pathVariables.entrySet().stream().reduce(path, accumulator, combiner);
    }

    private String queryParamsString() {
        String entryStream = queryParams.entries().stream().
                map(entryPair -> urlFormParameterEscaper.escape(entryPair.getKey()) + "=" + urlFormParameterEscaper.escape(entryPair.getValue())).
                collect(Collectors.joining());
        if (entryStream.length() > 0) {
            return "?" + entryStream;
        } else {
            return "";
        }
    }
}
