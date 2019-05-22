/*
 * Copyright 2019 Wooga GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

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
