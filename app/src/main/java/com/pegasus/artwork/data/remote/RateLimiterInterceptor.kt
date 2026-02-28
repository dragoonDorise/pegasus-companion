package com.pegasus.artwork.data.remote

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Interceptor
import okhttp3.Response

class RateLimiterInterceptor : Interceptor {

    private val mutex = Mutex()
    private var lastRequestTime = 0L

    companion object {
        private const val MIN_INTERVAL_MS = 1200L
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        runBlocking {
            mutex.withLock {
                val now = System.currentTimeMillis()
                val elapsed = now - lastRequestTime
                if (elapsed < MIN_INTERVAL_MS) {
                    val delay = MIN_INTERVAL_MS - elapsed
                    kotlinx.coroutines.delay(delay)
                }
                lastRequestTime = System.currentTimeMillis()
            }
        }
        return chain.proceed(chain.request())
    }
}
