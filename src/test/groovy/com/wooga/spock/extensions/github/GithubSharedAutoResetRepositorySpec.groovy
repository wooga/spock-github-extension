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

    def "resets commits on repository reset"() {
        given: "a repository with captured refs"
        def defaultBranch = testRepository.defaultBranch
        def capturedCommit = testRepository.getCommit(defaultBranch)
        testRepository.captureResetRefs()

        and: "a new commit in the head of the main branch"
        testRepository.commit("simple test commit")

        when: "repository is reseted"
        testRepository.resetRepository()

        then: "head commit is the same as the captured state"
        testRepository.getCommit(defaultBranch).SHA1 == capturedCommit.SHA1
    }

    def "delete created branches on repository reset"() {
        given: "a repository with captured refs"
        testRepository.captureResetRefs()

        and: "a newly created branch"
        def createdBranch = testRepository.createBranch("testbranch")

        when: "repository is reseted"
        testRepository.resetRepository()

        then: "newly created branch does not exists anymore"
        !testRepository.branches.containsKey(createdBranch.name)
    }

    def "rewind branches on repository reset"() {
        given: "a repository with a secondary branch and captured refs"
        def secondaryBranch = testRepository.createBranch("secondary")
        def previousCommit = testRepository.getCommit(secondaryBranch.SHA1)
        testRepository.captureResetRefs()


        and: "a new commit in the head of a secondary branch"
        testRepository.createCommit().
                message("secondary commit").
                tree(previousCommit.tree.sha).
                parent(secondaryBranch.SHA1).create()

        when: "repository is reseted"
        testRepository.resetRepository()

        then: "head commit is the same as the captured state"
        testRepository.branches.containsKey(secondaryBranch.name)
        testRepository.getCommit(secondaryBranch).SHA1 == previousCommit.SHA1

    }

    def "deletes created tags on repository reset"() {
        given: "a repository with captured refs"
        testRepository.captureResetRefs()

        and: "a newly created tag"
        def newTag = testRepository.tag("newTag")

        when: "repository is reseted"
        testRepository.resetRepository()

        then: "newly created tag does not exists anymore"
        !testRepository.findTag(newTag.name).isPresent()
    }
}
