import org.junit.Ignore
import org.restler.Service
import org.restler.client.CGLibClientFactory
import org.restler.client.ServiceMethodInvocationExecutor
import spock.lang.Specification

/**
 * Created by pasa on 26.05.2015.
 */
@Ignore
class MethodDescriptionProducingSpec extends Specification {

    def executor = Mock(ServiceMethodInvocationExecutor);
    def service = new Service(new CGLibClientFactory(executor));
    def client = service.produceClient(Greeter.class);

    def "interceptor calls executor"() {

        when:
        client.getGreeting("French", "Medved");
        then:
        1 * executor.<String> execute(!null, null, !null, !null);
    }

}
