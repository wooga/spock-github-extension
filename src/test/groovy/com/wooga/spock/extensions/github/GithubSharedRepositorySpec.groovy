package com.wooga.spock.extensions.github

import spock.lang.Shared
import spock.lang.Unroll

class GithubSharedRepositorySpec extends BaseGithubRepositorySpec {

    @Shared
    @GithubRepository(
            usernameEnv = "ATLAS_GITHUB_INTEGRATION_USER",
            tokenEnv = "ATLAS_GITHUB_INTEGRATION_PASSWORD",
            resetAfterTestCase = false,
            repositoryNamePrefix = "shared-test-repo"
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
    def "keeps state after each example: #message"() {
        given:
        testRepository.commit("Commit one")
        testRepository.commit("Commit two")

        expect:
        testRepository.repository.queryCommits().list().collect().size() == commits

        where:
        commits << [3,5]
        message << ['initial', 'second run']
    }
}
