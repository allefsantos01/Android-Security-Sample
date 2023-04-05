package com.developer.allefsousa.totp.config

import com.developer.allefsousa.totp.enum.HmacAlgorithm
import java.util.concurrent.TimeUnit

open class TimeBasedOneTimePasswordConfig(val timeStep: Long,
                                          val timeStepUnit: TimeUnit,
                                          codeDigits: Int,
                                          hmacAlgorithm: HmacAlgorithm
): HmacOneTimePasswordConfig(codeDigits, hmacAlgorithm) {

    init {
        require(timeStep >= 0) { "Time step must have a positive value." }
    }

}