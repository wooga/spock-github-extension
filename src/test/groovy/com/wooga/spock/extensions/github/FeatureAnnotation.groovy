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

package com.wooga.spock.extensions.github


import org.junit.Rule
import org.junit.contrib.java.lang.system.EnvironmentVariables
import spock.lang.Retry
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

@Retry(delay = 2000, count = 2, mode = Retry.Mode.SETUP_FEATURE_CLEANUP)
class FeatureAnnotation extends Specification {

    @Rule
    public final EnvironmentVariables environmentVariables = new EnvironmentVariables()

    @Shared
    Iterable<Repository> testRepos = []

    def setup() {
        environmentVariables.set("GITHUB_USERNAME", System.getenv("ATLAS_GITHUB_INTEGRATION_USER"))
        environmentVariables.set("GITHUB_TOKEN", System.getenv("ATLAS_GITHUB_INTEGRATION_PASSWORD"))
    }

    def cleanupSpec() {
        assert testRepos.every {!it.exists()}, "test repos should have been deleted"
    }

    @GithubRepository
    @Unroll
    def "can annotate a feature method with a repo"(Repository repo) {
        expect:
        repo.exists()
    }

    @GithubRepository
    def "can annotate a feature method with a repo with iteration"(Boolean theValue, Repository repo) {
        expect:
        repo.exists()

        cleanup:
        testRepos << repo

        where:
        theValue | _
        false    | _
        true     | _

        and:
        repo = null
    }

    @GithubRepository(resetAfterTestCase = true)
    @Unroll
    def "can reset repo for each iteration"(String message, int expectedCommitCount, Repository repo) {
        given: "add an empty commit"
        repo.commit(message)

        expect:
        repo.queryCommits().list().collect().size() == expectedCommitCount

        cleanup:
        testRepos << repo

        where:
        message    | expectedCommitCount
        "commit 1" | 2
        "commit 2" | 2
        "commit 3" | 2

        and:
        repo = null
    }

    @GithubRepository(resetAfterTestCase = false)
    @Unroll
    def "can share repo in iteration"(String message, int expectedCommitCount, Repository repo) {
        given: "add an empty commit"
        repo.commit(message)

        expect:
        repo.queryCommits().list().collect().size() == expectedCommitCount

        cleanup:
        testRepos << repo

        where:
        message    | expectedCommitCount
        "commit 1" | 2
        "commit 2" | 3
        "commit 3" | 4

        and:
        repo = null
    }
}
