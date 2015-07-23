package org.restler.client

import org.restler.test.Greeter
import spock.lang.Specification

class ControllerMethodInvocationMapperTest extends Specification {

    def "Controller method invocation mapper should correctly process null arguments"() {
        given:
        def mapper = new ControllerMethodInvocationMapper("any url")
        def method = Greeter.class.getDeclaredMethod("getGreeting", [String.class, String.class] as Class[])
        when:
        def invocation = mapper.apply(method, null, null)

        then:
        invocation.pathVariables['language'] == "null"
        invocation.requestParams.size() == 0
    }

}
