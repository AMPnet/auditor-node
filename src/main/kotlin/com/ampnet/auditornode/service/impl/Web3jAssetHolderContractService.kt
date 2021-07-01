package com.ampnet.auditornode.service.impl

import arrow.core.Either
import arrow.core.flatten
import arrow.core.right
import com.ampnet.auditornode.configuration.properties.RpcProperties
import com.ampnet.auditornode.contract.AssetHolder
import com.ampnet.auditornode.model.contract.AssetId
import com.ampnet.auditornode.model.error.RpcError.ContractReadError
import com.ampnet.auditornode.model.error.RpcError.RpcConnectionError
import com.ampnet.auditornode.model.error.Try
import com.ampnet.auditornode.persistence.model.IpfsHash
import com.ampnet.auditornode.service.AssetHolderContractService
import mu.KotlinLogging
import org.kethereum.model.Address
import org.web3j.protocol.Web3j
import org.web3j.tx.ReadonlyTransactionManager
import org.web3j.tx.exceptions.ContractCallException
import org.web3j.tx.gas.DefaultGasProvider
import javax.inject.Inject
import javax.inject.Singleton

private val logger = KotlinLogging.logger {}

@Singleton
class Web3jAssetHolderContractService @Inject constructor(
    private val web3j: Web3j,
    private val rpcProperties: RpcProperties
) : AssetHolderContractService {

    private class Contract(contractAddress: Address, web3j: Web3j) : AssetHolder(
        contractAddress.hex,
        web3j,
        ReadonlyTransactionManager(web3j, contractAddress.hex),
        DefaultGasProvider()
    )

    private fun <T, R> getValueFromContract(
        contractAddress: Address,
        valueName: String,
        contractGetter: (Contract) -> T,
        wrapper: (T) -> R
    ): Try<R> =
        Either.catch {
            logger.info { "Fetching $valueName from contract address: $contractAddress" }
            val contract = Contract(contractAddress, web3j)
            val value = contractGetter(contract)
                .right()
                .map(wrapper)
            logger.info { "Got $valueName: $value" }
            value
        }
            .mapLeft {
                when (it) {
                    is ContractCallException, is NullPointerException -> ContractReadError(
                        "Could not fetch $valueName from asset contract"
                    )
                    else -> RpcConnectionError(rpcProperties.url, it)
                }
            }
            .flatten()

    override fun getAssetId(contractAddress: Address): Try<AssetId> =
        getValueFromContract(contractAddress, "asset ID", { it.id().send() }, ::AssetId)

    override fun getAssetInfoIpfsHash(contractAddress: Address): Try<IpfsHash> =
        getValueFromContract(contractAddress, "asset info IPFS hash", { it.info().send() }, ::IpfsHash)
}
