package com.ampnet.auditornode.configuration.properties

import io.micronaut.context.annotation.ConfigurationProperties

@ConfigurationProperties("auditor")
class AuditorProperties {
    var contractAddress: String = "0x992E8FeA2D91807797717178Aa6abEc7F20c31a8"
}

@Suppress("MagicNumber")
@ConfigurationProperties("ipfs")
class IpfsProperties {
    var gatewayUrl: String = "https://ipfs.io/ipfs/{ipfsHash}"
    var localClientPort: Int = 5001
}

@ConfigurationProperties("rpc")
class RpcProperties {
    var url: String = "https://ropsten.infura.io/v3/08664baf7af14eda956db2b71a79f12f"
}
