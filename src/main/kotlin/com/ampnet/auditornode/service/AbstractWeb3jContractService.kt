package com.ampnet.auditornode.service

import arrow.core.Either
import arrow.core.flatten
import arrow.core.right
import com.ampnet.auditornode.configuration.properties.RpcProperties
import com.ampnet.auditornode.model.contract.ContractAddress
import com.ampnet.auditornode.model.error.RpcError.ContractReadError
import com.ampnet.auditornode.model.error.RpcError.RpcConnectionError
import com.ampnet.auditornode.model.error.Try
import mu.KLogger
import org.web3j.tx.exceptions.ContractCallException

@Suppress("UnnecessaryAbstractClass")
abstract class AbstractWeb3jContractService(
    private val logger: KLogger,
    private val rpcProperties: RpcProperties
) {

    protected abstract val contractName: String

    protected interface IContract {
        val contractAddress: ContractAddress
    }

    protected fun <T, R, C : IContract> getValueFromContract(
        valueName: String,
        contract: C,
        contractGetter: (C) -> T,
        converter: (T) -> R
    ): Try<R> =
        Either.catch {
            logger.info { "Fetching $valueName from contract address: ${contract.contractAddress}" }
            val value = contractGetter(contract)
                .right()
                .map(converter)
            logger.info { "Got $valueName: $value" }
            value
        }
            .mapLeft {
                logger.error(it) { "RPC error" }
                when (it) {
                    is ContractCallException, is NullPointerException -> ContractReadError(
                        "Could not fetch $valueName from $contractName contract"
                    )
                    else -> RpcConnectionError(rpcProperties.url, it)
                }
            }
            .flatten()
}
