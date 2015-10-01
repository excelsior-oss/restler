package org.restler.spring.mvc

import org.restler.client.RestlerException
import spock.lang.Specification

class SpringMvcMethodInvocationMapperSpec extends Specification {

    def baseUri = "http://localhost:8080"
    def baseUriWithPath = "http://localhost:8080/api"

    def anyUri = new URI(baseUri)
    def uriWithPath = new URI(baseUriWithPath)
    def greeter = Mock(Greeter)

    def "Controller method invocation mapper should correctly process null arguments"() {
        given:
        def mapper = new SpringMvcMethodInvocationMapper(anyUri, ParameterResolver.valueOfParamResolver())
        def method = Greeter.class.getDeclaredMethod("getGreeting", [String.class, String.class] as Class[])

        when:
        def invocation = mapper.map(greeter, method, null, null)

        then:
        invocation.url == new URI(baseUri + "/greeter/greetings/null")
    }

    def "In case of call of method with url template variable without corresponding method parameter, exception should be thrown"() {
        given:

        def mapper = new SpringMvcMethodInvocationMapper(anyUri, ParameterResolver.valueOfParamResolver())
        def methodWithNotMappedVar = Greeter.class.getDeclaredMethod("methodWithNotMappedVar", [String.class] as Class[])

        when:
        mapper.map(greeter, methodWithNotMappedVar, null)

        then:
        thrown(RestlerException)
    }

    def "Base uris with paths should be supported"() {
        given:
        def mapper = new SpringMvcMethodInvocationMapper(uriWithPath, ParameterResolver.valueOfParamResolver())
        def method = Greeter.class.getDeclaredMethod("getGreeting", [String.class, String.class] as Class[])

        when:
        def invocation = mapper.map(greeter, method, null, null)

        then:
        invocation.url == new URI(baseUriWithPath + "/greeter/greetings/null")
    }

}
