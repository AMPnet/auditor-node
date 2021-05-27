package com.ampnet.auditornode.service

import com.ampnet.auditornode.persistence.model.AssetContractAddress
import com.ampnet.auditornode.persistence.model.UnsignedTransaction
import com.ampnet.auditornode.script.api.model.AuditResult

interface AuditRegistryContractTransactionService {
    fun castAuditVoteForAsset(
        assetContractAddress: AssetContractAddress,
        auditResult: AuditResult
    ): UnsignedTransaction?
}
