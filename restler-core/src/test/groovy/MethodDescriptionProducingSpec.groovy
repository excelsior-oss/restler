import io.github.chcat.restclientgenerator.factory.ControllerMethodExecutor
import io.github.chcat.restclientgenerator.factory.ControllerMethodInterceptor
import spock.lang.Specification

/**
 * Created by pasa on 26.05.2015.
 */
class MethodDescriptionProducingSpec extends Specification{

    def executor = Mock(ControllerMethodExecutor);
    def interceptor = new ControllerMethodInterceptor(executor);
    def method = Greeter.getMethods()[0];

    def "interceptor calls executor"(){

        when:
            interceptor.intercept(null,method,["French","Medved"].toArray(),null);
        then:
            1 * executor.<String>execute(!null,null,!null,!null);
    }

}
