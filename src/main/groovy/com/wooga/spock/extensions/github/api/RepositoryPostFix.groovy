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

package com.wooga.spock.extensions.github.api

/**
 * A simple base class describing test repository postfix name parts.
 */
abstract class RepositoryPostFix {
    /**
     * Returns a postfix name part.
     *
     * @return a {@code String} value with a postfix part.
     */
    abstract String getPostFix()
}

/**
 * A {@link RepositoryPostFix} implementation which always returns an empty {@code String} value.
 */
class EmptyRepositoryPostFix extends RepositoryPostFix {

    /**
     * Returns empty {@code String}.
     *
     * @return an empty {@code String} value.
     */
    @Override
    String getPostFix() {
        ""
    }
}

/**
 * A {@link RepositoryPostFix} implementation which tries to fetch the {@code travis} build number from the environment.
 */
class TravisBuildNumberPostFix extends RepositoryPostFix {
    /**
     * Returns the travis build number if value is set in the environment.
     * <p>
     * Tries to read the value of {@code TRAVIS_JOB_NUMBER} from the environment and returns it.
     * If the travis job number can't be found it returns an empty {@code String}.
     *
     * @return a {@code String} value with the travis job number or an empty {@code String}.
     */
    @Override
    String getPostFix() {
        String key = "TRAVIS_JOB_NUMBER"
        def env = System.getenv()
        if (env.containsKey(key)) {
            return env.get(key)
        }
        return ""
    }
}