package com.ampnet.auditornode.service.impl

import mu.KotlinLogging
import org.kethereum.model.Address
import org.kethereum.model.ChainId
import org.kethereum.model.SignedTransaction
import org.kethereum.model.Transaction
import org.kethereum.rpc.EthereumRPC
import org.kethereum.rpc.model.BlockInformation
import org.komputing.khex.extensions.toHexString
import org.komputing.khex.model.HexString
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameter
import java.math.BigInteger
import javax.inject.Inject
import javax.inject.Singleton

private val logger = KotlinLogging.logger {}

@Suppress("TooManyFunctions")
@Singleton
class GethEthereumRpc @Inject constructor(private val web3j: Web3j) : EthereumRPC {

    private fun notImplemented(): Nothing = throw NotImplementedError(
        "Requested method is not implemented; either implement it or use Web3j instance if possible"
    )

    override fun blockNumber(): BigInteger = notImplemented()

    override fun call(transaction: Transaction, block: String): HexString? {
        val web3jTransaction = org.web3j.protocol.core.methods.request.Transaction.createEthCallTransaction(
            transaction.from?.hex,
            transaction.to?.hex,
            transaction.input.toHexString()
        )
        logger.info {
            "Geth RPC call: eth_call(from: ${web3jTransaction.from}, to: ${web3jTransaction.to}," +
                " data: ${web3jTransaction.data})"
        }

        val result = web3j.ethCall(web3jTransaction, DefaultBlockParameter.valueOf(block))?.send()?.value
        logger.info { "Got result for eth_call: $result" }

        return result?.let { HexString(it) }
    }

    override fun chainId(): ChainId = notImplemented()

    override fun clientVersion(): String = notImplemented()

    override fun estimateGas(transaction: Transaction): BigInteger = notImplemented()

    override fun gasPrice(): BigInteger = notImplemented()

    override fun getBalance(address: Address, block: String): BigInteger = notImplemented()

    override fun getBlockByNumber(number: BigInteger): BlockInformation = notImplemented()

    override fun getCode(address: String, block: String): HexString = notImplemented()

    override fun getStorageAt(address: String, position: String, block: String): HexString = notImplemented()

    override fun getTransactionByHash(hash: String): SignedTransaction = notImplemented()

    override fun getTransactionCount(address: String, block: String): BigInteger = notImplemented()

    override fun sendRawTransaction(data: String): String = notImplemented()
}
