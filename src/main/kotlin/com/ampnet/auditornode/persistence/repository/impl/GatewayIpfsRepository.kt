package com.ampnet.auditornode.persistence.repository.impl

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import com.ampnet.auditornode.configuration.properties.IpfsProperties
import com.ampnet.auditornode.configuration.properties.ProgramArgumentPropertyNames
import com.ampnet.auditornode.model.error.IpfsError.IpfsEmptyResponseError
import com.ampnet.auditornode.model.error.IpfsError.IpfsHttpError
import com.ampnet.auditornode.model.error.Try
import com.ampnet.auditornode.persistence.model.IpfsHash
import com.ampnet.auditornode.persistence.model.IpfsTextFile
import com.ampnet.auditornode.persistence.repository.IpfsRepository
import io.micronaut.context.annotation.Requires
import io.micronaut.http.HttpRequest
import io.micronaut.http.client.BlockingHttpClient
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@Requires(missingProperty = ProgramArgumentPropertyNames.USE_LOCAL_IPFS)
class GatewayIpfsRepository @Inject constructor(
    private val ipfsProperties: IpfsProperties,
    private val blockingHttpClient: BlockingHttpClient
) : IpfsRepository {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun fetchTextFile(hash: IpfsHash): Try<IpfsTextFile> =
        Either.catch {
            val fileUrl = ipfsProperties.gatewayUrl.replace("{ipfsHash}", hash.value)
            log.info("Fetching file from IPFS: GET {}", fileUrl)
            val request = HttpRequest.GET<String>(fileUrl)
            blockingHttpClient.retrieve(request)
        }
            .mapLeft { IpfsHttpError(it) }
            .flatMap { it?.right() ?: IpfsEmptyResponseError(hash).left() }
            .map { IpfsTextFile(it) }
}
