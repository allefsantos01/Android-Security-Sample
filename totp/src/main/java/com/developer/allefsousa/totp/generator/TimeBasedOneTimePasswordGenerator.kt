package com.developer.allefsousa.totp.generator

import com.developer.allefsousa.totp.config.TimeBasedOneTimePasswordConfig
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.floor


class TimeBasedOneTimePasswordGenerator(
    private val secret: ByteArray,
    private val config: TimeBasedOneTimePasswordConfig
) {

    private val hmacOneTimePasswordGenerator: HmacOneTimePasswordGenerator =
        HmacOneTimePasswordGenerator(secret, config)

    /**
     * Calculate the current time slot.
     *
     * The timeslot is basically the number of `timeStep`s from
     * [TimeBasedOneTimePasswordConfig] which fits into the [timestamp].
     *
     * @param timestamp The Unix timestamp against the counting of the time
     * steps is calculated. The default value is the current system time from
     * [System.currentTimeMillis].
     */
    fun counter(timestamp: Long = System.currentTimeMillis()): Long {
        if (config.timeStep == 0L) {
            // To avoid a divide by zero
            return 0
        }

        return floor(
            timestamp.toDouble()
                .div(TimeUnit.MILLISECONDS.convert(config.timeStep, config.timeStepUnit))
        ).toLong()
    }

    /**
     * Convenience method for [counter].
     */
    fun counter(date: Date): Long = counter(date.time)

    /**
     * Convenience method for [counter].
     */

    /**
     * Calculates the start of the given time slot.
     *
     * This is basically the reverse calculation of counter(timestamp) method.
     *
     * @param counter The counter representing the time slot.
     * @return The Unix timestamp where the given time slot starts.
     */
    fun timeslotStart(counter: Long): Long {
        val timeStepMillis =
            TimeUnit.MILLISECONDS.convert(config.timeStep, config.timeStepUnit).toDouble()
        return (counter * timeStepMillis).toLong()
    }

    /**
     * Generates a code representing the time-based one-time password.
     *
     * The TOTP algorithm uses the HTOP algorithm via [HmacOneTimePasswordGenerator.generate],
     * with a counter parameter that represents the number of `timeStep`s from
     * [TimeBasedOneTimePasswordConfig] which fits into the [timestamp].
     *
     * The timestamp can be seen as the challenge to be solved. This should
     * normally be a continuous value over time (e.g. the current time).
     *
     * @param timestamp The Unix timestamp against the counting of the time
     * steps is calculated. The default value is the current system time from
     * [System.currentTimeMillis].
     */
    fun generate(timestamp: Long = System.currentTimeMillis()): String =
        hmacOneTimePasswordGenerator.generate(counter(timestamp))

    /**
     * Convenience method for [generate].
     */
    fun generate(date: Date): String = generate(date.time)

    /**
     * Convenience method for [generate].
     */

    /**
     * Validates the given code.
     *
     * @param code the code calculated from the challenge to validate.
     * @param timestamp the used challenge for the code. The default value is the
     *                  current system time from [System.currentTimeMillis].
     */
    fun isValid(code: String, timestamp: Long = System.currentTimeMillis()): Boolean {
        return code == generate(timestamp)
    }

    /**
     * Convenience method for [isValid].
     */
    fun isValid(code: String, date: Date) = isValid(code, date.time)

}