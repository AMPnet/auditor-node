package com.ampnet.auditornode.model.contract

import com.ampnet.auditornode.persistence.model.IpfsHash
import java.math.BigInteger

@JvmInline
value class AssetId(val value: BigInteger)

@JvmInline
value class AssetTypeId(val value: BigInteger)

@JvmInline
value class LatestAuditTimestamp(val value: BigInteger)

@JvmInline
value class AssetAuditGapDuration(val value: BigInteger)

data class AssetAuditResult(
    val verified: Boolean,
    val auditInfo: IpfsHash,
    val timestamp: LatestAuditTimestamp
)

data class AssetDescriptor(
    val assetHolder: ContractAddress,
    val tokenizedAsset: ContractAddress,
    val id: AssetId,
    val typeId: AssetTypeId,
    val name: String,
    val ticker: String
)
