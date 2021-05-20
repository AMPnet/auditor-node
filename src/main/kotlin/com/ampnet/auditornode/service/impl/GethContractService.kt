package com.ampnet.auditornode.service.impl

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import com.ampnet.auditornode.configuration.properties.AuditorProperties
import com.ampnet.auditornode.configuration.properties.RpcProperties
import com.ampnet.auditornode.contract.ExampleStorageContractRPCConnector
import com.ampnet.auditornode.contract.ExampleStorageContractTransactionGenerator
import com.ampnet.auditornode.model.EthereumAddress
import com.ampnet.auditornode.model.EthereumTransaction
import com.ampnet.auditornode.model.error.RpcError.ContractReadError
import com.ampnet.auditornode.model.error.RpcError.RpcConnectionError
import com.ampnet.auditornode.model.error.Try
import com.ampnet.auditornode.persistence.model.IpfsHash
import com.ampnet.auditornode.service.ContractService
import mu.KotlinLogging
import org.kethereum.model.Address
import org.kethereum.rpc.EthereumRPC
import org.komputing.khex.extensions.toHexString
import org.web3j.protocol.Web3j
import java.math.BigInteger
import javax.inject.Inject
import javax.inject.Singleton

private val logger = KotlinLogging.logger {}

@Singleton
class GethContractService @Inject constructor(
    auditorProperties: AuditorProperties,
    private val rpcProperties: RpcProperties,
    private val web3j: Web3j,
    rpc: EthereumRPC
) : ContractService {

    private val contractAddress = Address(auditorProperties.contractAddress)
    private val contractConnector = ExampleStorageContractRPCConnector(contractAddress, rpc)
    private val contractTransactionGenerator = ExampleStorageContractTransactionGenerator(contractAddress)

    override fun currentBlockNumber(): Try<BigInteger> =
        Either.catch {
            logger.info { "Fetching block number" }
            web3j.ethBlockNumber().send().blockNumber
        }
            .mapLeft { RpcConnectionError(rpcProperties.url, it) }

    override fun getIpfsDirectoryHash(): Try<IpfsHash> =
        Either.catch {
            logger.info { "Fetching IPFS directory hash from contract address: $contractAddress" }
            val hash = contractConnector.getHash()
                ?.right()
                ?.map { IpfsHash(it) }
            logger.info { "Got IPFS hash: $hash" }
            hash
        }
            .mapLeft { RpcConnectionError(rpcProperties.url, it) }
            .flatMap {
                it ?: ContractReadError(
                    "Could not retrieve IPFS directory hash; make sure your local ethereum light client is " +
                        "fully synced with Ropsten testnet"
                ).left()
            }

    override fun storeIpfsDirectoryHash(newHash: IpfsHash): EthereumTransaction {
        val transaction = contractTransactionGenerator.updateHash(newHash.value)
        return EthereumTransaction(
            to = EthereumAddress(contractAddress.hex),
            data = transaction.input.toHexString()
        )
    }
}
