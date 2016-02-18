package groovy.org.restler.spring.data

import org.restler.http.HttpCall
import org.restler.spring.data.SpringDataMethodInvocationMapper
import spock.lang.Specification

class SpringDataMethodInvocationMapperSpec extends Specification {
    def "Proxy for Spring Data Rest repository should be created successfully"() {
        given:
        def mapper = new SpringDataMethodInvocationMapper(new URI("http://localhost"), null)
        def findOneMethod = Repository.class.methods.find { it.name == "findOne" }
        def receiver = Mock(Repository.class)

        when:
        def call = mapper.map(receiver, findOneMethod,  1L)

        then:
        ((HttpCall)call).url == new URI("http://localhost:80/testRepo/1")
    }

}
