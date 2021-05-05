package com.ampnet.auditornode.service.impl

import org.kethereum.model.Address
import org.kethereum.model.ChainId
import org.kethereum.model.SignedTransaction
import org.kethereum.model.Transaction
import org.kethereum.rpc.EthereumRPC
import org.kethereum.rpc.model.BlockInformation
import org.komputing.khex.extensions.toHexString
import org.komputing.khex.model.HexString
import org.slf4j.LoggerFactory
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameter
import java.math.BigInteger

class GethEthereumRpc(private val web3j: Web3j) : EthereumRPC { // TODO expose and initialize as bean

    private val log = LoggerFactory.getLogger(javaClass)

    override fun blockNumber(): BigInteger = TODO("Unsupported method")

    override fun call(transaction: Transaction, block: String): HexString? {
        val web3jTransaction = org.web3j.protocol.core.methods.request.Transaction.createEthCallTransaction(
            transaction.from?.hex,
            transaction.to?.hex,
            transaction.input.toHexString()
        )
        log.info(
            "Geth RPC call: eth_call(from: {}, to: {}, data: {})",
            web3jTransaction.from, web3jTransaction.to, web3jTransaction.data
        )

        val result = web3j.ethCall(web3jTransaction, DefaultBlockParameter.valueOf(block))?.send()?.value
        log.info("Got result for eth_call: {}", result)

        return result?.let { HexString(it) }
    }

    override fun chainId(): ChainId = TODO("Unsupported method")

    override fun clientVersion(): String = TODO("Unsupported method")

    override fun estimateGas(transaction: Transaction): BigInteger = TODO("Unsupported method")

    override fun gasPrice(): BigInteger = TODO("Unsupported method")

    override fun getBalance(address: Address, block: String): BigInteger = TODO("Unsupported method")

    override fun getBlockByNumber(number: BigInteger): BlockInformation = TODO("Unsupported method")

    override fun getCode(address: String, block: String): HexString = TODO("Unsupported method")

    override fun getStorageAt(address: String, position: String, block: String): HexString = TODO("Unsupported method")

    override fun getTransactionByHash(hash: String): SignedTransaction = TODO("Unsupported method")

    override fun getTransactionCount(address: String, block: String): BigInteger = TODO("Unsupported method")

    override fun sendRawTransaction(data: String): String = TODO("Unsupported method")
}
