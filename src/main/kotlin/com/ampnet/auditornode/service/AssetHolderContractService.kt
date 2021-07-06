package com.ampnet.auditornode.service

import com.ampnet.auditornode.model.contract.AssetId
import com.ampnet.auditornode.model.error.Try
import com.ampnet.auditornode.persistence.model.IpfsHash
import org.kethereum.model.Address

interface AssetHolderContractService {
    fun getAssetId(contractAddress: Address): Try<AssetId>
    fun getAssetInfoIpfsHash(contractAddress: Address): Try<IpfsHash>
}
