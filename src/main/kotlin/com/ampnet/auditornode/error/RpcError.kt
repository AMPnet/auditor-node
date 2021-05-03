package com.ampnet.auditornode.error

sealed class RpcError(message: String, cause: Throwable?) : ApplicationError(message, cause) {

    class ContractReadError(message: String) : RpcError(message, null)

    class RpcConnectionError(rpcBaseUrl: String, cause: Throwable) : RpcError(
        message = "Cannot connect to RPC: $rpcBaseUrl",
        cause = cause
    )
}
