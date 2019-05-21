package com.wooga.spock.extensions.github.interceptor


import com.wooga.spock.extensions.github.Repository
import groovy.transform.InheritConstructors
import org.spockframework.runtime.extension.IMethodInvocation
import org.spockframework.runtime.model.FieldInfo
import spock.lang.Specification

@InheritConstructors
abstract class GithubRepositoryFieldInterceptor extends GithubRepositoryManagingInterceptor<FieldInfo> {

    @Override
    Repository setupRepository(IMethodInvocation invocation) {
        def repo = super.setupRepository(invocation)
        def spec = getSpec(invocation)
        info.writeValue(spec, repo)
        repo
    }

    @Override
    void destroyRepository(IMethodInvocation invocation) {
        Repository repository = getRepository(invocation)
        repository.repository.delete()
    }

    @Override
    void resetRepository(IMethodInvocation invocation) {
        Repository repository = getRepository(invocation)
        repository.resetRepository()
    }

    @Override
    void captureResetRef(IMethodInvocation invocation) {
        Repository repository = getRepository(invocation)
        repository.captureResetRefs()
    }

    protected Specification getSpec(IMethodInvocation invocation) {
        ((info.shared) ? invocation.sharedInstance : invocation.instance) as Specification
    }

    protected Repository getRepository(IMethodInvocation invocation) {
        final specInstance = getSpec(invocation)
        info.readValue(specInstance) as Repository
    }
}
