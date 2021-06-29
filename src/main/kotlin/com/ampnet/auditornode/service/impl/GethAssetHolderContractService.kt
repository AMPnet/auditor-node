package com.ampnet.auditornode.service.impl

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import com.ampnet.auditornode.configuration.properties.RpcProperties
import com.ampnet.auditornode.contract.asset.AssetHolderContractRPCConnector
import com.ampnet.auditornode.model.contract.AssetId
import com.ampnet.auditornode.model.error.RpcError.ContractReadError
import com.ampnet.auditornode.model.error.RpcError.RpcConnectionError
import com.ampnet.auditornode.model.error.Try
import com.ampnet.auditornode.persistence.model.IpfsHash
import com.ampnet.auditornode.service.AssetHolderContractService
import mu.KotlinLogging
import org.kethereum.model.Address
import org.kethereum.rpc.EthereumRPC
import javax.inject.Inject
import javax.inject.Singleton

private val logger = KotlinLogging.logger {}

@Singleton
class GethAssetHolderContractService @Inject constructor(
    private val rpc: EthereumRPC,
    private val rpcProperties: RpcProperties
) : AssetHolderContractService {

    private fun connector(contractAddress: Address) =
        AssetHolderContractRPCConnector(contractAddress, rpc)

    private fun <T, R> getValueFromContract(
        contractAddress: Address,
        valueName: String,
        contractGetter: (AssetHolderContractRPCConnector) -> T?,
        wrapper: (T) -> R
    ): Try<R> =
        Either.catch {
            logger.info { "Fetching $valueName from contract address: $contractAddress" }
            val contractConnector = connector(contractAddress)
            val id = contractGetter(contractConnector)
                ?.right()
                ?.map(wrapper)
            logger.info { "Got $valueName: $id" }
            id
        }
            .mapLeft { RpcConnectionError(rpcProperties.url, it) }
            .flatMap {
                it ?: ContractReadError("Could not fetch $valueName from asset contract").left()
            }

    override fun getAssetId(contractAddress: Address): Try<AssetId> =
        getValueFromContract(contractAddress, "asset ID", { it.id() }, ::AssetId)

    override fun getAssetInfoIpfsHash(contractAddress: Address): Try<IpfsHash> =
        getValueFromContract(contractAddress, "asset info IPFS hash", { it.info() }, ::IpfsHash)
}
