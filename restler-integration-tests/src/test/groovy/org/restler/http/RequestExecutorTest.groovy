package org.restler.http

import com.google.common.collect.ImmutableMultimap
import com.google.common.net.HttpHeaders
import org.restler.client.RestlerException
import org.restler.util.IntegrationSpec
import spock.lang.Shared
import spock.lang.Specification

abstract class RequestExecutorTest extends Specification implements IntegrationSpec {

    @Shared
    RequestExecutor executor

    def "RequestExecutor should successfully execute GET requests"() {
        given:
        HttpCall call = new HttpCall(new URI("http://localhost:8080/get"), HttpMethod.GET, null, ImmutableMultimap.of(), String.class)

        when:
        def res = executor.execute(call)

        then:
        res.getResult() == "OK"
    }

    def "RequestExecutor should throw RestlerException when given not serializable body"() {
        given:
        HttpCall call = new HttpCall(new URI("http://localhost:8080/postBody"), HttpMethod.POST, new NotSerializable(), ImmutableMultimap.of(), String.class)

        when:
        executor.execute(call)

        then:
        thrown(RestlerException)
    }

    def "RequestExecutor should pass given headers"() {
        given:
        def token = "Basic " + Base64.getMimeEncoder().encodeToString("user:password".getBytes())
        def headers = ImmutableMultimap.of(HttpHeaders.AUTHORIZATION, token)
        HttpCall call = new HttpCall(new URI("http://localhost:8080/secured/get"), HttpMethod.GET, null, headers, String.class)

        when:
        def res = executor.execute(call)

        then:
        res.getResult() == "Secure OK"
    }

    def "RequestExecutor should successfully call void-returning methods"() {
        given:
        HttpCall call = new HttpCall(new URI("http://localhost:8080/void"), HttpMethod.GET, null, ImmutableMultimap.of(), getClass().getMethod("voidMethod").getReturnType())

        when:
        executor.execute(call)

        then:
        true
    }

    def "RequestExecutor should successfully call methods returning body"() {
        given:
        HttpCall call = new HttpCall(new URI("http://localhost:8080/postBody"), HttpMethod.POST, new Serializable("data"), ImmutableMultimap.of(), Serializable.class)

        when:
        def res = executor.execute(call)

        then:
        res.getResult().getField() == "data"
    }

    def "RequestExecutor should return failed response on not ok http status code"() {
        given:
        HttpCall call = new HttpCall(new URI("http://localhost:8080/throwException"), HttpMethod.POST, "java.lang.RuntimeException", ImmutableMultimap.of(), Serializable.class)

        when:
        def res = executor.execute(call)

        then:
        res instanceof FailedResponse
    }

    void voidMethod() {}
}

class Serializable {

    String field;

    Serializable() {
    }

    Serializable(String field) {
        this.field = field
    }

    String getField() {
        return field
    }

    void setField(String field) {
        this.field = field
    }
}

class NotSerializable {

    def getField() {
        throw new RuntimeException()
    }

}
