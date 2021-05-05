package com.ampnet.auditornode.service.impl

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.ampnet.auditornode.configuration.AuditorConfiguration
import com.ampnet.auditornode.contract.ExampleStorageContractRPCConnector
import com.ampnet.auditornode.contract.ExampleStorageContractTransactionGenerator
import com.ampnet.auditornode.model.error.RpcError.ContractReadError
import com.ampnet.auditornode.model.error.RpcError.RpcConnectionError
import com.ampnet.auditornode.model.error.Try
import com.ampnet.auditornode.persistence.model.IpfsHash
import com.ampnet.auditornode.service.ContractService
import org.kethereum.model.Address
import org.kethereum.model.Transaction
import org.slf4j.LoggerFactory
import org.web3j.protocol.Web3j
import org.web3j.protocol.http.HttpService
import java.math.BigInteger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GethContractService @Inject constructor(
    private val auditorConfiguration: AuditorConfiguration
) : ContractService {

    private val log = LoggerFactory.getLogger(javaClass)
    private val web3j = Web3j.build(HttpService(auditorConfiguration.rpcUrl))
    private val rpc = GethEthereumRpc(web3j)
    private val contractAddress = Address(auditorConfiguration.contractAddress)
    private val contractConnector = ExampleStorageContractRPCConnector(contractAddress, rpc)

    override fun currentBlockNumber(): Try<BigInteger> =
        Either.catch {
            log.info("Fetching block number")
            web3j.ethBlockNumber().send().blockNumber
        }
            .mapLeft { RpcConnectionError(auditorConfiguration.rpcUrl, it) }

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
        return ExampleStorageContractTransactionGenerator(contractAddress)
            .updateHash(newHash.value)
    }
}
