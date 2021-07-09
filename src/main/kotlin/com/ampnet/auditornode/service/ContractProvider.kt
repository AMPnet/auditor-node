package com.ampnet.auditornode.service

import com.ampnet.auditornode.model.contract.ContractAddress

interface ContractProvider {
    fun getAssetHolderContract(contractAddress: ContractAddress): AssetHolderContractService
    fun getAssetListHolderContract(contractAddress: ContractAddress): AssetListHolderContractService
    fun getERC20Contract(contractAddress: ContractAddress): ERC20ContractService
}
