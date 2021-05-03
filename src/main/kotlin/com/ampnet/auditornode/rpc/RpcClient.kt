package com.ampnet.auditornode.rpc

import arrow.core.Either
import com.ampnet.auditornode.model.error.RpcError
import com.ampnet.auditornode.model.error.Try
import org.web3j.protocol.Web3j
import org.web3j.protocol.http.HttpService
import java.math.BigInteger

@Deprecated(message = "for removal")
class RpcClient(private val rpcBaseUrl: String) { // TODO delete this class

    private val web3 = Web3j.build(HttpService(rpcBaseUrl))

    fun currentBlockNumber(): Try<BigInteger> =
        Either.catch {
            web3.ethBlockNumber().send().blockNumber
        }
            .mapLeft { RpcError.RpcConnectionError(rpcBaseUrl, it) }
}
