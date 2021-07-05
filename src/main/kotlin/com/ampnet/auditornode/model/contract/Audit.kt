package com.ampnet.auditornode.model.contract

import com.ampnet.auditornode.persistence.model.IpfsHash
import java.math.BigInteger

@JvmInline
value class AuditorPoolId(val value: BigInteger)

data class AuditorPool(
    val id: AuditorPoolId,
    val name: String,
    val info: IpfsHash,
    val active: Boolean,
    val activeMembers: BigInteger
)

data class Auditor(
    val address: EthereumAddress,
    val totalAuditsPerformed: BigInteger,
    val totalListingsPerformed: BigInteger,
    val info: IpfsHash
)
