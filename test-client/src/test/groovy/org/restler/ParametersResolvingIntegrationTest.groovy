package org.restler

import org.restler.integration.ControllerApi
import org.restler.spring.mvc.SpringMvcSupport
import spock.lang.Specification

class ParametersResolvingIntegrationTest extends Specification {

    def service = new Restler("http://localhost:8080", new SpringMvcSupport()).build()
    def controller = service.produceClient(ControllerApi.class)

    def "With default parameter resolver 'null' string should be passed as string"() {

        when:
        def result = controller.isNull("null")

        then:
        !result
    }

    def "With default parameter resolver null should be passed as null"() {

        when:
        def result = controller.isNull(null)

        then:
        result
    }

}
