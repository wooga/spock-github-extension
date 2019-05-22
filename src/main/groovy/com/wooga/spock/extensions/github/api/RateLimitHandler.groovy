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

import org.kohsuke.github.RateLimitHandler

/**
 * A {@code RateLimitHandler} implementation which sets a timeout to wait until the github rate limit is reset.
 */
class RateLimitHandlerWait extends RateLimitHandler {
    @Override
    void onError(IOException e, HttpURLConnection uc) throws IOException {
        try {
            Thread.sleep(parseWaitTime(uc))
        } catch (InterruptedException ignored) {
            throw (InterruptedIOException)new InterruptedIOException().initCause(e)
        }
    }

    private static long parseWaitTime(HttpURLConnection uc) {
        String v = uc.getHeaderField("X-RateLimit-Reset")
        if (v==null) {
            return 10000
        }

        return Math.max(10000, Long.parseLong(v)*1000 - System.currentTimeMillis())
    }
}

/**
 * A {@code RateLimitHandler} implementation which throws an {@code IOException} when the rate limit is reached.
 */
class RateLimitHandlerFail extends RateLimitHandler {
    @Override
    void onError(IOException e, HttpURLConnection uc) throws IOException {
        throw (IOException)new IOException("API rate limit reached").initCause(e)
    }
}


