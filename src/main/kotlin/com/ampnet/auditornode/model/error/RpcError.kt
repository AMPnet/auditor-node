package com.ampnet.auditornode.model.error

sealed class RpcError(message: String, cause: Throwable?) : ApplicationError(message, cause) {

    data class ContractReadError(override val message: String) : RpcError(message, null)

    data class RpcConnectionError(val rpcBaseUrl: String, override val cause: Throwable) : RpcError(
        message = "Cannot connect to RPC: $rpcBaseUrl",
        cause = cause
    )
}
