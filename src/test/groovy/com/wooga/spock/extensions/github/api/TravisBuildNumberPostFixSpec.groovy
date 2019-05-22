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

import org.junit.Rule
import org.junit.contrib.java.lang.system.EnvironmentVariables
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

class TravisBuildNumberPostFixSpec extends Specification {

    @Subject
    RepositoryPostFix postFix

    @Rule
    EnvironmentVariables env = new EnvironmentVariables()

    @Unroll
    def "returns #message when #reason"() {
        given: "the postfix instance"
        postFix = new TravisBuildNumberPostFix()

        and: "value in TRAVIS_JOB_NUMBER env variable"
        //remove current value from env if we running in CI
        env.clear("TRAVIS_JOB_NUMBER")
        if(setValue) {
            env.set("TRAVIS_JOB_NUMBER", expectedValue)
        }

        expect:
        postFix.getPostFix() == expectedValue

        where:
        expectedValue | message                                   | reason
        "32"          | "build number from TRAVIS_JOB_NUMBER env" | "when env variable is set"
        ""            | "build number from TRAVIS_JOB_NUMBER env" | "when env variable is not set"

        setValue = expectedValue != ""
    }
}
