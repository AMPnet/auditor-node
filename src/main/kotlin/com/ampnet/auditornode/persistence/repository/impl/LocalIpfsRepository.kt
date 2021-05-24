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
import mu.KotlinLogging
import javax.inject.Inject
import javax.inject.Singleton

private val logger = KotlinLogging.logger {}

@Singleton
@Requires(property = ProgramArgumentPropertyNames.USE_LOCAL_IPFS)
class LocalIpfsRepository @Inject constructor(
    private val ipfsProperties: IpfsProperties,
    private val blockingHttpClient: BlockingHttpClient
) : IpfsRepository {

    override fun fetchTextFile(hash: IpfsHash): Try<IpfsTextFile> =
        Either.catch {
            val fileUrl = "http://localhost:${ipfsProperties.localClientPort}/api/v0/cat?arg=${hash.value}"
            logger.info { "Fetching file from IPFS: POST $fileUrl" }
            val request = HttpRequest.POST(fileUrl, "")
            blockingHttpClient.retrieve(request)
        }
            .mapLeft { IpfsHttpError(it) }
            .flatMap { it?.right() ?: IpfsEmptyResponseError(hash).left() }
            .map { IpfsTextFile(it) }

    override fun fetchTextFileFromDirectory(directoryHash: IpfsHash, fileName: String): Try<IpfsTextFile> =
        Either.catch {
            val port = ipfsProperties.localClientPort
            val fileUrl = "http://localhost:$port/api/v0/cat?arg=${directoryHash.value}/$fileName"
            logger.info { "Fetching file from IPFS folder: POST $fileUrl" }
            val request = HttpRequest.POST(fileUrl, "")
            blockingHttpClient.retrieve(request)
        }
            .mapLeft { IpfsHttpError(it) }
            .flatMap { it?.right() ?: IpfsEmptyResponseError(directoryHash, fileName).left() }
            .map { IpfsTextFile(it) }
}
