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
import org.kohsuke.github.GHBranch
import org.kohsuke.github.GHCommit
import org.kohsuke.github.GHRef
import org.kohsuke.github.GHRepository
import spock.lang.Retry
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Subject

@Retry(delay = 2000, count = 2, mode = Retry.Mode.SETUP_FEATURE_CLEANUP)
class RepositorySpec extends Specification {

    @Rule
    public final EnvironmentVariables environmentVariables = new EnvironmentVariables()

    def setup() {
        environmentVariables.set("GITHUB_USERNAME", System.getenv("ATLAS_GITHUB_INTEGRATION_USER"))
        environmentVariables.set("GITHUB_TOKEN", System.getenv("ATLAS_GITHUB_INTEGRATION_PASSWORD"))

        reference = test.repository
        subject = new Repository(test.repository, test.client, test.userName, test.token)
    }

    @Subject
    Repository subject

    @Shared
    @GithubRepository(
                        usernameEnv = "ATLAS_GITHUB_INTEGRATION_USER",
                        tokenEnv = "ATLAS_GITHUB_INTEGRATION_PASSWORD",
                        resetAfterTestCase = true
    )
    Repository test
    GHRepository reference

    def "can capture current ref state for later reset"() {
        given: "the current ref state"
        def refs = reference.getRefs()

        and: "a captured state"
        subject.captureResetRefs()

        and: "some commits"
        subject.commit("commit 1")
        subject.commit("commit 2")

        when:
        subject.resetRepository()

        then:
        subject.getRefs().collect{it.ref} == refs.collect {it.ref}
    }



    def "convenient method getBranchRef fetches ref of branch"() {
        given: "the default branch name"
        String branchName = reference.defaultBranch

        and: "the ref of the master branch"
        GHRef expectedRef = reference.getRef("heads/${branchName}")

        expect:
        subject.getBranchRef(branchName).ref == expectedRef.ref
    }

    def "convenient method getDefaultBranch returns default branch as GHBranch object"() {
        given: "the default branch name"
        String branchName = reference.defaultBranch

        and: "the current branch ref"
        GHRef branchRef = reference.getRef("heads/${branchName}")

        when:
        def branch = subject.getDefaultBranch()

        then:
        noExceptionThrown()
        branch != null
        branch.name == branchName
        branch.SHA1 == branchRef.object.sha
    }

    def "convenient method getCommit returns commit from provided GHBranch object"() {
        given: "the default branch name"
        String branchName = reference.defaultBranch

        and: "the branch object"
        GHBranch branch = reference.getBranch(branchName)

        and: "the branch sha"
        String sha1 = branch.SHA1

        and: "a commit object from sha1"
        GHCommit exptectedCommit = reference.getCommit(sha1)

        when:
        GHCommit commit = subject.getCommit(branch)

        then:
        noExceptionThrown()
        commit != null
        commit.SHA1 == exptectedCommit.SHA1
    }

    def "convenient method createBranch(String) creates branch from defaultBranch"() {
        given: "the default branch name"
        String branchName = reference.defaultBranch

        and: "the branch object"
        GHBranch branch = reference.getBranch(branchName)

        and: "the branch sha"
        String sha1 = branch.SHA1

        when:
        GHBranch newBranch = subject.createBranch("custom/branch")

        then:
        newBranch.SHA1 == sha1
    }

    def "convenient method createBranch(String, GHBranch) creates branch from provided branch"() {
        given: "the default branch name"
        String branchName = reference.defaultBranch

        and: "the branch object"
        GHBranch branch = reference.getBranch(branchName)

        and: "a new custom branch to branch off from"
        reference.createRef("refs/heads/custom/base1", branch.SHA1)
        test.commit("test commit", "custom/base1")
        GHBranch base = reference.getBranch("custom/base1")

        when:
        GHBranch newBranch = subject.createBranch("custom/branch2", base)

        then:
        newBranch.SHA1 == base.SHA1
    }

    def "convenient method createBranch(String, GHCommit) creates branch from provided branch"() {
        given: "the default branch name"
        String branchName = reference.defaultBranch

        and: "the branch object"
        GHBranch branch = reference.getBranch(branchName)

        and: "a new custom branch to branch off from"
        reference.createRef("refs/heads/custom/base2", branch.SHA1)
        GHCommit base = test.commit("test commit", "custom/base2")

        when:
        GHBranch newBranch = subject.createBranch("custom/branch3", base)

        then:
        newBranch.SHA1 == base.SHA1
    }

    def "convenient method commit(String) creates commit on default branch HEAD"() {
        given: "the default branch name"
        String branchName = reference.defaultBranch

        and: "current default branch SHA"
        String parentSha = reference.getBranch(branchName).SHA1

        when:
        GHCommit commit = subject.commit(message)

        then:
        commit != null
        commit.commitShortInfo.message == message
        commit.parentSHA1s == [parentSha]
        commit.SHA1 == reference.getBranch(branchName).SHA1

        where:
        message = "My Test Message"
    }

    def "convenient method commit(String, String) creates commit on provided branch HEAD"() {
        given: "the default branch name"
        String branchName = reference.defaultBranch

        and: "current default branch SHA"
        String parentSha = reference.getBranch(branchName).SHA1

        when:
        GHCommit commit = subject.commit(message, branchName)

        then:
        commit != null
        commit.commitShortInfo.message == message
        commit.parentSHA1s == [parentSha]
        commit.SHA1 == reference.getBranch(branchName).SHA1

        where:
        message = "My Test Message"
    }
}
