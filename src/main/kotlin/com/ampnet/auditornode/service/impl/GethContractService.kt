package com.ampnet.auditornode.service.impl

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.ampnet.auditornode.configuration.properties.AuditorProperties
import com.ampnet.auditornode.configuration.properties.RpcProperties
import com.ampnet.auditornode.contract.ExampleStorageContractRPCConnector
import com.ampnet.auditornode.contract.ExampleStorageContractTransactionGenerator
import com.ampnet.auditornode.model.error.RpcError.ContractReadError
import com.ampnet.auditornode.model.error.RpcError.RpcConnectionError
import com.ampnet.auditornode.model.error.Try
import com.ampnet.auditornode.persistence.model.IpfsHash
import com.ampnet.auditornode.service.ContractService
import org.kethereum.model.Address
import org.kethereum.model.Transaction
import org.kethereum.rpc.EthereumRPC
import org.slf4j.LoggerFactory
import org.web3j.protocol.Web3j
import java.math.BigInteger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GethContractService @Inject constructor(
    auditorProperties: AuditorProperties,
    private val rpcProperties: RpcProperties,
    private val web3j: Web3j,
    rpc: EthereumRPC
) : ContractService {

    private val log = LoggerFactory.getLogger(javaClass)
    private val contractAddress = Address(auditorProperties.contractAddress)
    private val contractConnector = ExampleStorageContractRPCConnector(contractAddress, rpc)
    private val contractTransactionGenerator = ExampleStorageContractTransactionGenerator(contractAddress)

    override fun currentBlockNumber(): Try<BigInteger> =
        Either.catch {
            log.info("Fetching block number")
            web3j.ethBlockNumber().send().blockNumber
        }
            .mapLeft { RpcConnectionError(rpcProperties.url, it) }

    override fun getIpfsFileHash(): Try<IpfsHash> {
        log.info("Fetching IPFS file hash from contract address: $contractAddress")
        val hash = contractConnector.getHash()
            ?.right()
            ?.map { IpfsHash(it) }
        log.info("Got IPFS hash: {}", hash)

        return hash ?: ContractReadError(
            "Could not retrieve IPFS file hash; make sure your local ethereum light client is " +
                "fully synced with Ropsten testnet"
        ).left()
    }

    override fun storeIpfsFileHash(newHash: IpfsHash): Transaction { // TODO use different model for transaction here
        return contractTransactionGenerator.updateHash(newHash.value)
    }
}
