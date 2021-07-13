package com.wooga.spock.extensions.github

import spock.lang.Specification

import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy

class RepositoryFactorySpec extends Specification{

    def "creates new repository"() {
        given: "some repository metadata"
        def baseName = "basereponame"
        def defaultMetadata = proxyDefaultMetadata(usernameEnv: "ATLAS_GITHUB_INTEGRATION_USER",
                                                    tokenEnv: "ATLAS_GITHUB_INTEGRATION_PASSWORD")

        when: "creating new repo with same name as existing repository"
        def newRepo = new RepositoryFactory(defaultMetadata).setupRepository(baseName)

        then: "repository is created successfully"
        newRepo.exists()

        cleanup:
        tryToDelete(newRepo)
    }


    def "deletes existing repository when creating new repository with existing name"() {
        given: "some repository metadata"
        def baseName = "basereponame"
        def defaultMetadata = proxyDefaultMetadata(usernameEnv: "ATLAS_GITHUB_INTEGRATION_USER",
                                                    tokenEnv: "ATLAS_GITHUB_INTEGRATION_PASSWORD")
        and: "a existing repository"
        def existingRepo = new RepositoryFactory(defaultMetadata).setupRepository(baseName)

        when: "creating new repo with same name as existing repository"
        def newRepo = new RepositoryFactory(defaultMetadata).setupRepository(baseName)

        then: "repository is created successfully"
        newRepo.exists()
        and: "previous existing repository is deleted"
        !existingRepo.exists()

        cleanup:
        tryToDelete(existingRepo)
        tryToDelete(newRepo)
    }

    boolean tryToDelete(Repository repo) {
        try {
            repo?.delete()
            return true
        } catch(FileNotFoundException) {
            return false
        }
    }

    GithubRepository proxyDefaultMetadata(Map<String, Object> overrides = new HashMap<>()) {
        Class[] proxyInterface = [GithubRepository.class]
        GithubRepository repo = Proxy.newProxyInstance(getClass().classLoader, proxyInterface, new InvocationHandler() {
            @Override
            Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                if(overrides.containsKey(method.name)) {
                    return overrides[method.name]
                }
                return method.defaultValue
            }
        }) as GithubRepository
        return repo
    }
}
