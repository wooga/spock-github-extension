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
        void delete()
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
        resetRefs = tryGetRefs()
    }

    void resetRepository() {
        resetRepositoryRefs()
        cleanupPullRequests()
        cleanupReleases()
    }

    boolean exists() {
        try {
            client.getRepositoryById(repository.id)
            return true
        } catch (FileNotFoundException) {
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
        def email = repository.owner.email ?: "test@gmail.com"
        commitBuilder.author(repository.owner.name, email, new Date())
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
        tag(tagName, branch.SHA1)
    }

    GHTag tag(String tagName, String ref) {
        repository.createRef("refs/tags/$tagName", ref)
        findTag(tagName).orElse(null)
    }

    void delete(int retries=3) {
        try {
            if(this.exists()) {
                repository.delete()
            }
        } catch (FileNotFoundException _) {
        } catch (Exception e) {
            if(retries > 0) {
                sleep(500)
                delete(retries-1)
            } else {
                throw e
            }
        }

    }

    Optional<GHTag> findTag(String tagName) {
        //there is no other way at the moment to load a GHTag
        Optional.ofNullable(listTags().find { it.name == tagName } as GHTag)
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

    void resetRepositoryRefs(GHRef[] refsToReset) {
        Map<String, GHRef> capturedRefs = refsToReset.inject(new HashMap<String, GHRef>()) { memo, item ->
            memo[item.ref] = item
            memo
        }

        this.tryGetRefs().each {
            if(it.ref.startsWith("refs/pull")) { //why the hell github puts pull requests as refs??
                return
            }
            GHRef oldState = capturedRefs[it.ref]
            if (oldState) {
                it.updateTo(oldState.object.sha, true)
            } else {
                tryToDelete(it)
            }
        }
    }

    GHRef[] tryGetRefs() {
        try {
            return repository.refs
        } catch (HttpException e) {
            if (!e.message.contains("Git Repository is empty.")) {
                throw e
            }
            return []
        }
    }



    private static void tryToDelete(GHRef ref) {
        try {
            ref.delete()
        } catch (ignored) {
        }
    }
}
