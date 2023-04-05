package com.developer.allefsousa.totp.config

import com.developer.allefsousa.totp.enum.HmacAlgorithm


open class HmacOneTimePasswordConfig(val codeDigits: Int, val hmacAlgorithm: HmacAlgorithm) {
    init {
        require(codeDigits >= 0) { "Number of code digits must be positive." }
    }
}