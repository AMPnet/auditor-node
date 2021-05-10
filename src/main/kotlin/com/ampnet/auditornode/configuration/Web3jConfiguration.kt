package com.ampnet.auditornode.configuration

import com.ampnet.auditornode.configuration.properties.RpcProperties
import io.micronaut.context.annotation.Factory
import mu.KotlinLogging
import org.web3j.protocol.Web3j
import org.web3j.protocol.http.HttpService
import javax.inject.Inject
import javax.inject.Singleton

private val logger = KotlinLogging.logger {}

@Factory
class Web3jConfiguration {

    @Inject
    @Singleton
    fun web3j(rpcProperties: RpcProperties): Web3j {
        logger.info { "RPC connection URL: ${rpcProperties.url}" }
        return Web3j.build(HttpService(rpcProperties.url))
    }
}
