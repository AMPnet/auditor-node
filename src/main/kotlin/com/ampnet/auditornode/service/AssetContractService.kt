package com.ampnet.auditornode.service

import com.ampnet.auditornode.model.error.Try
import com.ampnet.auditornode.persistence.model.AssetCategoryId
import com.ampnet.auditornode.persistence.model.IpfsHash

interface AssetContractService {
    fun getAssetInfoIpfsHash(): Try<IpfsHash>
    fun getAssetCategoryId(): Try<AssetCategoryId>
}
