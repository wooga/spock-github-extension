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

import org.kohsuke.github.GHIssueState
import org.kohsuke.github.GHPullRequest
import spock.lang.Retry
import spock.lang.Specification

@Retry(delay = 2000, count = 2, mode = Retry.Mode.SETUP_FEATURE_CLEANUP)
abstract class BaseGithubRepositorySpec extends Specification {

    abstract Repository getTestRepository()

    def "can create a simple commit"() {
        given:
        def defaultBranch = testRepository.defaultBranch
        def currentCommit = testRepository.getCommit(defaultBranch)
        def refStates = testRepository.refs

        expect:
        def commit = testRepository.commit("simple test commit")
        commit.parentSHA1s.contains(currentCommit.SHA1)

        cleanup:
        testRepository.resetRepositoryRefs(refStates)
    }

    def "can create a branch"() {
        given:
        def defaultBranch = testRepository.defaultBranch
        def refStates = testRepository.refs

        when:
        testRepository.createBranch("feature/new", defaultBranch)

        then:
        def result = testRepository.repository.getBranch("feature/new")
        result != null
        result.SHA1 == defaultBranch.SHA1

        cleanup:
        testRepository.resetRepositoryRefs(refStates)
    }

    def "can create a tag"() {
        given:
        def defaultBranch = testRepository.defaultBranch
        def currentCommit = testRepository.getCommit(defaultBranch)
        def refStates = testRepository.refs

        expect:
        def tag = testRepository.tag("customTag")
        tag.commit.SHA1 == currentCommit.SHA1

        cleanup:
        testRepository.resetRepositoryRefs(refStates)
    }

    def "can create a release"() {
        given:
        def refStates = testRepository.refs

        when:
        testRepository.createRelease(releaseName, tagName)

        then:
        def release = testRepository.repository.getReleaseByTagName(tagName)
        release.name == releaseName

        cleanup:
        testRepository.resetRepositoryRefs(refStates)
        testRepository.cleanupReleases()

        where:
        tagName  | releaseName
        "v1.0.0" | "release 1.0.0"
    }

    def "can create a pull request"() {
        given:
        def refStates = testRepository.refs

        when:
        testRepository.setupPullRequestWithFileChange(pullRequestName, body, branchName, fileName, fileContent)

        then:
        List<GHPullRequest> pullRequests = testRepository.repository.queryPullRequests().state(GHIssueState.OPEN).list().collect()
        pullRequests.size() == 1
        pullRequests[0].listFiles().size() == 1
        pullRequests[0].body == body

        cleanup:
        testRepository.resetRepositoryRefs(refStates)
        testRepository.cleanupPullRequests()

        where:
        pullRequestName | body     | branchName     | fileName  | fileContent
        "Test PR"       | "a test" | "test" | "test.md" | "test"
    }
}
