package org.restler.spring.mvc

import spock.lang.Specification

import java.lang.reflect.Method

class ParameterNameDiscovererSpec extends Specification {

    ParameterNameDiscoverer parameterNameDiscoverer = new ParameterNameDiscoverer()

    def "Test discover parameter names for method with primitive type parameters"() {

        given:
        Method method = TestMethods.getMethod("methodWithPrimitiveArguments", int.class, double.class, short.class, byte.class,
                boolean.class, float.class, char.class)
        when:
        def names = parameterNameDiscoverer.getParameterNames(method)
        then:
        names[0] == "intParameter"
        names[1] == "doubleParameter"
        names[2] == "shortParameter"
        names[3] == "byteParameter"
        names[4] == "booleanParameter"
        names[5] == "floatParameter"
        names[6] == "charParameter"
    }

    def "Test discover parameter names for method with arrays in arguments"() {
        given:
        Method method = TestMethods.getMethod("methodWithArrayArguments", int[].class, String[].class, Object[].class)
        when:
        def names = parameterNameDiscoverer.getParameterNames(method)
        then:
        names[0] == "intArray"
        names[1] == "stringArray"
        names[2] == "objectArray"
    }

    def "Test discover parameter names for method with different arguments"() {
        given:
        Method method = TestMethods.getMethod("methodWithDefferentArguments", String.class, Object.class, short[].class, int.class)
        when:
        def names = parameterNameDiscoverer.getParameterNames(method)
        then:
        names[0] == "stringParameter"
        names[1] == "objectParameter"
        names[2] == "shortArray"
        names[3] == "intParameter"
    }

    def "Test discover parameter names for method with vararg"() {
        given:
        Method method = TestMethods.getMethod("methodWithVararg", int[].class);
        when:
        def names = parameterNameDiscoverer.getParameterNames(method)
        then:
        names[0] == "integerArgs"
    }
}
