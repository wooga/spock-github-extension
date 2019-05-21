package com.wooga.spock.extensions.github.interceptor


import groovy.transform.InheritConstructors
import org.spockframework.runtime.extension.IMethodInvocation
import org.spockframework.runtime.model.FieldInfo
import org.spockframework.runtime.model.SpecInfo

@InheritConstructors
class SharedGithubRepositoryInterceptor extends GithubRepositoryFieldInterceptor {

    boolean getResetAfterTestCase() {
        this.metadata.resetAfterTestCase()
    }

    @Override
    void interceptSetupSpecMethod(IMethodInvocation invocation) {
        setupRepository(invocation)
        invocation.proceed()
        captureResetRef(invocation)
    }

    @Override
    void interceptCleanupSpecMethod(IMethodInvocation invocation) {
        try {
            invocation.proceed()
        } finally {
            destroyRepository(invocation)
        }
    }

    @Override
    void interceptCleanupMethod(IMethodInvocation invocation) {
        try {
            invocation.proceed()
        } finally {
            resetRepository(invocation)
        }
    }

    @Override
    void install(FieldInfo info) {
        super.install(info)

        final spec = info.getParent().getTopSpec()
        spec.setupSpecInterceptors.add(this)
        spec.cleanupSpecInterceptors.add(this)

        if (resetAfterTestCase) {
            spec.cleanupInterceptors.add(this)
        }
    }
}