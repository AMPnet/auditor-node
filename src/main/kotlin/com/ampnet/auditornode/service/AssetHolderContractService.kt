package com.ampnet.auditornode.service

import com.ampnet.auditornode.model.contract.AssetAuditResult
import com.ampnet.auditornode.model.contract.AssetId
import com.ampnet.auditornode.model.contract.AssetTypeId
import com.ampnet.auditornode.model.contract.ContractAddress
import com.ampnet.auditornode.model.contract.EthereumAddress
import com.ampnet.auditornode.model.error.Try
import com.ampnet.auditornode.persistence.model.IpfsHash

interface AssetHolderContractService {
    val contractAddress: ContractAddress
    fun getAssetId(): Try<AssetId>
    fun getAssetTypeId(): Try<AssetTypeId>
    fun getAssetInfoIpfsHash(): Try<IpfsHash>
    fun getTokenizedAssetAddress(): Try<ContractAddress>
    fun getAssetListerAddress(): Try<EthereumAddress>
    fun getListingInfoIpfsHash(): Try<IpfsHash>
    fun getLatestAudit(): Try<AssetAuditResult>
}
