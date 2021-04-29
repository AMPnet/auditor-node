package com.ampnet.auditornode

import arrow.core.computations.either
import com.ampnet.auditornode.contract.ContractAbi
import com.ampnet.auditornode.error.ApplicationError
import com.ampnet.auditornode.ipfs.GatewayIpfsClient
import com.ampnet.auditornode.ipfs.LocalIpfsClient
import com.ampnet.auditornode.rpc.RpcClient
import com.ampnet.auditornode.script.evaluation.JavaScriptEvaluator
import org.kethereum.model.Transaction

const val INFURA_RPC_URL = "https://ropsten.infura.io/v3/08664baf7af14eda956db2b71a79f12f"
const val LOCAL_RPC_URL = "http://localhost:8545"

suspend fun main(args: Array<String>) {
    val rpcBaseUrl = if (args.contains("--local-geth")) LOCAL_RPC_URL else INFURA_RPC_URL
    println("RPC base URL: $rpcBaseUrl")

    val ipfsClient = if (args.contains("--local-ipfs")) LocalIpfsClient else GatewayIpfsClient
    println("Using ${ipfsClient.javaClass.simpleName}")

    val contractAbi = ContractAbi(rpcBaseUrl)
    val rpcClient = RpcClient(rpcBaseUrl)

    val result = either<ApplicationError, Transaction> {
        val currentBlockNumber = rpcClient.currentBlockNumber().bind()
        println("Current block number: $currentBlockNumber")
        val ipfsFileHash = contractAbi.getIpfsFileHash().bind()
        println("Input IPFS hash: $ipfsFileHash")
        val ipfsFile = ipfsClient.fetchFile(ipfsFileHash).bind()
        val evaluationResult = JavaScriptEvaluator.evaluate(ipfsFile).bind()
        contractAbi.storeIpfsFileHash("${evaluationResult.success}")
    }

    println(result)
}
