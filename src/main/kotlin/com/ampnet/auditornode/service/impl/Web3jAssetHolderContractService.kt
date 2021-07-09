package com.ampnet.auditornode.service.impl

import com.ampnet.auditornode.configuration.properties.RpcProperties
import com.ampnet.auditornode.contract.AssetHolder
import com.ampnet.auditornode.model.contract.AssetAuditResult
import com.ampnet.auditornode.model.contract.AssetId
import com.ampnet.auditornode.model.contract.AssetTypeId
import com.ampnet.auditornode.model.contract.ContractAddress
import com.ampnet.auditornode.model.contract.EthereumAddress
import com.ampnet.auditornode.model.contract.LatestAuditTimestamp
import com.ampnet.auditornode.model.error.Try
import com.ampnet.auditornode.persistence.model.IpfsHash
import com.ampnet.auditornode.service.AbstractWeb3jContractService
import com.ampnet.auditornode.service.AssetHolderContractService
import mu.KotlinLogging
import org.web3j.protocol.Web3j
import org.web3j.tx.ReadonlyTransactionManager
import org.web3j.tx.gas.DefaultGasProvider
import javax.inject.Inject

private val logger = KotlinLogging.logger {}

@Suppress("UsePropertyAccessSyntax")
class Web3jAssetHolderContractService @Inject constructor(
    web3j: Web3j,
    rpcProperties: RpcProperties,
    override val contractAddress: ContractAddress
) : AbstractWeb3jContractService(logger, rpcProperties), AssetHolderContractService {

    private class Contract(override val contractAddress: ContractAddress, web3j: Web3j) : IContract, AssetHolder(
        contractAddress.value,
        web3j,
        ReadonlyTransactionManager(web3j, contractAddress.value),
        DefaultGasProvider()
    )

    private val contract by lazy {
        Contract(contractAddress, web3j)
    }

    override val contractName: String = "asset holder"

    override fun getAssetId(): Try<AssetId> =
        getValueFromContract("asset ID", contract, { it.id().send() }, ::AssetId)

    override fun getAssetTypeId(): Try<AssetTypeId> =
        getValueFromContract("asset type ID", contract, { it.typeId().send() }, ::AssetTypeId)

    override fun getAssetInfoIpfsHash(): Try<IpfsHash> =
        getValueFromContract("asset info IPFS hash", contract, { it.info().send() }, ::IpfsHash)

    override fun getTokenizedAssetAddress(): Try<ContractAddress> =
        getValueFromContract("tokenized asset address", contract, { it.tokenizedAsset().send() }, ::ContractAddress)

    override fun getAssetListerAddress(): Try<EthereumAddress> =
        getValueFromContract("asset lister address", contract, { it.listedBy().send() }, ::EthereumAddress)

    override fun getListingInfoIpfsHash(): Try<IpfsHash> =
        getValueFromContract("asset listing info IPFS hash", contract, { it.listingInfo().send() }, ::IpfsHash)

    override fun getLatestAudit(): Try<AssetAuditResult> =
        getValueFromContract("latest asset audit", contract, { it.getLatestAudit().send() }) {
            AssetAuditResult(
                verified = it.assetVerified,
                auditInfo = IpfsHash(it.additionalInfo),
                timestamp = LatestAuditTimestamp(it.timestamp)
            )
        }
}
