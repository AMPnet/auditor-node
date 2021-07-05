package com.ampnet.auditornode.service

import com.ampnet.auditornode.model.contract.Balance
import com.ampnet.auditornode.model.contract.EthereumAddress
import com.ampnet.auditornode.model.error.Try

interface ERC20ContractService {
    fun getBalanceOf(address: EthereumAddress): Try<Balance>
}
