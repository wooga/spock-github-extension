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

import spock.lang.Retry
import spock.lang.Shared
import spock.lang.Unroll

@Retry(delay = 2000, count = 2, mode = Retry.Mode.SETUP_FEATURE_CLEANUP)
class GithubSharedAutoResetRepositorySpec extends BaseGithubRepositorySpec {

    @Shared
    @GithubRepository(
            usernameEnv = "ATLAS_GITHUB_INTEGRATION_USER",
            tokenEnv = "ATLAS_GITHUB_INTEGRATION_PASSWORD",
            resetAfterTestCase = true,
            repositoryNamePrefix = "shared-test-repo-auto-reset"
    )
    Repository testRepo

    @Override
    Repository getTestRepository() {
        return testRepo
    }

    def cleanupSpec() {
        // We can't test this from our test, we'd have to use an ExternalSpec Runner to run a different spec
        //assert testRepository.exists(), "testRepository should not have been deleted before cleanupSpec"
    }

    def "creates test repo"() {
        expect:
        testRepository != null
    }

    @Unroll
    def "resets state after each example: #message"() {
        given:
        testRepository.commit("Commit one")
        testRepository.commit("Commit two")

        expect:
        testRepository.repository.queryCommits().list().collect().size() == commits

        where:
        commits << [3, 3]
        message << ['initial', 'second run']
    }
}
