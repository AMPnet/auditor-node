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
import javax.inject.Singleton

private val logger = KotlinLogging.logger {}

@Singleton
@Suppress("UsePropertyAccessSyntax")
class Web3jAssetHolderContractService @Inject constructor(
    private val web3j: Web3j,
    rpcProperties: RpcProperties
) : AbstractWeb3jContractService(logger, rpcProperties), AssetHolderContractService {

    private class Contract(override val contractAddress: ContractAddress, web3j: Web3j) : IContract, AssetHolder(
        contractAddress.value,
        web3j,
        ReadonlyTransactionManager(web3j, contractAddress.value),
        DefaultGasProvider()
    )

    override val contractName: String = "asset holder"

    override fun getAssetId(contractAddress: ContractAddress): Try<AssetId> =
        getValueFromContract("asset ID", Contract(contractAddress, web3j), { it.id().send() }, ::AssetId)

    override fun getAssetTypeId(contractAddress: ContractAddress): Try<AssetTypeId> =
        getValueFromContract("asset type ID", Contract(contractAddress, web3j), { it.typeId().send() }, ::AssetTypeId)

    override fun getAssetInfoIpfsHash(contractAddress: ContractAddress): Try<IpfsHash> =
        getValueFromContract("asset info IPFS hash", Contract(contractAddress, web3j), { it.info().send() }, ::IpfsHash)

    override fun getTokenizedAssetAddress(contractAddress: ContractAddress): Try<ContractAddress> =
        getValueFromContract(
            "tokenized asset address",
            Contract(contractAddress, web3j),
            { it.tokenizedAsset().send() },
            ::ContractAddress
        )

    override fun getAssetListerAddress(contractAddress: ContractAddress): Try<EthereumAddress> =
        getValueFromContract(
            "asset lister address",
            Contract(contractAddress, web3j),
            { it.listedBy().send() },
            ::EthereumAddress
        )

    override fun getListingInfoIpfsHash(contractAddress: ContractAddress): Try<IpfsHash> =
        getValueFromContract(
            "asset listing info IPFS hash",
            Contract(contractAddress, web3j),
            { it.listingInfo().send() },
            ::IpfsHash
        )

    override fun getLatestAudit(contractAddress: ContractAddress): Try<AssetAuditResult> =
        getValueFromContract("latest asset audit", Contract(contractAddress, web3j), { it.getLatestAudit().send() }) {
            AssetAuditResult(
                verified = it.assetVerified,
                auditInfo = IpfsHash(it.additionalInfo),
                timestamp = LatestAuditTimestamp(it.timestamp)
            )
        }
}
