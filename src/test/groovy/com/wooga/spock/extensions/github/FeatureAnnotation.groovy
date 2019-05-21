package com.wooga.spock.extensions.github


import org.junit.Rule
import org.junit.contrib.java.lang.system.EnvironmentVariables
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

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
