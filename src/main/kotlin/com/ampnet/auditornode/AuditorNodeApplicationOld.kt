//package com.ampnet.auditornode // TODO delete this file
//
//import arrow.core.computations.either
//import com.ampnet.auditornode.contract.ContractAbi
//import com.ampnet.auditornode.error.ApplicationError
//import com.ampnet.auditornode.persistence.repository.impl.GatewayIpfsRepository
//import com.ampnet.auditornode.persistence.repository.impl.LocalIpfsRepository
//import com.ampnet.auditornode.rpc.RpcClient
//import com.ampnet.auditornode.script.evaluation.JavaScriptEvaluator
//import org.komputing.khex.extensions.toHexString
//
//const val INFURA_RPC_URL = "https://ropsten.infura.io/v3/08664baf7af14eda956db2b71a79f12f"
//const val LOCAL_RPC_URL = "http://localhost:8545"
//const val TEST_SCRIPT_HASH = "QmSuwCUCZXzPunnrCWL7CnSLixboTa7HftVBjcVgi3TMaK"
//const val ANOTHER_SCRIPT_HASH = "QmUjWRsdGdWJ36bUAcdzNikTikXknWSW2VnLtAkKBiEf3d"
//
//suspend fun main(args: Array<String>) {
//    val rpcBaseUrl = if (args.contains("--local-geth")) LOCAL_RPC_URL else INFURA_RPC_URL
//    println("RPC base URL: $rpcBaseUrl")
//
//    val ipfsClient = if (args.contains("--local-ipfs")) LocalIpfsRepository() else GatewayIpfsRepository()
//    println("Using ${ipfsClient.javaClass.simpleName}")
//
//    val contractAbi = ContractAbi(rpcBaseUrl)
//    val rpcClient = RpcClient(rpcBaseUrl)
//
//    val result = either<ApplicationError, String> {
//        val currentBlockNumber = rpcClient.currentBlockNumber().bind()
//        println("Current block number: $currentBlockNumber")
//        val ipfsFileHash = contractAbi.getIpfsFileHash().bind()
//        println("Input IPFS hash: $ipfsFileHash")
//        val ipfsFile = ipfsClient.fetchTextFile(ipfsFileHash).bind()
//        val evaluationResult = JavaScriptEvaluator.evaluate(ipfsFile).bind()
//
//        val newIpfsFileHash = if (evaluationResult.success) {
//            ANOTHER_SCRIPT_HASH
//        } else {
//            TEST_SCRIPT_HASH
//        }
//
//        val transaction = contractAbi.storeIpfsFileHash(newIpfsFileHash)
//        """{"to":"${transaction.to}","data":"${transaction.input.toHexString()}"}"""
//    }
//
//    println(result)
//}
