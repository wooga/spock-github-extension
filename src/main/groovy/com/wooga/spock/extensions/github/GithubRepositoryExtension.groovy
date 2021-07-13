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

import com.wooga.spock.extensions.github.interceptor.FieldInterceptor
import com.wooga.spock.extensions.github.interceptor.GithubRepositoryFeatureInterceptor
import com.wooga.spock.extensions.github.interceptor.GithubRepositoryInterceptor
import com.wooga.spock.extensions.github.interceptor.SharedGithubRepositoryInterceptor
import org.spockframework.runtime.extension.AbstractAnnotationDrivenExtension
import org.spockframework.runtime.model.FeatureInfo
import org.spockframework.runtime.model.FieldInfo

class GithubRepositoryExtension extends AbstractAnnotationDrivenExtension<GithubRepository> {


    @Override
    void visitFeatureAnnotation(GithubRepository annotation, FeatureInfo feature) {
        def interceptor = GithubRepositoryFeatureInterceptor.withMetadata(annotation)
        interceptor.install(feature)
    }

    @Override
    void visitFieldAnnotation(GithubRepository annotation, FieldInfo field) {
        FieldInterceptor interceptor = field.isShared()?
                SharedGithubRepositoryInterceptor.withMetadata(annotation, field) :
                GithubRepositoryInterceptor.withMetadata(annotation, field)

        interceptor.install(field)
    }
}



