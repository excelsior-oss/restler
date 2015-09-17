package org.restler

import org.restler.integration.Controller
import org.restler.spring.mvc.SpringMvcSupport
import org.restler.util.IntegrationSpec
import spock.lang.Specification

class ParametersResolvingIntegrationTest extends Specification implements IntegrationSpec {

    def service = new Restler("http://localhost:8080", new SpringMvcSupport()).build()
    def controller = service.produceClient(Controller.class)

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
