package org.restler

import org.restler.integration.Controller
import spock.lang.Specification

class ParametersResolvingIntegrationTest extends Specification {

    def service = new ServiceBuilder("http://localhost:8080").build()
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
