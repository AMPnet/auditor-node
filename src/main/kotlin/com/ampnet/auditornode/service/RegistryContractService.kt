package com.ampnet.auditornode.service

import com.ampnet.auditornode.model.error.Try
import com.ampnet.auditornode.persistence.model.AssetCategoryId
import com.ampnet.auditornode.persistence.model.IpfsHash

interface RegistryContractService {
    fun getAuditingProcedureDirectoryIpfsHash(assetCategoryId: AssetCategoryId): Try<IpfsHash>
}
