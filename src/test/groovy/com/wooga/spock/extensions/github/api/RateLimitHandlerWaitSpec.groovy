package com.wooga.spock.extensions.github.api

import org.kohsuke.github.RateLimitHandler
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

class RateLimitHandlerWaitSpec extends Specification {

    @Subject
    RateLimitHandler waitHandler

    @Unroll("Sleeps thread #message")
    def "Sleeps thread until rate limit resets"() {
        given: "a wait handler"
        waitHandler = new RateLimitHandlerWait()

        and: "a test exception"
        IOException e = new IOException()

        and: "current time millis"
        long startTimeMillis = System.currentTimeMillis()

        and: "a http connection"
        HttpURLConnection uc = Mock(HttpURLConnection)
        def resetInSeconds = Math.ceil((startTimeMillis + rateLimitResetMilis) / 1000).longValue()

        uc.getHeaderField("X-RateLimit-Reset") >> "${resetInSeconds}"

        when:
        waitHandler.onError(e, uc)

        then:
        long endTimeMillis = System.currentTimeMillis()
        waitTimeRange.containsWithinBounds(endTimeMillis - startTimeMillis)

        where:
        rateLimitResetMilis << [5000, 15000]
        message = rateLimitResetMilis < 10000 ? "for minimum wait time of 10 sec" : "until rate limit resets"

        minTimeout = (Math.max(10000, rateLimitResetMilis) - 1000).longValue()
        maxTimeout = (Math.max(10000, rateLimitResetMilis) + 1000).longValue()
        waitTimeRange = (minTimeout..maxTimeout)
    }

    def "Sleeps thread for minimum wait time of 10 sec if rate limit header is missing"() {
        given: "a wait handler"
        waitHandler = new RateLimitHandlerWait()

        and: "a test exception"
        IOException e = new IOException()

        and: "current time millis"
        long startTimeMillis = System.currentTimeMillis()

        and: "a http connection"
        HttpURLConnection uc = Mock(HttpURLConnection)

        when:
        waitHandler.onError(e, uc)

        then:
        long endTimeMillis = System.currentTimeMillis()
        (9000.longValue()..11000.longValue()).containsWithinBounds(endTimeMillis - startTimeMillis)
    }
}
