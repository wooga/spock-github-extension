package com.wooga.spock.extensions.github.api

import org.kohsuke.github.RateLimitHandler
import spock.lang.Specification
import spock.lang.Subject

class RateLimitHandlerFailSpec extends Specification {

    @Subject
    RateLimitHandler failHandler


    def "throws custom error"() {
        given: "a wait handler"
        failHandler = new RateLimitHandlerFail()

        and: "a test exception"
        IOException e = new IOException()

        and: "a http connection"
        HttpURLConnection uc = Mock(HttpURLConnection)

        when:
        failHandler.onError(e, uc)

        then:
        def ioExeption = thrown(IOException)
        ioExeption.message == "API rate limit reached"
        ioExeption.cause == e
    }
}
