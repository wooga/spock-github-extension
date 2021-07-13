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


import com.wooga.spock.extensions.github.api.RepositoryPostFix
import groovy.transform.InheritConstructors
import org.kohsuke.github.GHRepository
import org.kohsuke.github.GitHub
import org.kohsuke.github.GitHubBuilder
import org.spockframework.runtime.model.NodeInfo

@InheritConstructors
class RepositoryFactory {

    protected final GithubRepository metadata

    private GitHub client
    private String repositoryOwner
    private String token

    static String getRepositoryBaseName(NodeInfo info) {
        info.name.replaceAll(/\s|\[|\]|\(|\)|\{|\}/, '-')
    }

    RepositoryFactory(GithubRepository metadata) {
        this.metadata = metadata
    }

    Repository setupRepository(String repositoryBaseName) {
        def repositoryName = createRepositoryName(repositoryBaseName)
        deleteRepoIfExists(getRepositoryFullName(repositoryName))

        //create github repo
        def builder = getClient().createRepository(repositoryName)
        builder.description(metadata.repositoryDescription())
        builder.autoInit(metadata.autoInit())

        if (metadata.autoInit()) {
            builder.licenseTemplate(metadata.licenseTemplate())
            if (metadata.gitignoreTemplate()) {
                builder.gitignoreTemplate(metadata.gitignoreTemplate())
            }
        }

        builder.private_(metadata.createPrivateRepository())
        builder.issues(metadata.setupIssues())
        builder.wiki(metadata.setupWiki())
        builder.allowSquashMerge(metadata.allowSquashMerge())
        builder.allowRebaseMerge(metadata.allowRebaseMerge())
        builder.allowMergeCommit(metadata.allowMergeCommit())
        builder.downloads(metadata.enableDownloads())


        GHRepository repository = builder.create()
        new Repository(repository, getClient(), getRepositoryOwner(), getToken())
    }

    protected void deleteRepoIfExists(String repoName) {
        try {
            def repository = getClient().getRepository(repoName)
            repository?.delete()
        }
        catch (FileNotFoundException e) {
        }
    }

    protected String createRepositoryName(String repositoryBaseName) {
        String prefix = metadata.repositoryNamePrefix()
        List<RepositoryPostFix> postFixProvider = metadata.repositoryPostFixProvider().collect {
            it.getDeclaredConstructor().newInstance()
        } as List<RepositoryPostFix>

        String postfix = postFixProvider.inject("") { String postFix, provider ->
            String post = provider.postFix
            if (post && !post.empty) {
                postFix += "-${provider.postFix}"
            }
            return postFix
        }

        def repositoryName = "${repositoryBaseName}${postfix}"
        if(prefix) {
            repositoryName = "${prefix}-${repositoryName}"
        }
        return repositoryName
    }

    protected String getRepositoryFullName(String repositoryName) {
        return "${getRepositoryOwner()}/${repositoryName}"
    }

    protected String getRepositoryOwner() {
        if (!repositoryOwner) {
            repositoryOwner = getClient().myself.login
        }
        repositoryOwner
    }

    protected String getToken() {
        if (!token) {
            token = mustGetEnvVar(metadata.tokenEnv())
        }
        token
    }

    protected GitHub getClient() {
        if (!client) {
            def builder = new GitHubBuilder()
            client = builder.withOAuthToken(getToken())
                    .withEndpoint(metadata.endpoint())
                    .withRateLimitHandler(metadata.rateLimitHandler().getDeclaredConstructor().newInstance())
                    .build()
        }
        return client
    }

    private static String mustGetEnvVar(String envVar){
        def value = System.getenv(envVar)
        if (!value) {
            throw new IllegalArgumentException("Couldn't find environment variable ${envVar}")
        }
        return value
    }


}