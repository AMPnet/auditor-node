package com.ampnet.auditornode.service

import com.ampnet.auditornode.model.contract.AssetDescriptor
import com.ampnet.auditornode.model.contract.AssetId
import com.ampnet.auditornode.model.contract.ContractAddress
import com.ampnet.auditornode.model.error.Try

interface AssetListHolderContractService {
    val contractAddress: ContractAddress
    fun getAssets(): Try<List<AssetDescriptor>>
    fun getAssetById(assetId: AssetId): Try<AssetDescriptor>
}
