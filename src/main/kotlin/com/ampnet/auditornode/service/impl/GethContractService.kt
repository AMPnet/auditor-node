package com.ampnet.auditornode.service.impl

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.ampnet.auditornode.configuration.AuditorConfiguration
import com.ampnet.auditornode.contract.ExampleStorageContractRPCConnector
import com.ampnet.auditornode.contract.ExampleStorageContractTransactionGenerator
import com.ampnet.auditornode.error.RpcError
import com.ampnet.auditornode.error.RpcError.ContractReadError
import com.ampnet.auditornode.error.Try
import com.ampnet.auditornode.persistence.model.IpfsHash
import com.ampnet.auditornode.service.ContractService
import org.kethereum.model.Address
import org.kethereum.model.Transaction
import org.kethereum.rpc.HttpEthereumRPC
import org.web3j.protocol.Web3j
import org.web3j.protocol.http.HttpService
import java.math.BigInteger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GethContractService @Inject constructor(
    private val auditorConfiguration: AuditorConfiguration
) : ContractService {

    private val web3 = Web3j.build(HttpService(auditorConfiguration.rpcUrl))
    private val rpc = HttpEthereumRPC(baseURL = auditorConfiguration.rpcUrl)
    private val contractAddress = Address(auditorConfiguration.contractAddress)

    override fun currentBlockNumber(): Try<BigInteger> =
        Either.catch {
            web3.ethBlockNumber().send().blockNumber
        }
            .mapLeft { RpcError.RpcConnectionError(auditorConfiguration.rpcUrl, it) }

    override fun getIpfsFileHash(): Try<IpfsHash> {
        val hash = ExampleStorageContractRPCConnector(contractAddress, rpc)
            .getHash()
            ?.right()
            ?.map { IpfsHash(it) }

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
