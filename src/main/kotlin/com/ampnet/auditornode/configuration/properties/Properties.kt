package com.ampnet.auditornode.configuration.properties

import io.micronaut.context.annotation.ConfigurationProperties
import io.micronaut.core.bind.annotation.Bindable

@ConfigurationProperties("auditor")
interface AuditorProperties {

    @get:Bindable(defaultValue = "0xcaA9f2F9d9137E2fB806ecDf731CdD927aA9d97F")
    val assetContractAddress: String

    @get:Bindable(defaultValue = "0x9C1d4593148c26249624d334AA8316A3446a0cD2")
    val registryContractAddress: String

    @get:Bindable(defaultValue = "0xE239E7a361e0C82A1CF9E8C8B53353186B616EB7")
    val auditRegistryContractAddress: String
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
