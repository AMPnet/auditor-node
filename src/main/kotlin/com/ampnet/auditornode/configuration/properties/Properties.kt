package com.ampnet.auditornode.configuration.properties

import io.micronaut.context.annotation.ConfigurationProperties
import io.micronaut.core.bind.annotation.Bindable

@ConfigurationProperties("auditor")
interface AuditorProperties {
    @get:Bindable(defaultValue = "0x992E8FeA2D91807797717178Aa6abEc7F20c31a8")
    val contractAddress: String
}

@Suppress("MagicNumber")
@ConfigurationProperties("ipfs")
interface IpfsProperties {

    @get:Bindable(defaultValue = "https://ipfs.io/ipfs/{ipfsHash}")
    val gatewayUrl: String

    @get:Bindable(defaultValue = "5001")
    val localClientPort: Int
}

@ConfigurationProperties("rpc")
interface RpcProperties {
    @get:Bindable(defaultValue = "https://ropsten.infura.io/v3/08664baf7af14eda956db2b71a79f12f")
    val url: String
}

@ConfigurationProperties("script")
interface ScriptProperties {
    @get:Bindable(defaultValue = "")
    val properties: Map<String, String>
}
