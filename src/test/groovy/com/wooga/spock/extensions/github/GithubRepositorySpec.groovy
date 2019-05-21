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
