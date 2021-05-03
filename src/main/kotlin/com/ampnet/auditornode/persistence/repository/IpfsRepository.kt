package com.ampnet.auditornode.persistence.repository

import com.ampnet.auditornode.error.Try
import com.ampnet.auditornode.persistence.model.IpfsHash
import com.ampnet.auditornode.persistence.model.IpfsTextFile

interface IpfsRepository {
    fun fetchTextFile(hash: IpfsHash): Try<IpfsTextFile>
}
