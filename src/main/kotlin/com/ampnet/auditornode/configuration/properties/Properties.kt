package com.ampnet.auditornode.configuration.properties

import io.micronaut.context.annotation.ConfigurationProperties
import io.micronaut.core.bind.annotation.Bindable

@ConfigurationProperties("auditor")
interface AuditorProperties {

    @get:Bindable(defaultValue = "0x1cfca869dFb6085DAE18Ae2D6D9e6B9489cA0eB9")
    val apxCoordinatorContractAddress: String

    @get:Bindable(defaultValue = "QmZd1FZqpvawNksF2tdwVQLgiMgRfuar1er83AYxxdXQod")
    val auditingProcedureDirectoryIpfsHash: String
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
    @get:Bindable(defaultValue = "https://rpc.goerli.mudit.blog")
    val url: String
}

@ConfigurationProperties("script")
interface ScriptProperties {
    @get:Bindable(defaultValue = "")
    val properties: Map<String, String>
}
