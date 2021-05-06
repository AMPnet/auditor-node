package com.ampnet.auditornode.configuration.properties

import io.micronaut.context.annotation.ConfigurationProperties

@Suppress("MagicNumber")
@ConfigurationProperties("ipfs")
class IpfsProperties {
    var gatewayUrl: String = "https://ipfs.io/ipfs/{ipfsHash}"
    var localClientPort: Int = 5001
}
