package com.ampnet.auditornode.service.impl

import com.ampnet.auditornode.configuration.properties.RpcProperties
import com.ampnet.auditornode.contract.AssetListHolder
import com.ampnet.auditornode.model.contract.AssetDescriptor
import com.ampnet.auditornode.model.contract.AssetId
import com.ampnet.auditornode.model.contract.AssetTypeId
import com.ampnet.auditornode.model.contract.ContractAddress
import com.ampnet.auditornode.model.error.Try
import com.ampnet.auditornode.service.AbstractWeb3jContractService
import com.ampnet.auditornode.service.AssetListHolderContractService
import mu.KotlinLogging
import org.web3j.protocol.Web3j
import org.web3j.tx.ReadonlyTransactionManager
import org.web3j.tx.gas.DefaultGasProvider

private val logger = KotlinLogging.logger {}

@Suppress("UsePropertyAccessSyntax")
class Web3jAssetListHolderContractService constructor(
    web3j: Web3j,
    rpcProperties: RpcProperties,
    override val contractAddress: ContractAddress
) : AbstractWeb3jContractService(logger, rpcProperties), AssetListHolderContractService {

    private class Contract(override val contractAddress: ContractAddress, web3j: Web3j) : IContract, AssetListHolder(
        contractAddress.value,
        web3j,
        ReadonlyTransactionManager(web3j, contractAddress.value),
        DefaultGasProvider()
    )

    override val contractName: String = "asset list holder"

    private val contract by lazy {
        Contract(contractAddress, web3j)
    }

    @Suppress("UNCHECKED_CAST")
    override fun getAssets(): Try<List<AssetDescriptor>> =
        getValueFromContract("assets", contract, { it.getAssets().send() }) {
            (it as List<AssetListHolder.AssetDescriptor>)
                .map { descriptor ->
                    AssetDescriptor(
                        assetHolder = ContractAddress(descriptor.assetHolder),
                        tokenizedAsset = ContractAddress(descriptor.tokenizedAsset),
                        id = AssetId(descriptor.id),
                        typeId = AssetTypeId(descriptor.typeId),
                        name = descriptor.name,
                        ticker = descriptor.ticker
                    )
                }
        }

    override fun getAssetById(assetId: AssetId): Try<AssetDescriptor> =
        getValueFromContract("asset by ID", contract, { it.getAssetById(assetId.value).send() }) {
            AssetDescriptor(
                assetHolder = ContractAddress(it.assetHolder),
                tokenizedAsset = ContractAddress(it.tokenizedAsset),
                id = AssetId(it.id),
                typeId = AssetTypeId(it.typeId),
                name = it.name,
                ticker = it.ticker
            )
        }
}
