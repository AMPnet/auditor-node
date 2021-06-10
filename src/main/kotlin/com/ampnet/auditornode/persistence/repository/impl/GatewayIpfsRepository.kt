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
import com.ampnet.auditornode.persistence.model.IpfsBinaryFile
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
@Requires(missingProperty = ProgramArgumentPropertyNames.USE_LOCAL_IPFS)
class GatewayIpfsRepository @Inject constructor(
    private val ipfsProperties: IpfsProperties,
    private val blockingHttpClient: BlockingHttpClient
) : IpfsRepository {

    private fun <R, F> fetchFile(hash: IpfsHash, bodyType: Class<R>, wrapper: (R) -> F): Try<F> =
        Either.catch {
            val fileUrl = ipfsProperties.gatewayUrl.replace("{ipfsHash}", hash.value)
            logger.info { "Fetching file from IPFS: GET $fileUrl" }
            val request = HttpRequest.GET<R>(fileUrl)
            blockingHttpClient.retrieve(request, bodyType)
        }
            .mapLeft { IpfsHttpError(it) }
            .flatMap { it?.right() ?: IpfsEmptyResponseError(hash).left() }
            .map { wrapper(it) }

    override fun fetchTextFile(hash: IpfsHash): Try<IpfsTextFile> =
        fetchFile(hash, String::class.java, ::IpfsTextFile)

    override fun fetchBinaryFile(hash: IpfsHash): Try<IpfsBinaryFile> =
        fetchFile(hash, ByteArray::class.java, ::IpfsBinaryFile)

    fun <R, F> fetchFromDirectory(
        directoryHash: IpfsHash,
        fileName: String,
        bodyType: Class<R>,
        wrapper: (R) -> F
    ): Try<F> =
        Either.catch {
            val fileUrl = "${ipfsProperties.gatewayUrl.removeSuffix("/")}/$fileName"
                .replace("{ipfsHash}", directoryHash.value)
            logger.info { "Fetching file from IPFS folder: GET $fileUrl" }
            val request = HttpRequest.GET<R>(fileUrl)
            blockingHttpClient.retrieve(request, bodyType)
        }
            .mapLeft { IpfsHttpError(it) }
            .flatMap { it?.right() ?: IpfsEmptyResponseError(directoryHash, fileName).left() }
            .map { wrapper(it) }

    override fun fetchTextFileFromDirectory(directoryHash: IpfsHash, fileName: String): Try<IpfsTextFile> =
        fetchFromDirectory(directoryHash, fileName, String::class.java, ::IpfsTextFile)

    override fun fetchBinaryFileFromDirectory(directoryHash: IpfsHash, fileName: String): Try<IpfsBinaryFile> =
        fetchFromDirectory(directoryHash, fileName, ByteArray::class.java, ::IpfsBinaryFile)
}
