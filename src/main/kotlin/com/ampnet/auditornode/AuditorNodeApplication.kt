package com.ampnet.auditornode

import arrow.core.computations.either
import com.ampnet.auditornode.contract.ExampleStorageContractRPCConnector
import com.ampnet.auditornode.error.ApplicationError
import com.ampnet.auditornode.ipfs.GatewayIpfsClient
import com.ampnet.auditornode.ipfs.LocalIpfsClient
import com.ampnet.auditornode.script.api.AuditResult
import com.ampnet.auditornode.script.evaluation.JavaScriptEvaluator
import org.kethereum.model.Address
import org.kethereum.rpc.HttpEthereumRPC
import org.web3j.protocol.Web3j
import org.web3j.protocol.http.HttpService
import java.net.ConnectException

const val INFURA_RPC_URL = "https://ropsten.infura.io/v3/08664baf7af14eda956db2b71a79f12f"
const val LOCAL_RPC_URL = "http://localhost:8545"
const val CONTRACT_ADDRESS = "0x7FE38DAeeE945c53AC6CbbeD81c4e3cf7FF88Ac0"

suspend fun main(args: Array<String>) {
    val rpcBaseUrl = if (args.contains("--local-geth")) LOCAL_RPC_URL else INFURA_RPC_URL
    println("RPC base URL: $rpcBaseUrl")

    val web3 = Web3j.build(HttpService(rpcBaseUrl))

    try {
        val currentBlockNumber = web3.ethBlockNumber().send().blockNumber
        println("Current block number: $currentBlockNumber")
    } catch (e: ConnectException) {
        println("Cannot connect to RPC: $rpcBaseUrl")
        throw e
    }

    val ipfsFileHash = ExampleStorageContractRPCConnector(
        Address(CONTRACT_ADDRESS),
        HttpEthereumRPC(baseURL = rpcBaseUrl)
    )
        .hash()

    requireNotNull(ipfsFileHash) {
        "Could not retrieve IPFS file hash; make sure your local ethereum light client is " +
            "fully synced with Ropsten testnet"
    }

    println("Input IPFS hash: $ipfsFileHash")

    val ipfsClient = if (args.contains("--local-ipfs")) LocalIpfsClient else GatewayIpfsClient
    println("Using ${ipfsClient.javaClass.simpleName}")

    val result = either<ApplicationError, AuditResult> {
        val ipfsFile = ipfsClient.fetchFile(ipfsFileHash).bind()
        val evaluationResult = JavaScriptEvaluator.evaluate(ipfsFile).bind()
        evaluationResult
    }

    println(result)

}
