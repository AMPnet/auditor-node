package com.ampnet.auditornode.service.impl

import com.ampnet.auditornode.configuration.properties.RpcProperties
import com.ampnet.auditornode.model.contract.ContractAddress
import com.ampnet.auditornode.service.AssetHolderContractService
import com.ampnet.auditornode.service.AssetListHolderContractService
import com.ampnet.auditornode.service.ContractProvider
import com.ampnet.auditornode.service.ERC20ContractService
import io.micronaut.context.annotation.Factory
import mu.KotlinLogging
import org.web3j.protocol.Web3j
import javax.inject.Inject
import javax.inject.Singleton

private val logger = KotlinLogging.logger {}

@Factory
@Singleton
class Web3jContractProvider @Inject constructor(
    private val web3j: Web3j,
    private val rpcProperties: RpcProperties,
) : ContractProvider {

    override fun getAssetHolderContract(contractAddress: ContractAddress): AssetHolderContractService {
        logger.info { "Asset holder contract address: $contractAddress" }
        return Web3jAssetHolderContractService(web3j, rpcProperties, contractAddress)
    }

    override fun getAssetListHolderContract(contractAddress: ContractAddress): AssetListHolderContractService {
        logger.info { "Asset list holder contract address: $contractAddress" }
        return Web3jAssetListHolderContractService(web3j, rpcProperties, contractAddress)
    }

    override fun getERC20Contract(contractAddress: ContractAddress): ERC20ContractService {
        logger.info { "ERC20 contract address: $contractAddress" }
        return Web3jERC20ContractService(web3j, rpcProperties, contractAddress)
    }
}
