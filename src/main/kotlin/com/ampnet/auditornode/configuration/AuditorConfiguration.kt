package com.ampnet.auditornode.configuration

import io.micronaut.context.annotation.ConfigurationProperties

@ConfigurationProperties("auditor")
data class AuditorConfiguration(
    val rpcUrl: String,
    val contractAddress: String
)
