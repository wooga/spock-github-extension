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

package com.wooga.spock.extensions.github.interceptor

import com.wooga.spock.extensions.github.GithubRepository
import com.wooga.spock.extensions.github.Repository
import com.wooga.spock.extensions.github.RepositoryFactory
import groovy.transform.InheritConstructors
import org.spockframework.runtime.extension.AbstractMethodInterceptor
import org.spockframework.runtime.extension.IMethodInvocation
import org.spockframework.runtime.model.FeatureInfo
import org.spockframework.runtime.model.MethodInfo

import java.lang.reflect.Parameter

@InheritConstructors
class GithubRepositoryFeatureInterceptor extends AbstractMethodInterceptor {

    private RepositoryFactory factory
    private GithubRepository metadata
    private Repository repo;

    static GithubRepositoryFeatureInterceptor withMetadata(GithubRepository metadata) {
        def repoFactory = new RepositoryFactory(metadata)
        return new GithubRepositoryFeatureInterceptor(metadata, repoFactory)
    }

    GithubRepositoryFeatureInterceptor(GithubRepository metadata, RepositoryFactory factory) {
        this.metadata = metadata
        this.factory = factory
    }
    //Spock 2 does away with the unpredictable argument array size
    //https://spockframework.org/spock/docs/2.3/extensions.html#_injecting_method_parameters
    private static void injectRepository(IMethodInvocation invocation, Repository repo) {
        Map<Parameter, Integer> parameters = [:]
        invocation.method.reflection.parameters.eachWithIndex { parameter, i ->
            parameters << [(parameter): i]
        }

        parameters.findAll { Repository.equals it.key.type}
        .each { parameter, i ->
            if(!invocation.arguments[i] || invocation.arguments[i] == MethodInfo.MISSING_ARGUMENT) {
                invocation.arguments[i] = repo
            }
        }
    }

    //Spock 2 does away with the unpredictable argument array size, keeping it still for now for spock 1 compatibility
//    private static void injectRepository(IMethodInvocation invocation, Repository repo) {
//        Map<Parameter, Integer> parameters = [:]
//        invocation.method.reflection.parameters.eachWithIndex { parameter, i ->
//            parameters << [(parameter): i]
//        }
//        parameters = parameters.findAll { Repository.equals it.key.type}
//
//        // enlarge arguments array if necessary
//        def lastMyInjectableParameterIndex = parameters*.value.max()
//        lastMyInjectableParameterIndex = lastMyInjectableParameterIndex == null ?
//                0 :
//                lastMyInjectableParameterIndex + 1
//
//        if (invocation.arguments.length < lastMyInjectableParameterIndex) {
//            def newArguments = new Object[lastMyInjectableParameterIndex]
//            System.arraycopy invocation.arguments, 0, newArguments, 0, invocation.arguments.length
//            invocation.arguments = newArguments
//        }
//
//        parameters.each { parameter, i ->
//            if(!(invocation.arguments[i]) || invocation.arguments[i] == MethodInfo.MISSING_ARGUMENT) {
//                invocation.arguments[i] = repo
//            }
//        }
//    }

    //execute feature
    @Override
    void interceptFeatureMethod(IMethodInvocation invocation) throws Throwable {
        injectRepository(invocation, repo)
        invocation.proceed()
    }

    //NEW ITERATION
    @Override
    void interceptIterationExecution(IMethodInvocation invocation) throws Throwable {
        try {
            invocation.proceed()
        }
        finally {
            if (metadata.resetAfterTestCase()) {
                repo.resetRepository()
            }
        }
    }

    @Override
    void interceptSetupMethod(IMethodInvocation invocation) throws Throwable {
        invocation.proceed()
        setupRepository(invocation.feature)
        invocation.spec.setupInterceptors.remove(this)
    }

    //SETUP FEATURE
    @Override
    void interceptFeatureExecution(IMethodInvocation invocation) throws Throwable {
        invocation.spec.addSetupInterceptor(this)
        try {
            invocation.proceed()
        }
        finally {
            repo.delete()
        }
    }

    void install(FeatureInfo info) {
        info.addInterceptor(this)
        info.addIterationInterceptor(this)
        info.featureMethod.addInterceptor(this)
    }

    Repository setupRepository(FeatureInfo info) {
        def repoBaseName = RepositoryFactory.getRepositoryBaseName(info)
        this.repo = factory.setupRepository(repoBaseName)
        repo.captureResetRefs()
        return repo
    }
}
