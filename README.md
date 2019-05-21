spock-github-extension
======================

[![Coverage Status](https://coveralls.io/repos/github/wooga/spock-github-extension/badge.svg?branch=master)](https://coveralls.io/github/wooga/spock-github-extension?branch=master)

@GithubRepository
-----------------

Used on a `Repository` property or feature method this annotation will cause a test github repository to be created and injected before each feature method.
If the field is `@Shared` the repository is only deleted after all feature methods have run. You can have has many such fields as you like in a single spec.

### Example

```groovy
class ExampleSpec extends Specification {
    @Shared
    @GithubRepository
    Repository testRepo

    def "create new pull request"() {
        given:
        testRepo.createBranch("feature/1")
        testRepo.commit("empty commit", "feature/1")

        when:
        testRepo.createPullRequest("Test PR", "feature/1", testRepo.defaultBranch.name,"A pull request")

        then:
        testRepo.queryPullRequests().state(GHIssueState.OPEN).list().collect().size() == 1
    }
}
```

It is also possible to inject a test repository directly into a feature method

```groovy
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
```

The `@GithubRepository` supports the following parameters:

| name                      | default                          | description                                                                                         |
| ------------------------- | -------------------------------- | --------------------------------------------------------------------------------------------------- |
| usernameEnv               | `GITHUB_USERNAME`                | Sets the name of the environment variable which holds the github username.                          | 
| tokenEnv                  | `GITHUB_TOKEN`                   | Sets the name of the environment variable which holds the github token.                             |
| repositoryNamePrefix      | `test-repo`                      | Sets the prefix name for the to be created repository.                                              |
| repositoryPostFixProvider | `[EmptyRepositoryPostFix.class]` | Sets an array of {@link RepositoryPostFix} classes which are used to generate a postfix name value. |
| repositoryDescription     | `Integration test repo`          | Sets a short description text for the repository.                                                   |
| endpoint                  | `https://api.github.com`         | Sets the github api endpoint to use.                                                                |
| rateLimitHandler          | `RateLimitHandlerFail.class`     | Sets a rate limit handler to use.                                                                   |
| resetAfterTestCase        | `false`                          | Instructs the resource to reset the repository to the state before the test feature was executed.   |
| createPrivateRepository   | `false`                          | A flag indicating if the to be created repository is private.                                       |
| setupIssues               | `false`                          | A flag indicating if an issue tracker should be enabled.                                            |
| setupWiki                 | `false`                          | A flag indicating if a project wiki should be enabled.                                              |
| enableDownloads           | `false`                          | A flag indicating if downloads will be enabled.                                                     |
| allowRebaseMerge          | `true`                           | A flag indicating if rebase commits are allowed.                                                    |
| allowMergeCommit          | `true`                           | A flag indicating if merge commits are allowed.                                                     |
| allowSquashMerge          | `true`                           | A flag indicating if squash commits are allowed.                                                    |
| autoInit                  | `true`                           | A flag indicating if an initial commit with a simple README.md file will be created.                |
| gitignoreTemplate         | ``                               | Sets a name of a {@code gitignore template}.                                                        |
| licenseTemplate           | `MIT`                            | Sets a name of a {@code license template} to use.                                                   |

LICENSE
=======

Copyright 2018 Wooga GmbH

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

<http://www.apache.org/licenses/LICENSE-2.0>

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.


[unity-version-manager]: github.com/Larusso/unity-version-manager
