package com.ao3reader.data.remote

import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Rate limiter to ensure we respect AO3's Terms of Service.
 * Enforces a minimum 5-second delay between requests.
 */
@Singleton
class RateLimiter @Inject constructor() {
    private val minDelayMillis = 5000L  // 5 seconds per AO3 ToS
    private var lastRequestTime = 0L
    private val mutex = Mutex()

    /**
     * Throttles the execution of a suspending function to ensure
     * we don't make requests faster than the minimum delay.
     */
    suspend fun <T> throttle(block: suspend () -> T): T {
        mutex.withLock {
            val now = System.currentTimeMillis()
            val timeSinceLastRequest = now - lastRequestTime

            if (timeSinceLastRequest < minDelayMillis) {
                val delayTime = minDelayMillis - timeSinceLastRequest
                delay(delayTime)
            }

            return try {
                block()
            } finally {
                lastRequestTime = System.currentTimeMillis()
            }
        }
    }

    /**
     * Returns the time in milliseconds until the next request can be made.
     */
    fun getTimeUntilNextRequest(): Long {
        val timeSinceLastRequest = System.currentTimeMillis() - lastRequestTime
        return maxOf(0, minDelayMillis - timeSinceLastRequest)
    }

    /**
     * Resets the rate limiter (useful for testing).
     */
    fun reset() {
        lastRequestTime = 0L
    }
}
