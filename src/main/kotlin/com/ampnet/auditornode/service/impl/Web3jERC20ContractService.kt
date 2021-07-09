package com.ampnet.auditornode.service.impl

import com.ampnet.auditornode.configuration.properties.RpcProperties
import com.ampnet.auditornode.contract.ERC20
import com.ampnet.auditornode.model.contract.Balance
import com.ampnet.auditornode.model.contract.ContractAddress
import com.ampnet.auditornode.model.contract.EthereumAddress
import com.ampnet.auditornode.model.error.Try
import com.ampnet.auditornode.service.AbstractWeb3jContractService
import com.ampnet.auditornode.service.ERC20ContractService
import mu.KotlinLogging
import org.web3j.protocol.Web3j
import org.web3j.tx.ReadonlyTransactionManager
import org.web3j.tx.gas.DefaultGasProvider

private val logger = KotlinLogging.logger {}

class Web3jERC20ContractService constructor(
    web3j: Web3j,
    rpcProperties: RpcProperties,
    override val contractAddress: ContractAddress
) : AbstractWeb3jContractService(logger, rpcProperties), ERC20ContractService {

    private class Contract(override val contractAddress: ContractAddress, web3j: Web3j) : IContract, ERC20(
        contractAddress.value,
        web3j,
        ReadonlyTransactionManager(web3j, contractAddress.value),
        DefaultGasProvider()
    )

    override val contractName: String = "ERC20"

    private val contract by lazy {
        Contract(contractAddress, web3j)
    }

    override fun getBalanceOf(address: EthereumAddress): Try<Balance> =
        getValueFromContract(
            "balance for ethereum address ${address.value}",
            contract,
            { it.balanceOf(address.value).send() },
            ::Balance
        )
}
