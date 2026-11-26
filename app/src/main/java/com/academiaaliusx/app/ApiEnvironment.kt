package com.academiaaliusx.app

/**
 * Central configuration for the shared Academia Alius X backend.
 *
 * Endpoint paths and authentication must be implemented only after the
 * deployed Pages Functions contract is verified. No secrets belong here.
 */
object ApiEnvironment {
    const val PRODUCTION_BASE_URL: String = "https://academiaaliusx.com/"
    const val HTTPS_REQUIRED: Boolean = true
    const val API_CONTRACT_VERIFIED: Boolean = false
}
