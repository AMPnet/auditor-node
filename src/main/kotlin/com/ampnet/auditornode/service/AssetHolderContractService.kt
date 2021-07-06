package com.ampnet.auditornode.service

import com.ampnet.auditornode.model.contract.AssetId
import com.ampnet.auditornode.model.contract.ContractAddress
import com.ampnet.auditornode.model.error.Try
import com.ampnet.auditornode.persistence.model.IpfsHash

interface AssetHolderContractService {
    fun getAssetId(contractAddress: ContractAddress): Try<AssetId>
    fun getAssetInfoIpfsHash(contractAddress: ContractAddress): Try<IpfsHash>
}
