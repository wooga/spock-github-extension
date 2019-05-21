package com.wooga.spock.extensions.github.interceptor

import com.wooga.spock.extensions.github.GithubRepository
import com.wooga.spock.extensions.github.Repository
import com.wooga.spock.extensions.github.api.RepositoryPostFix
import groovy.transform.InheritConstructors
import org.kohsuke.github.GHRepository
import org.kohsuke.github.GitHub
import org.kohsuke.github.GitHubBuilder
import org.spockframework.runtime.extension.AbstractMethodInterceptor
import org.spockframework.runtime.extension.IMethodInvocation
import org.spockframework.runtime.model.NodeInfo

@InheritConstructors
abstract class GithubRepositoryManagingInterceptor<T extends NodeInfo> extends AbstractMethodInterceptor {

    protected final GithubRepository metadata

    private GitHub client
    private String repositoryName
    private String repositoryOwner
    private String token
    private T info

    T getInfo() {
        this.info
    }

    GithubRepositoryManagingInterceptor(GithubRepository metadata) {
        super()
        this.metadata = metadata
    }

    void install(T info) {
        this.info = info
    }

    void maybeDelete(String repoName) {
        try {
            def repository = getClient().getRepository(repoName)
            repository.delete()
        }
        catch (Exception e) {
        }
    }

    String getRepositoryBaseName() {
        info.name.replaceAll(/\s|\[|\]|\(|\)|\{|\}/, '-')
    }

    String getRepositoryName() {
        if (!repositoryName) {

            String prefix = metadata.repositoryNamePrefix()
            List<RepositoryPostFix> postFixProvider = metadata.repositoryPostFixProvider().collect {
                it.getDeclaredConstructor().newInstance()
            } as List<RepositoryPostFix>

            String postfix = postFixProvider.inject("") { String postFix, provider ->
                String post = provider.postFix
                if (post && !post.empty) {
                    postFix += "-${provider.postFix}"
                }
                postFix
            }

            repositoryName = "${getRepositoryBaseName()}${postfix}"

            if(prefix) {
                repositoryName = "${prefix}-${repositoryName}"
            }
        }

        repositoryName
    }

    String getRepositoryFullName() {
        "${getRepositoryOwner()}/${getRepositoryName()}"
    }

    String getRepositoryOwner() {
        if (!repositoryOwner) {
            repositoryOwner = System.getenv(metadata.usernameEnv())
        }

        repositoryOwner
    }

    String getToken() {
        if (!token) {
            token = System.getenv(metadata.tokenEnv())
        }

        token
    }

    GitHub getClient() {
        if (!client) {
            def builder = new GitHubBuilder()
            client = builder.withOAuthToken(System.getenv()[metadata.tokenEnv()])
                    .withEndpoint(metadata.endpoint())
                    .withRateLimitHandler(metadata.rateLimitHandler().getDeclaredConstructor().newInstance())
                    .build()
        }

        client
    }

    Repository setupRepository(IMethodInvocation invocation) {
        maybeDelete(getRepositoryFullName())

        //create github repo
        def builder = getClient().createRepository(getRepositoryName())
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

    abstract void destroyRepository(IMethodInvocation invocation)

    abstract void resetRepository(IMethodInvocation invocation)

    abstract void captureResetRef(IMethodInvocation invocation)
}