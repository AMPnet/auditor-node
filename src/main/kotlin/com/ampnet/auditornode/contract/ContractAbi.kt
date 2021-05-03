package com.ampnet.auditornode.contract

import arrow.core.left
import arrow.core.right
import com.ampnet.auditornode.model.error.RpcError.ContractReadError
import com.ampnet.auditornode.model.error.Try
import org.kethereum.model.Address
import org.kethereum.model.Transaction
import org.kethereum.rpc.HttpEthereumRPC

@Deprecated(message = "for removal")
class ContractAbi(rpcBaseUrl: String) { // TODO delete

    private val contractAddress = Address("0x992E8FeA2D91807797717178Aa6abEc7F20c31a8")
    private val rpc = HttpEthereumRPC(baseURL = rpcBaseUrl)

    fun getIpfsFileHash(): Try<String> {
        val hash = ExampleStorageContractRPCConnector(contractAddress, rpc)
            .getHash()

        return when (hash) {
            null -> ContractReadError(
                "Could not retrieve IPFS file hash; make sure your local ethereum light client is " +
                    "fully synced with Ropsten testnet"
            ).left()
            else -> hash.right()
        }
    }

    fun storeIpfsFileHash(newHash: String): Transaction =
        ExampleStorageContractTransactionGenerator(contractAddress)
            .updateHash(newHash)
}
