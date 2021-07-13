package com.wooga.spock.extensions.github.interceptor

import org.spockframework.runtime.model.FieldInfo

interface FieldInterceptor {
    void install(FieldInfo info)
}
