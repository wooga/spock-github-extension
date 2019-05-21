package com.wooga.spock.extensions.github


import org.kohsuke.github.*

import static org.kohsuke.github.GHPullRequest.MergeMethod.MERGE

/**
 * A utility wrapper around {@code org.kohsuke.github.GHRepository} and {@code org.kohsuke.github.Github}.
 * <p>
 * This class wraps all methods of {@code org.kohsuke.github.GHRepository} and exposes a few handy utility methods
 * to write simple test setups.
 */
class Repository {

    private interface GHRepositoryDelegateExcludes {
        String getDefaultBranch()
    }

    @Delegate(excludeTypes = [GHRepositoryDelegateExcludes.class])
    final GHRepository repository

    final GitHub client
    final String userName
    final String token
    private GHRef[] resetRefs

    Repository(GHRepository repository, GitHub client, String userName, String token) {
        this.repository = repository
        this.client = client
        this.userName = userName
        this.token = token
    }

    void captureResetRefs() {
        try {
            resetRefs = repository.refs
        } catch(HttpException e) {
            if(!e.message.contains("Git Repository is empty.")) {
                throw e
            }
            resetRefs = []
        }
    }

    void resetRepository() {
        resetRepositoryRefs()
        cleanupPullRequests()
        cleanupReleases()
    }

    boolean exists() {
        try {
            client.getRepository(fullName)
            return true
        } catch (ignored) {
            return false
        }
    }

    GHRef getBranchRef(String branchName) {
        getRef("heads/$branchName")
    }

    GHBranch getDefaultBranch() {
        getBranch(repository.defaultBranch)
    }

    GHCommit getCommit(GHBranch branch) {
        getCommit(branch.SHA1)
    }

    GHBranch createBranch(String branchName) {
        createBranch(branchName, defaultBranch)
    }

    GHBranch createBranch(String branchName, GHBranch base) {
        createBranch(branchName, base.SHA1)
    }

    GHBranch createBranch(String branchName, GHCommit base) {
        createBranch(branchName, base.SHA1)
    }

    GHBranch createBranch(String branchName, String ref) {
        createRef("refs/heads/$branchName", ref)
        getBranch(branchName)
    }

    GHCommit commit(String message, String branchName) {
        GHBranch branch = getBranch(branchName)
        GHCommit commit = repository.getCommit(branch.getSHA1())

        def commitBuilder = repository.createCommit()
        commitBuilder.message(message)
        commitBuilder.tree(commit.tree.getSha())
        commitBuilder.parent(commit.getSHA1())
        commitBuilder.author(repository.owner.name, repository.owner.email, new Date())
        GHCommit newCommit = commitBuilder.create()

        GHRef branchRef = getBranchRef(branchName)
        branchRef.updateTo(newCommit.SHA1)

        newCommit
    }

    GHCommit commit(String message) {
        commit(message, repository.defaultBranch)
    }

    GHTag tag(String tagName) {
        GHBranch branch = defaultBranch
        tag(tagName, branch)
    }

    GHTag tag(String tagName, GHBranch branch) {
        tag(tagName, branch.SHA1)
    }

    GHTag tag(String tagName, GHCommit commit) {
        tag(tagName, commit.getSHA1())
    }

    GHTag tag(String tagName, String ref) {
        repository.createRef("refs/tags/$tagName", ref)
        //there is no other way at the moment to load a GHTag
        listTags().find { it.name == tagName } as GHTag
    }

    GHPullRequest createPullRequest(String title, String head) {
        createPullRequest(title, head, repository.defaultBranch, "")
    }

    GHPullRequest setupPullRequestWithFileChange(String title, String body, String branchName) {
        setupPullRequestWithFileChange(title, body, branchName, branchName.replace('/', '_'), body)
    }

    GHPullRequest setupPullRequestWithFileChange(String title, String body, String branchName, String changedFileName, String fileContent) {
        final String refName = "refs/heads/$branchName"

        GHBranch defaultBranch = defaultBranch

        createRef(refName, defaultBranch.getSHA1())
        def contentBuilder = createContent()
        contentBuilder.path(changedFileName)

        try {
            def content = getFileContent(changedFileName, defaultBranch.getSHA1())
            contentBuilder.sha(content.getSha())
        } catch (GHFileNotFoundException ignored) {
        }

        contentBuilder.branch(branchName)
        contentBuilder.content(fileContent)
        contentBuilder.message("test file ${branchName}")
        contentBuilder.commit()

        createPullRequest(title, branchName, defaultBranch.name, body)
    }

    void mergePullRequest(GHPullRequest pr, String message, GHPullRequest.MergeMethod mergeMethod = MERGE) {
        pr.merge(message, pr.head.sha, mergeMethod)
    }

    void mergePullRequest(GHPullRequest pr, GHPullRequest.MergeMethod mergeMethod = MERGE) {
        pr.merge(null, pr.head.sha, mergeMethod)
    }

    GHRelease createRelease(String name, String tagName) {
        createRelease(name, tagName, repository.defaultBranch)
    }

    GHRelease createRelease(String name, String tagName, String commitish) {
        def releaseBuilder = createRelease(tagName ? tagName : name)
        releaseBuilder.name(name)
        releaseBuilder.draft(false)
        releaseBuilder.prerelease(false)
        releaseBuilder.commitish(commitish)
        releaseBuilder.create()
    }

    void cleanupReleases() {
        try {
            PagedIterable<GHRelease> releases = listReleases()
            releases.each {
                it.delete()
            }
        }
        catch (Error ignored) {

        }
    }

    void cleanupPullRequests() {
        PagedIterable<GHPullRequest> pullRequests = queryPullRequests().list()
        pullRequests.each {
            it.close()
        }
    }

    void resetRepositoryRefs() {
        resetRepositoryRefs(resetRefs)
    }

    static void resetRepositoryRefs(GHRef[] refs) {
        Map<String, GHRef> capturedRefs = refs.inject(new HashMap<String, GHRef>()) { memo, item ->
            memo[item.ref] = item
            memo
        }

        GHRef[] currentRefs = refs

        currentRefs.each {
            GHRef oldState = capturedRefs[it.ref]
            if (oldState) {
                it.updateTo(oldState.object.sha, true)
            } else {
                try {
                    it.delete()
                } catch (ignored) {
                }
            }
        }
    }
}
