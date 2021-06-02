package com.ampnet.auditornode.persistence.model

import com.ampnet.auditornode.util.NativeReflection
import java.math.BigInteger

@JvmInline
value class AssetCategoryId(val value: BigInteger)

@JvmInline
value class AssetContractAddress(val value: String)

@NativeReflection
data class UnsignedTransaction(
    val to: String,
    val data: String
)
