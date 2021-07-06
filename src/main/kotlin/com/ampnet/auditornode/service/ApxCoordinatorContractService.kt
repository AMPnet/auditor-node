package com.ampnet.auditornode.service

import com.ampnet.auditornode.model.contract.AssetId
import com.ampnet.auditornode.model.contract.UnsignedTransaction
import com.ampnet.auditornode.persistence.model.IpfsHash
import com.ampnet.auditornode.script.api.model.AuditResult

interface ApxCoordinatorContractService {
    fun generateTxForPerformAudit(
        assetId: AssetId,
        auditResult: AuditResult,
        directoryIpfsHash: IpfsHash
    ): UnsignedTransaction?
}
