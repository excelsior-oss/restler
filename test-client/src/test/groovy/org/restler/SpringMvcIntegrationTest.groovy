package org.restler

import org.restler.client.CGLibClientFactory
import org.restler.client.RestlerException
import org.restler.http.security.authentication.CookieAuthenticationStrategy
import org.restler.integration.ControllerApi
import org.restler.spring.mvc.SpringMvcSupport
import org.restler.spring.mvc.files.RestlerMultipartFile
import spock.lang.Specification

import static Tests.login
import static Tests.password

class SpringMvcIntegrationTest extends Specification {

    Service serviceWithFormAuth = new Restler("http://localhost:8080", new SpringMvcSupport()).
            formAuthentication(new URI("http://localhost:8080/login"), login, password).
            build();

    Service serviceWithBasicAuth = new Restler("http://localhost:8080", new SpringMvcSupport()).
            httpBasicAuthentication(login, password).
            build();

    def controller = serviceWithFormAuth.produceClient(ControllerApi.class);
    def controllerWithBasicAuth = serviceWithBasicAuth.produceClient(ControllerApi.class)

    def "test unsecured get"() {
        expect:
        "OK" == controller.publicGet()
    }

    def "test get with variable"() {
        expect:
        "Variable OK" == controller.getWithVariable("test", "Variable OK")
    }

    def "test secured get authorized with form auth"() {
        expect:
        "Secure OK" == controller.securedGet()
    }

    def "test secured get authorized with basic auth"() {
        expect:
        "Secure OK" == controllerWithBasicAuth.securedGet()
    }

    def "test exception CGLibClient when class not a controller"() {
        when:
        serviceWithFormAuth.produceClient(CGLibClientFactory.class)
        then:
        thrown(RestlerException)
    }

    def "test exception CookieAuthenticationRequestExecutor when cookie name is empty"() {
        when:
        new CookieAuthenticationStrategy("");
        then:
        thrown(IllegalArgumentException)
    }

    def "methods, that returns void should be called successfully"() {
        when:
        controller.returnVoid()
        then:
        true
    }

    def "file upload"() {
        when:
        def result = controller.fileUpload("testFile.txt", new RestlerMultipartFile("file", new File("resources/testFile.txt"), "text/plain"))
        then:
        result == "Success"
    }
}
