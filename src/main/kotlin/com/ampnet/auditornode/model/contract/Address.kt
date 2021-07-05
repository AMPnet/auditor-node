package com.ampnet.auditornode.model.contract

@JvmInline
value class ContractAddress(val value: String) {
    fun asEthereumAddress() = EthereumAddress(value)
}

@JvmInline
value class EthereumAddress(val value: String)
