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


import groovy.transform.InheritConstructors
import org.spockframework.runtime.extension.IMethodInvocation
import org.spockframework.runtime.model.FieldInfo
import org.spockframework.runtime.model.SpecInfo

@InheritConstructors
class GithubRepositoryInterceptor extends GithubRepositoryFieldInterceptor {

    @Override
    void interceptSetupMethod(IMethodInvocation invocation) {
        setupRepository(invocation)
        invocation.proceed()
    }

    @Override
    void interceptCleanupMethod(IMethodInvocation invocation) {
        try {
            invocation.proceed()
        } finally {
            destroyRepository(invocation)
        }
    }

    @Override
    void install(FieldInfo info) {
        super.install(info)

        final spec = info.parent.getTopSpec()
        spec.setupInterceptors.add(this)
        spec.cleanupInterceptors.add(this)
    }
}