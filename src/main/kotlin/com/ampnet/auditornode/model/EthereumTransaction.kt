package com.ampnet.auditornode.model

data class EthereumTransaction(
    val to: EthereumAddress,
    val data: String
) {
    fun toJson(): String = """{"to":"${to.value}","data":"$data"}""" // TODO serialize via some library
}
