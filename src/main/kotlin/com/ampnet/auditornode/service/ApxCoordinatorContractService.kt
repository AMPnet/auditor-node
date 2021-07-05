package com.ampnet.auditornode.service

import com.ampnet.auditornode.model.contract.AssetAuditGapDuration
import com.ampnet.auditornode.model.contract.AssetId
import com.ampnet.auditornode.model.contract.Auditor
import com.ampnet.auditornode.model.contract.AuditorPool
import com.ampnet.auditornode.model.contract.AuditorPoolId
import com.ampnet.auditornode.model.contract.EthereumAddress
import com.ampnet.auditornode.model.contract.UnsignedTransaction
import com.ampnet.auditornode.model.contract.UsdcPerAudit
import com.ampnet.auditornode.model.contract.UsdcPerList
import com.ampnet.auditornode.model.error.Try
import com.ampnet.auditornode.persistence.model.IpfsHash
import com.ampnet.auditornode.script.api.model.AuditResult

interface ApxCoordinatorContractService {
    fun getStableCoinContract(): Try<ERC20ContractService>
    fun getAssetListHolderContract(): Try<AssetListHolderContractService>
    fun getAssetAuditGapDuration(): Try<AssetAuditGapDuration>
    fun getUsdcPerAudit(): Try<UsdcPerAudit>
    fun getUsdcPerList(): Try<UsdcPerList>
    fun getAuditorPoolMemberships(auditorAddress: EthereumAddress): Try<List<AuditorPoolId>>
    fun getAuditorPools(): Try<List<AuditorPool>>
    fun getAuditorPoolById(auditorPoolId: AuditorPoolId): Try<AuditorPool>
    fun getAuditorPoolMembers(auditorPoolId: AuditorPoolId): Try<List<Auditor>>
    fun generateTxForPerformAudit(
        assetId: AssetId,
        auditResult: AuditResult,
        directoryIpfsHash: IpfsHash
    ): UnsignedTransaction?
}
