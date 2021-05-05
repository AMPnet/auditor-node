package com.ampnet.auditornode.persistence.repository.impl

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import com.ampnet.auditornode.configuration.ProgramArgumentPropertyNames
import com.ampnet.auditornode.model.error.IpfsError
import com.ampnet.auditornode.model.error.IpfsError.IpfsEmptyResponseError
import com.ampnet.auditornode.model.error.Try
import com.ampnet.auditornode.model.script.Http
import com.ampnet.auditornode.persistence.model.IpfsHash
import com.ampnet.auditornode.persistence.model.IpfsTextFile
import com.ampnet.auditornode.persistence.repository.IpfsRepository
import io.micronaut.context.annotation.Requires
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@Requires(missingProperty = ProgramArgumentPropertyNames.USE_LOCAL_IPFS)
class GatewayIpfsRepository @Inject constructor(private val http: Http) : IpfsRepository {

    companion object {
        private const val URL = "https://ipfs.io/ipfs/{ipfsHash}"
    }

    private val log = LoggerFactory.getLogger(javaClass)

    override fun fetchTextFile(hash: IpfsHash): Try<IpfsTextFile> =
        Either.catch {
            val url = URL.replace("{ipfsHash}", hash.value)
            log.info("Fetching file from IPFS: GET {}", url)
            http.get(url)
        }
            .mapLeft { IpfsError.IpfsHttpError(it) }
            .flatMap { it?.right() ?: IpfsEmptyResponseError(hash).left() }
            .map { IpfsTextFile(it) }
}
