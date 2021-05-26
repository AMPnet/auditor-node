package com.ampnet.auditornode.service.impl

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import com.ampnet.auditornode.configuration.properties.AuditorProperties
import com.ampnet.auditornode.configuration.properties.RpcProperties
import com.ampnet.auditornode.contract.RegistryContractRPCConnector
import com.ampnet.auditornode.model.error.RpcError
import com.ampnet.auditornode.model.error.Try
import com.ampnet.auditornode.persistence.model.AssetCategoryId
import com.ampnet.auditornode.persistence.model.IpfsHash
import com.ampnet.auditornode.service.RegistryContractService
import mu.KotlinLogging
import org.kethereum.model.Address
import org.kethereum.rpc.EthereumRPC
import javax.inject.Inject
import javax.inject.Singleton

private val logger = KotlinLogging.logger {}

@Singleton
class GethRegistryContractService @Inject constructor(
    rpc: EthereumRPC,
    auditorProperties: AuditorProperties,
    private val rpcProperties: RpcProperties
) : RegistryContractService {

    private val contractAddress = Address(auditorProperties.registryContractAddress)
    private val contractConnector = RegistryContractRPCConnector(contractAddress, rpc)

    override fun getAuditingProcedureDirectoryIpfsHash(assetCategoryId: AssetCategoryId): Try<IpfsHash> =
        Either.catch {
            logger.info {
                "Fetching auditing procedure directory IPFS hash for asset category ID $assetCategoryId from " +
                    "contract address: $contractAddress"
            }
            val hash = contractConnector.auditingProcedures(assetCategoryId.value)
                ?.right()
                ?.map { IpfsHash(it) }
            logger.info { "Got auditing procedure directory IPFS hash: $hash" }
            hash
        }
            .mapLeft { RpcError.RpcConnectionError(rpcProperties.url, it) }
            .flatMap {
                it ?: RpcError.ContractReadError(
                    "Could not retrieve auditing procedure directory IPFS hash from registry contract"
                ).left()
            }
}
