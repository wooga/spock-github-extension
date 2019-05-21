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