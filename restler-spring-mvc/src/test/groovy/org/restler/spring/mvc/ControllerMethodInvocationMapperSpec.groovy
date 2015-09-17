package org.restler.spring.mvc

import org.restler.client.RestlerException
import spock.lang.Specification

class ControllerMethodInvocationMapperSpec extends Specification {

    def baseUrl = "http://localhost:8080"
    def anyUri = new URI(baseUrl)

    def "Controller method invocation mapper should correctly process null arguments"() {
        given:
        def mapper = new ControllerMethodInvocationMapper(anyUri, ParameterResolver.valueOfParamResolver())
        def method = Greeter.class.getDeclaredMethod("getGreeting", [String.class, String.class] as Class[])

        when:
        def invocation = mapper.apply(method, null, null)

        then:
        invocation.url == new URI(baseUrl + "/greeter/greetings/null")
    }

    def "In case of call of method with url template variable without corresponding method parameter, exception should be thrown"() {
        given:

        def mapper = new ControllerMethodInvocationMapper(anyUri, ParameterResolver.valueOfParamResolver())
        def methodWithNotMappedVar = Greeter.class.getDeclaredMethod("methodWithNotMappedVar", [String.class] as Class[])

        when:
        mapper.apply(methodWithNotMappedVar, null)

        then:
        thrown(RestlerException)
    }


}
