package com.wooga.spock.extensions.github


import com.wooga.spock.extensions.github.interceptor.GithubRepositoryFeatureInterceptor
import com.wooga.spock.extensions.github.interceptor.GithubRepositoryInterceptor
import com.wooga.spock.extensions.github.interceptor.SharedGithubRepositoryInterceptor
import org.spockframework.runtime.extension.AbstractAnnotationDrivenExtension
import org.spockframework.runtime.model.FeatureInfo
import org.spockframework.runtime.model.FieldInfo

class GithubRepositoryExtension extends AbstractAnnotationDrivenExtension<GithubRepository> {

    @Override
    void visitFeatureAnnotation(GithubRepository annotation, FeatureInfo feature) {
        def interceptor

        interceptor = new GithubRepositoryFeatureInterceptor(annotation)
        interceptor.install(feature)
    }

    @Override
    void visitFieldAnnotation(GithubRepository annotation, FieldInfo field) {
        def interceptor

        if (field.isShared()) {
            interceptor = new SharedGithubRepositoryInterceptor(annotation)
        } else {
            interceptor = new GithubRepositoryInterceptor(annotation)
        }

        interceptor.install(field)
    }
}



