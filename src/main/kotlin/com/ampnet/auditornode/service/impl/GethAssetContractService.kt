package com.ampnet.auditornode.service.impl

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import com.ampnet.auditornode.configuration.properties.AuditorProperties
import com.ampnet.auditornode.configuration.properties.RpcProperties
import com.ampnet.auditornode.contract.AssetContractRPCConnector
import com.ampnet.auditornode.model.error.RpcError.ContractReadError
import com.ampnet.auditornode.model.error.RpcError.RpcConnectionError
import com.ampnet.auditornode.model.error.Try
import com.ampnet.auditornode.persistence.model.AssetCategoryId
import com.ampnet.auditornode.persistence.model.IpfsHash
import com.ampnet.auditornode.service.AssetContractService
import mu.KotlinLogging
import org.kethereum.model.Address
import org.kethereum.rpc.EthereumRPC
import javax.inject.Inject
import javax.inject.Singleton

private val logger = KotlinLogging.logger {}

@Singleton
class GethAssetContractService @Inject constructor(
    auditorProperties: AuditorProperties,
    private val rpcProperties: RpcProperties,
    rpc: EthereumRPC
) : AssetContractService {

    private val contractAddress = Address(auditorProperties.assetContractAddress)
    private val contractConnector = AssetContractRPCConnector(contractAddress, rpc)

    override fun getAssetInfoIpfsHash(): Try<IpfsHash> =
        Either.catch {
            logger.info { "Fetching asset info IPFS hash from contract address: $contractAddress" }
            val hash = contractConnector.info()
                ?.right()
                ?.map { IpfsHash(it) }
            logger.info { "Got asset info IPFS hash: $hash" }
            hash
        }
            .mapLeft { RpcConnectionError(rpcProperties.url, it) }
            .flatMap {
                it ?: ContractReadError("Could not retrieve asset info IPFS hash from asset contract").left()
            }

    override fun getAssetCategoryId(): Try<AssetCategoryId> =
        Either.catch {
            logger.info { "Fetching asset category ID from contract address: $contractAddress" }
            val assetCategoryId = contractConnector.categoryId()
                ?.right()
                ?.map { AssetCategoryId(it) }
            logger.info { "Got asset category ID: $assetCategoryId" }
            assetCategoryId
        }
            .mapLeft { RpcConnectionError(rpcProperties.url, it) }
            .flatMap {
                it ?: ContractReadError("Could not retrieve asset category ID from asset contract").left()
            }
}
