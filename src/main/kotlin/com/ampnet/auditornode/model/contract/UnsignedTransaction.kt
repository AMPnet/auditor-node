package com.ampnet.auditornode.model.contract

import com.ampnet.auditornode.util.NativeReflection

@NativeReflection
data class UnsignedTransaction(
    val to: String,
    val data: String
)
