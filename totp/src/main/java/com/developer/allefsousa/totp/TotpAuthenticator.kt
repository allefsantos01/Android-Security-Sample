package com.developer.allefsousa.totp


import com.developer.allefsousa.totp.config.TimeBasedOneTimePasswordConfig
import com.developer.allefsousa.totp.enum.HmacAlgorithm
import com.developer.allefsousa.totp.generator.TimeBasedOneTimePasswordGenerator
import java.util.*
import org.apache.commons.codec.binary.Base32
import java.util.concurrent.TimeUnit

class TotpAuthenticator(private val base32secret: ByteArray) {

    companion object {
        val CONFIG = TimeBasedOneTimePasswordConfig(30, TimeUnit.SECONDS, 6, HmacAlgorithm.SHA1)
    }


    private val timeBasedOneTimePasswordGenerator: TimeBasedOneTimePasswordGenerator =
        TimeBasedOneTimePasswordGenerator(Base32().decode(base32secret), CONFIG)


    /**
     * Generates a code as a TOTP one-time password.
     *
     * @param timestamp the challenge for the code. The default value is the
     *                  current system time from [System.currentTimeMillis].
     */
    fun generate(timestamp: Date = Date(System.currentTimeMillis())): String {
        return timeBasedOneTimePasswordGenerator.generate(timestamp)
    }

}