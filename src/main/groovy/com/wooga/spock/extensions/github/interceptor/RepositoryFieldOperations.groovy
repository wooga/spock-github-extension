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


import com.wooga.spock.extensions.github.Repository
import com.wooga.spock.extensions.github.RepositoryFactory
import groovy.transform.InheritConstructors
import org.spockframework.runtime.extension.IMethodInvocation
import org.spockframework.runtime.model.FieldInfo
import spock.lang.Specification

@InheritConstructors
class RepositoryFieldOperations {

    RepositoryFactory repoFactory
    FieldInfo info

    RepositoryFieldOperations(FieldInfo info, RepositoryFactory repoFactory) {
        this.info = info
        this.repoFactory = repoFactory
    }

    Repository setupRepository(IMethodInvocation invocation) {
        def repoBaseName = RepositoryFactory.getRepositoryBaseName(info)
        def repo = repoFactory.setupRepository(repoBaseName)
        def spec = getSpec(invocation)
        info.writeValue(spec, repo)
        return repo
    }

    void destroyRepository(IMethodInvocation invocation) {
        Repository repository = getRepository(invocation)
        repository.delete()
    }

    void resetRepository(IMethodInvocation invocation) {
        Repository repository = getRepository(invocation)
        repository.resetRepository()
    }

    void captureResetRef(IMethodInvocation invocation) {
        Repository repository = getRepository(invocation)
        repository.captureResetRefs()
    }

    protected Repository getRepository(IMethodInvocation invocation) {
        final specInstance = getSpec(invocation)
        info.readValue(specInstance) as Repository
    }

    protected Specification getSpec(IMethodInvocation invocation) {
        ((info.shared) ? invocation.sharedInstance : invocation.instance) as Specification
    }
}
