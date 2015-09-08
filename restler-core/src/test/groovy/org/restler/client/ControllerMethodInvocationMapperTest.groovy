package org.restler.client

import org.restler.spring.ControllerMethodInvocationMapper
import org.restler.test.Greeter
import spock.lang.Specification

class ControllerMethodInvocationMapperTest extends Specification {

    def anyUri = null

    def "Controller method invocation mapper should correctly process null arguments"() {
        given:
        def mapper = new ControllerMethodInvocationMapper(anyUri, ParameterResolver.valueOfParamResolver())
        def method = Greeter.class.getDeclaredMethod("getGreeting", [String.class, String.class] as Class[])

        when:
        def invocation = mapper.apply(method, null, null)

        then:
        invocation.pathVariables['language'] == "null"
        invocation.queryParams.size() == 0
    }

    def "In case of call of method with url template variable without corresponding method parameter, exception should be thrown"() {
        given:

        def mapper = new ControllerMethodInvocationMapper(anyUri, ParameterResolver.valueOfParamResolver())
        def methodWithNotMappedVar = Greeter.class.getDeclaredMethod("methodWithNotMappedVar", [String.class] as Class[])

        when:
        try {
            mapper.apply(methodWithNotMappedVar, null)
        } catch (RestlerException e) {
            e.printStackTrace()
        }

        then:
        thrown(RestlerException)
    }


}
