package com.ampnet.auditornode.persistence.repository.impl

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import com.ampnet.auditornode.configuration.ProgramArgumentPropertyNames
import com.ampnet.auditornode.error.IpfsError
import com.ampnet.auditornode.error.IpfsError.IpfsEmptyResponseError
import com.ampnet.auditornode.error.Try
import com.ampnet.auditornode.persistence.model.IpfsHash
import com.ampnet.auditornode.persistence.model.IpfsTextFile
import com.ampnet.auditornode.persistence.repository.IpfsRepository
import com.ampnet.auditornode.script.api.Http
import io.micronaut.context.annotation.Requires
import javax.inject.Singleton

@Singleton
@Requires(missingProperty = ProgramArgumentPropertyNames.USE_LOCAL_IPFS)
class GatewayIpfsRepository : IpfsRepository {

    companion object {
        private const val URL = "https://ipfs.io/ipfs/{ipfsHash}"
    }

    override fun fetchTextFile(hash: IpfsHash): Try<IpfsTextFile> =
        Either.catch {
            Http.get(URL.replace("{ipfsHash}", hash.value))
        }
            .mapLeft { IpfsError.IpfsHttpError(it) }
            .flatMap { it?.right() ?: IpfsEmptyResponseError(hash).left() }
            .map { IpfsTextFile(it) }
}
