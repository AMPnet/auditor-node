package com.ampnet.auditornode

import com.ampnet.auditornode.contract.ExampleStorageContractRPCConnector
import com.ampnet.auditornode.scriptapi.Http
import com.ampnet.auditornode.scriptapi.JavaScriptApi
import org.graalvm.polyglot.Context
import org.graalvm.polyglot.HostAccess
import org.graalvm.polyglot.Source
import org.kethereum.model.Address
import org.kethereum.rpc.HttpEthereumRPC
import org.web3j.protocol.Web3j
import org.web3j.protocol.http.HttpService
import java.net.ConnectException

const val INFURA_RPC_URL = "https://ropsten.infura.io/v3/08664baf7af14eda956db2b71a79f12f"
const val LOCAL_RPC_URL = "http://localhost:8545"
const val CONTRACT_ADDRESS = "0x7FE38DAeeE945c53AC6CbbeD81c4e3cf7FF88Ac0"
const val IPFS_GATEWAY = "https://ipfs.io/ipfs/{ipfsHash}"
const val LOCAL_IPFS_CLIENT = "http://localhost:5001/api/v0/cat?arg={ipfsHash}"

fun main(args: Array<String>) {
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

    val ipfsBaseUrl = if (args.contains("--local-ipfs")) LOCAL_IPFS_CLIENT else IPFS_GATEWAY
    println("IPFS base URL: $ipfsBaseUrl")

    val fetchFromIpfs =
        if (args.contains("--local-ipfs")) {
            { hash: String -> Http.post(ipfsBaseUrl.replace("{ipfsHash}", hash)) }
        } else {
            { hash: String -> Http.get(ipfsBaseUrl.replace("{ipfsHash}", hash)) }
        }

    val ipfsFile = try {
        fetchFromIpfs(ipfsFileHash)
    } catch (e: ConnectException) {
        println("Cannot fetch file from IPFS client/gateway: $ipfsBaseUrl")
        throw e
    }

    requireNotNull(ipfsFile) { "No IPFS file content" }

    Context.newBuilder("js")
        .allowHostAccess(HostAccess.EXPLICIT)
        .allowHostClassLookup { fullClassName -> fullClassName.startsWith(JavaScriptApi::class.java.`package`.name) }
        .build()
        .use {
            val apiObjects = listOf(Http.createJavaScriptApiObject()).joinToString(separator = "\n")
            val scriptSource = "$apiObjects\n$ipfsFile"
            val source = Source.create("js", scriptSource)
            it.eval(source)
        }
}
