package com.ampnet.auditornode.configuration.properties

import io.micronaut.context.annotation.ConfigurationProperties

@ConfigurationProperties("auditor")
class AuditorProperties {
    var contractAddress: String = "0x992E8FeA2D91807797717178Aa6abEc7F20c31a8"
}
