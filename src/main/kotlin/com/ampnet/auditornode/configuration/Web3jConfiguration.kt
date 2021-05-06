package com.ampnet.auditornode.configuration

import com.ampnet.auditornode.configuration.properties.RpcProperties
import io.micronaut.context.annotation.Factory
import org.slf4j.LoggerFactory
import org.web3j.protocol.Web3j
import org.web3j.protocol.http.HttpService
import javax.inject.Inject
import javax.inject.Singleton

@Factory
class Web3jConfiguration {

    private val log = LoggerFactory.getLogger(javaClass)

    @Inject
    @Singleton
    fun web3j(rpcProperties: RpcProperties): Web3j {
        log.info("RPC connection URL: {}", rpcProperties.url)
        return Web3j.build(HttpService(rpcProperties.url))
    }
}
