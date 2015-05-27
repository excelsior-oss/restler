import org.restler.factory.CGLibClientFactory
import org.restler.factory.ControllerMethodExecutor
import spock.lang.Specification
/**
 * Created by pasa on 26.05.2015.
 */
class MethodDescriptionProducingSpec extends Specification{

    def executor = Mock(ControllerMethodExecutor);
    def factory = new CGLibClientFactory(executor);
    def client = factory.produce(Greeter.class);

    def "interceptor calls executor"(){

        when:
            client.getGreeting("French","Medved");
        then:
            1 * executor.<String>execute(!null,null,!null,!null);
    }

}
