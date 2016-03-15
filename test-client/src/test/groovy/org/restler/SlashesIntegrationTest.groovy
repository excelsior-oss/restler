package org.restler

import org.restler.integration.BothSlashesController
import org.restler.integration.LeftSlashController
import org.restler.integration.NoSlashedController
import org.restler.integration.RightSlashController
import org.restler.spring.mvc.SpringMvcSupport
import spock.lang.Specification

class SlashesIntegrationTest extends Specification /* implements IntegrationSpec */ {

    static def controllers = [NoSlashedController, BothSlashesController, LeftSlashController, RightSlashController]
    static def methods = ["noSlashes", "bothSlashes", "leftSlash", "rightSlash"]
    static def data = controllers.collect { ctrl ->
        methods.collect {
            return [ctrl, it]
        }
    }.flatten()

    def service = new Restler("http://localhost:8080", new SpringMvcSupport()).build()

    def "All slash combinations should be processed correctly"() {

        given:
        def controller = service.produceClient(controllerClass)

        when:
        def result = controller."$methodName"()

        then:
        result == methodName

        where:

        pair << data
        controllerClass = data[0]
        methodName = data[1]
    }

}
