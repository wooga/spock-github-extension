package com.wooga.spock.extensions.github

import com.wooga.spock.extensions.github.api.EmptyRepositoryPostFix
import com.wooga.spock.extensions.github.api.RepositoryPostFix
import spock.lang.Specification

class PostFixProviderSpec extends Specification {

    static class APostfix extends RepositoryPostFix {
        @Override
        String getPostFix() {
            return "A"
        }
    }

    static class BPostfix extends RepositoryPostFix {
        @Override
        String getPostFix() {
            return "B"
        }
    }

    static class CPostfix extends RepositoryPostFix {
        @Override
        String getPostFix() {
            return "C"
        }
    }

    @GithubRepository(
            usernameEnv = "ATLAS_GITHUB_INTEGRATION_USER",
            tokenEnv = "ATLAS_GITHUB_INTEGRATION_PASSWORD",
            repositoryPostFixProvider = [APostfix, BPostfix, CPostfix]
    )
    def "can specify a list of postfix provider"(Repository repo) {
        expect:
        repo.name.matches(/test-repo-.*-A-B-C/)
    }

    @GithubRepository(
            usernameEnv = "ATLAS_GITHUB_INTEGRATION_USER",
            tokenEnv = "ATLAS_GITHUB_INTEGRATION_PASSWORD",
            repositoryPostFixProvider = [APostfix, EmptyRepositoryPostFix, BPostfix, CPostfix]
    )
    def "skips empty postfix provider"(Repository repo) {
        expect:
        repo.name.matches(/test-repo-.*-A-B-C/)
    }
}
