package com.ampnet.auditornode.configuration

import io.micronaut.context.annotation.ConfigurationProperties
import io.micronaut.context.annotation.Value

@ConfigurationProperties("auditor")
data class AuditorConfiguration(
    @Value("rpc-url") var rpcUrl: String,
    @Value("contract-address") var contractAddress: String
)
