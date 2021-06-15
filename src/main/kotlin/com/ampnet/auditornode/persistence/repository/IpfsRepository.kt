package com.ampnet.auditornode.persistence.repository

import com.ampnet.auditornode.model.error.Try
import com.ampnet.auditornode.persistence.model.IpfsBinaryFile
import com.ampnet.auditornode.persistence.model.IpfsHash
import com.ampnet.auditornode.persistence.model.IpfsTextFile

interface IpfsRepository {
    fun fetchTextFile(hash: IpfsHash): Try<IpfsTextFile>
    fun fetchBinaryFile(hash: IpfsHash): Try<IpfsBinaryFile>
    fun fetchTextFileFromDirectory(directoryHash: IpfsHash, fileName: String): Try<IpfsTextFile>
    fun fetchBinaryFileFromDirectory(directoryHash: IpfsHash, fileName: String): Try<IpfsBinaryFile>
}
