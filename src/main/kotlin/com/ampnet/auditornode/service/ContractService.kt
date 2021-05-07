package com.ampnet.auditornode.service

import com.ampnet.auditornode.model.EthereumTransaction
import com.ampnet.auditornode.model.error.Try
import com.ampnet.auditornode.persistence.model.IpfsHash
import java.math.BigInteger

interface ContractService {
    fun currentBlockNumber(): Try<BigInteger>
    fun getIpfsFileHash(): Try<IpfsHash>
    fun storeIpfsFileHash(newHash: IpfsHash): EthereumTransaction
}
