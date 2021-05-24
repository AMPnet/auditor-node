package com.ampnet.auditornode.service

import com.ampnet.auditornode.model.EthereumTransaction
import com.ampnet.auditornode.model.error.Try
import com.ampnet.auditornode.persistence.model.IpfsHash
import java.math.BigInteger

interface ContractService {
    fun currentBlockNumber(): Try<BigInteger>
    fun getIpfsDirectoryHash(): Try<IpfsHash>
    fun storeIpfsDirectoryHash(newHash: IpfsHash): EthereumTransaction
}
