package com.ampnet.auditornode.rpc

import arrow.core.Either
import com.ampnet.auditornode.error.RpcError
import com.ampnet.auditornode.error.Try
import org.web3j.protocol.Web3j
import org.web3j.protocol.http.HttpService
import java.math.BigInteger

class RpcClient(private val rpcBaseUrl: String) {

    private val web3 = Web3j.build(HttpService(rpcBaseUrl))

    fun currentBlockNumber(): Try<BigInteger> =
        Either.catch {
            web3.ethBlockNumber().send().blockNumber
        }
            .mapLeft { RpcError.RpcConnectionError(rpcBaseUrl, it) }
}
