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
