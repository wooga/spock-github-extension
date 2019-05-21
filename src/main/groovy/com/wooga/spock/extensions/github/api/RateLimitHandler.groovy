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


