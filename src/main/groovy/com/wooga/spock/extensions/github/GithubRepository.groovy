package com.wooga.spock.extensions.github

import com.wooga.spock.extensions.github.api.EmptyRepositoryPostFix
import com.wooga.spock.extensions.github.api.RateLimitHandlerFail
import com.wooga.spock.extensions.github.api.RepositoryPostFix
import org.kohsuke.github.RateLimitHandler
import org.spockframework.runtime.extension.ExtensionAnnotation

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

/**
 *  Test fields or feature methods marked with this annotation will trigger the creation of a github test repository
 *  resource with the provided properties.
 *  <p>
 *  The extensions will create and setup a GitHub repository and inject a {@link Repository} object into the test field
 *  or feature method.
 *
 *  @see Repository
 */
@Retention(RetentionPolicy.RUNTIME)
@Target([ElementType.FIELD, ElementType.METHOD])
@ExtensionAnnotation(GithubRepositoryExtension.class)
@interface GithubRepository {
    /**
     * Returns the name of the environment variable which holds the github username.
     *
     * @return a {@code String} value with the github username environment variable name.
     */
    String usernameEnv() default "GITHUB_USERNAME";

    /**
     * Returns the name of the environment variable which holds the github token.
     *
     * @return a {@code String} value with the github token environment variable name.
     */
    String tokenEnv() default "GITHUB_TOKEN";

    /**
     * Returns the prefix name for the to be created repository.
     * <p>
     * The test repository name will be created by concatenating prefix
     * with the result of the {@link #repositoryPostFixProvider}.
     *
     * @see #repositoryPostFixProvider
     * @return the repository name prefix
     */
    String repositoryNamePrefix() default "test-repo";

    /**
     * Returns an array of {@link RepositoryPostFix} classes which are used to generate a postfix name value.
     * <p>
     * Each provider class will be instantiated and the {@link RepositoryPostFix#getPostFix()} executed.
     * The list of all postfix values will be concatenated with a {@code -} and attached to the {@link #repositoryNamePrefix()}.
     * Empty values will be dropped.
     *
     * @return an array of {@link RepositoryPostFix} classes.
     *
     * @see RepositoryPostFix
     * @see EmptyRepositoryPostFix
     * @see #repositoryNamePrefix()
     */
    Class<RepositoryPostFix>[] repositoryPostFixProvider() default [EmptyRepositoryPostFix.class];

    /**
     * Returns a short description text for the repository.
     *
     * @return a description text for the repository.
     */
    String repositoryDescription() default "Integration test repo";

    /**
     * Returns the github api endpoint to use.
     *
     * @return the github api endpoint to use.
     */
    String endpoint() default "https://api.github.com";

    /**
     * The kosuke github api allows to set custom rate limit handlers. The handler must be of type
     * {@code org.kohsuke.github.RateLimitHandler} and will be invoked when the user rate limit is depleted.
     *
     * @return a class of type {@code org.kohsuke.github.RateLimitHandler}
     * @see RateLimitHandlerWait
     * @see RateLimitHandlerFail
     */
    Class<RateLimitHandler> rateLimitHandler() default RateLimitHandlerFail.class;

    /**
     * Instructs the resource to reset the repository to the state before the test feature was executed.
     * <p>
     * This flag makes only sense for repositories attached {@code @Shared} fields. It makes it possible to create a
     * test repository once for the whole {@code Specification} and reset changes after each test case. Be aware that
     * issues and pull-requests are unrevertable. All issues and pull-requests will only be closed.
     *
     * @return a {@code boolean} value indicating if a {@code shared} repository should be reset after each test feature.
     */
    boolean resetAfterTestCase() default false;

    /**
     * A github repository creation flag indicating if the to be created repository
     * will be {@code private} or {@code public}.
     *
     * @return a {@code boolean} value indicating if the repository will be {@code private} (true) or {@code public} (false).
     */
    boolean createPrivateRepository() default false;

    /**
     * A github repository creation flag indicating if an issue tracker should be enabled.
     *
     * @return a {@code boolean} value indicating if an issue tracker should be enabled.
     */
    boolean setupIssues() default false;

    /**
     * A github repository creation flag indicating if a project wiki should be enabled.
     *
     * @return a {@code boolean} value indicating if a wiki should be created.
     */
    boolean setupWiki() default false;

    /**
     * A github repository creation flag indicating if downloads will be enabled.
     *
     * @return a {@code boolean} value indicating if downloads will be enabled.
     */
    boolean enableDownloads() default false;

    /**
     * A github repository creation flag indicating if rebase commits are allowed.
     *
     * @return a {@code boolean} value indicating if rebase commits are allowed.
     */
    boolean allowRebaseMerge() default true;

    /**
     * A github repository creation flag indicating if merge commits are allowed.
     *
     * @return a {@code boolean} value indicating if merge commits are allowed.
     */
    boolean allowMergeCommit() default true;

    /**
     * A github repository creation flag indicating if squash commits are allowed.
     *
     * @return a {@code boolean} value indicating if squash commits are allowed.
     */
    boolean allowSquashMerge() default true;

    /**
     * A github repository creation flag indicating if an initial commit with a simple README.md file will be created.
     *
     * @return a {@code boolean} value indicating if the repository will be initialized.
     */
    boolean autoInit() default true;

    /**
     * Returns a name of a {@code gitignore template}.
     * See https://developer.github.com/v3/repos/#create
     *
     * @return a {@code String} name of a {@code gitignore template}.
     */
    String gitignoreTemplate() default "";

    /**
     * Returns a name of a {@code license template} to use.
     * See https://developer.github.com/v3/repos/#create
     *
     * @return a {@code String} name of a {@code license template}.
     */
    String licenseTemplate() default "MIT";
}
