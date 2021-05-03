package com.ampnet.auditornode.persistence.repository.impl

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import com.ampnet.auditornode.configuration.ProgramArgumentPropertyNames
import com.ampnet.auditornode.error.IpfsError.IpfsEmptyResponseError
import com.ampnet.auditornode.error.IpfsError.IpfsHttpError
import com.ampnet.auditornode.error.Try
import com.ampnet.auditornode.persistence.model.IpfsHash
import com.ampnet.auditornode.persistence.model.IpfsTextFile
import com.ampnet.auditornode.persistence.repository.IpfsRepository
import com.ampnet.auditornode.script.api.Http
import io.micronaut.context.annotation.Requires
import javax.inject.Singleton

@Singleton
@Requires(property = ProgramArgumentPropertyNames.USE_LOCAL_IPFS)
class LocalIpfsRepository : IpfsRepository {

    companion object {
        private const val URL = "http://localhost:5001/api/v0/cat?arg={ipfsHash}"
    }

    override fun fetchTextFile(hash: IpfsHash): Try<IpfsTextFile> =
        Either.catch {
            Http.post(URL.replace("{ipfsHash}", hash.value))
        }
            .mapLeft { IpfsHttpError(it) }
            .flatMap { it?.right() ?: IpfsEmptyResponseError(hash).left() }
            .map { IpfsTextFile(it) }
}
