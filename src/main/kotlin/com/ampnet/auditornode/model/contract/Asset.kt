package com.ampnet.auditornode.model.contract

import com.ampnet.auditornode.persistence.model.IpfsHash
import java.math.BigInteger

@JvmInline
value class AssetId(val value: BigInteger)

@JvmInline
value class AssetTypeId(val value: BigInteger)

@JvmInline
value class LatestAuditTimestamp(val value: BigInteger)

data class AssetAuditResult(
    val verified: Boolean,
    val auditInfo: IpfsHash,
    val timestamp: LatestAuditTimestamp
)
