package com.ampnet.auditornode.persistence.model

import com.ampnet.auditornode.util.NativeReflection
import java.math.BigInteger

inline class AssetCategoryId(val value: BigInteger) // TODO use `value class` instead on later Kotlin version

inline class AssetContractAddress(val value: String) // TODO use `value class` instead on later Kotlin version

@NativeReflection
data class UnsignedTransaction(
    val to: String,
    val data: String
)
