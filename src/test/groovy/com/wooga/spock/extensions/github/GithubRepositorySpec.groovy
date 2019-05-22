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

import spock.lang.Shared

class GithubRepositorySpec extends BaseGithubRepositorySpec {

    @GithubRepository(
            usernameEnv = "ATLAS_GITHUB_INTEGRATION_USER",
            tokenEnv = "ATLAS_GITHUB_INTEGRATION_PASSWORD")
    Repository testRepo

    @Override
    Repository getTestRepository() {
        return testRepo
    }

    @Shared
    Iterable<Repository> testRepos = []

    def cleanup() {
        // We can't check the repository got deleted after cleanup in the cleanup method.
        testRepos << testRepository
    }

    def cleanupSpec() {
        assert testRepos.every { !it.exists() }, "test repos should have been deleted"
        // We can't test this from our test, we'd have to use an ExternalSpec Runner to run a different spec
        //assert testRepository.exists(), "testRepository should not have been deleted before cleanupSpec"
    }

    def "creates test repo"() {
        expect:
        testRepository != null
    }
}
