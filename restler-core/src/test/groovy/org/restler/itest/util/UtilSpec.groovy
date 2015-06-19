package org.restler.itest.util

import org.restler.util.Util
import spock.lang.Specification

class UtilSpec extends Specification {
    def "test utils toString"() {
        expect:
        String testString = "Util toString OK"
        InputStream inputStream = new ByteArrayInputStream(testString.getBytes("UTF-8"))
        Util.toString(inputStream) == testString
    }
}
