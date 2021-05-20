package com.ampnet.auditornode.persistence.repository

import com.ampnet.auditornode.model.error.Try
import com.ampnet.auditornode.persistence.model.IpfsHash
import com.ampnet.auditornode.persistence.model.IpfsTextFile

interface IpfsRepository {
    fun fetchTextFile(hash: IpfsHash): Try<IpfsTextFile>
    fun fetchTextFileFromDirectory(directoryHash: IpfsHash, fileName: String): Try<IpfsTextFile>
}
