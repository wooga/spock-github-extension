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
import org.kohsuke.github.GHCommit
import org.kohsuke.github.GHException
import org.kohsuke.github.GHFileNotFoundException
import org.kohsuke.github.GHPullRequest
import org.kohsuke.github.HttpException
import spock.lang.Retry
import spock.lang.Specification

@Retry(delay = 2000, count = 2, mode = Retry.Mode.SETUP_FEATURE_CLEANUP)
class GithubRepositoryBuilderSpec extends Specification {

    @Rule
    public final EnvironmentVariables environmentVariables = new EnvironmentVariables()

    def setup() {
        environmentVariables.set("GITHUB_USERNAME", System.getenv("ATLAS_GITHUB_INTEGRATION_USER"))
        environmentVariables.set("GITHUB_TOKEN", System.getenv("ATLAS_GITHUB_INTEGRATION_PASSWORD"))
    }

    @GithubRepository()
    def "creates repository with default settings"(Repository repo) {
        expect: "A single commit"
        List<GHCommit> commits = repo.queryCommits().list().collect()
        commits.size() == 1
        commits.first().commitShortInfo.message == "Initial commit"

        and: "the default description"
        repo.description == "Integration test repo"

        and: "a default README.md"
        def readme = repo.getFileContent("README.md")
        String readmeContent = readme.read().text
        readmeContent.contains(repo.name)
        readmeContent.contains(repo.description)

        and: "a default MIT LICENSE"
        def license = repo.getFileContent("LICENSE")
        String licenseContent = license.read().text
        licenseContent.contains("MIT")

        and: "no .gitignore"
        when:
        repo.getFileContent(".gitignore")
        then:
        def e = thrown(GHFileNotFoundException)
        e.message.contains("Not Found")

        and: "has no issue tracker"
        repo.openIssueCount == 0

        when: "trying to open an issue"
        repo.createIssue("Test Issue").create()

        then:
        e = thrown(HttpException)
        e.message.contains("Issues are disabled for this repo")

        and: "has no wiki"
        !repo.hasWiki()

        and: "disabled downloads"
        !repo.hasDownloads()

        and: "is a private repo"
        !repo.isPrivate()

        and: "allows pull-request merge commits"
        def pr1 = repo.setupPullRequestWithFileChange("Merge PR", "", "pr/merge")

        when:
        sleep(200)
        repo.mergePullRequest(pr1, GHPullRequest.MergeMethod.MERGE)

        then:
        noExceptionThrown()

        and: "allows pull-request squash commits"
        def pr2 = repo.setupPullRequestWithFileChange("Merge PR", "", "pr/squash")

        when:
        sleep(200)
        repo.mergePullRequest(pr2, GHPullRequest.MergeMethod.SQUASH)

        then:
        noExceptionThrown()

        and: "allows pull-request rebase commits"
        def pr3 = repo.setupPullRequestWithFileChange("Merge PR", "", "pr/rebase")

        when:
        sleep(200)
        repo.mergePullRequest(pr3, GHPullRequest.MergeMethod.REBASE)

        then:
        noExceptionThrown()
    }


    @GithubRepository(autoInit = false)
    def "can set autoInit to false"(Repository repo) {
        when:
        repo.queryCommits().list().collect()

        then:
        def e = thrown(GHException)
        e.message.contains("Failed to retrieve")
    }

    @GithubRepository(createPrivateRepository = true)
    def "can create private repo"(Repository repo) {
        expect:
        repo.isPrivate()
    }

    @GithubRepository(setupWiki = true)
    def "can create repo with wiki"(Repository repo) {
        expect:
        repo.hasWiki()
    }

    @GithubRepository(enableDownloads = true)
    def "can create repo with downloads"(Repository repo) {
        expect:
        repo.hasDownloads()
    }

    @GithubRepository(setupIssues = true)
    def "can create repo with issue tracker"(Repository repo) {
        when:
        repo.createIssue("Test Issue").create()

        then:
        notThrown(GHFileNotFoundException)
    }

    @GithubRepository(licenseTemplate = "")
    def "can create repo without License"(Repository repo) {
        when:
        repo.getFileContent("LICENSE")

        then:
        def e = thrown(GHFileNotFoundException)
        e.message.contains("Not Found")
    }

    @GithubRepository(licenseTemplate = "wtfpl")
    def "can create repo with license"(Repository repo) {
        when:
        repo.getFileContent("LICENSE")

        then:
        notThrown(GHFileNotFoundException)
    }

    @GithubRepository(gitignoreTemplate = "Ruby")
    def "can create repo with custom gitignore"(Repository repo) {
        when:
        repo.getFileContent(".gitignore")

        then:
        notThrown(GHFileNotFoundException)
    }

    @GithubRepository(allowMergeCommit = false)
    def "can disable merge commits"(Repository repo) {
        given:
        def pr1 = repo.setupPullRequestWithFileChange("Merge PR", "", "pr/merge")

        when:
        sleep(200)
        repo.mergePullRequest(pr1, GHPullRequest.MergeMethod.MERGE)

        then:
        def e = thrown(HttpException)
        e.message.contains("Merge commits are not allowed on this repository.")
    }

    @GithubRepository(allowSquashMerge = false)
    def "can disable squash commits"(Repository repo) {
        given:
        def pr1 = repo.setupPullRequestWithFileChange("Merge PR", "", "pr/squash")

        when:
        sleep(200)
        repo.mergePullRequest(pr1, GHPullRequest.MergeMethod.SQUASH)

        then:
        def e = thrown(HttpException)
        e.message.contains("Squash merges are not allowed on this repository.")
    }

    @GithubRepository(allowRebaseMerge = false)
    def "can disable rebase commits"(Repository repo) {
        given:
        def pr1 = repo.setupPullRequestWithFileChange("Merge PR", "", "pr/rebase")

        when:
        sleep(200)
        repo.mergePullRequest(pr1, GHPullRequest.MergeMethod.REBASE)

        then:
        def e = thrown(HttpException)
        e.message.contains("Rebase merges are not allowed on this repository.")
    }
}
