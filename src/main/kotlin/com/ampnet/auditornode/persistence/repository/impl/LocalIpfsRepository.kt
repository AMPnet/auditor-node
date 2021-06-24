package com.ampnet.auditornode.persistence.repository.impl

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import com.ampnet.auditornode.configuration.properties.IpfsProperties
import com.ampnet.auditornode.configuration.properties.ProgramArgumentPropertyNames
import com.ampnet.auditornode.model.error.IpfsError.IpfsEmptyResponseError
import com.ampnet.auditornode.model.error.IpfsError.IpfsHttpError
import com.ampnet.auditornode.model.error.IpfsError.MissingUploadedIpfsDirectoryHash
import com.ampnet.auditornode.model.error.Try
import com.ampnet.auditornode.model.response.IpfsDirectoryUploadResponse
import com.ampnet.auditornode.model.response.IpfsFileUploadResponse
import com.ampnet.auditornode.persistence.model.IpfsBinaryFile
import com.ampnet.auditornode.persistence.model.IpfsHash
import com.ampnet.auditornode.persistence.model.IpfsTextFile
import com.ampnet.auditornode.persistence.model.NamedIpfsFile
import com.ampnet.auditornode.persistence.repository.IpfsRepository
import com.fasterxml.jackson.databind.ObjectMapper
import io.micronaut.context.annotation.Requires
import io.micronaut.http.HttpRequest
import io.micronaut.http.MediaType
import io.micronaut.http.client.BlockingHttpClient
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.multipart.MultipartBody
import mu.KotlinLogging
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import javax.inject.Inject
import javax.inject.Singleton

private val logger = KotlinLogging.logger {}

@Singleton
@Requires(property = ProgramArgumentPropertyNames.USE_LOCAL_IPFS)
class LocalIpfsRepository @Inject constructor(
    private val ipfsProperties: IpfsProperties,
    private val blockingHttpClient: BlockingHttpClient,
    private val httpClient: HttpClient,
    private val objectMapper: ObjectMapper
) : IpfsRepository {

    override fun fetchTextFile(hash: IpfsHash): Try<IpfsTextFile> =
        fetchFile(hash, String::class.java, ::IpfsTextFile)

    override fun fetchBinaryFile(hash: IpfsHash): Try<IpfsBinaryFile> =
        fetchFile(hash, ByteArray::class.java, ::IpfsBinaryFile)

    override fun fetchTextFileFromDirectory(directoryHash: IpfsHash, fileName: String): Try<IpfsTextFile> =
        fetchFileFromDirectory(directoryHash, fileName, String::class.java, ::IpfsTextFile)

    override fun fetchBinaryFileFromDirectory(directoryHash: IpfsHash, fileName: String): Try<IpfsBinaryFile> =
        fetchFileFromDirectory(directoryHash, fileName, ByteArray::class.java, ::IpfsBinaryFile)

    override fun uploadFilesToDirectory(files: Flux<NamedIpfsFile>): Mono<Try<IpfsDirectoryUploadResponse>> {
        return files.collectList().flatMap {
            val requestBody = MultipartBody.builder()

            it.forEach { upload -> requestBody.addPart("file", upload.fileName, upload.content) }

            val request = HttpRequest.POST(
                "http://localhost:${ipfsProperties.localClientPort}/api/v0/add" +
                    "?quieter=true&wrap-with-directory=true&pin=true",
                requestBody.build()
            ).apply {
                contentType(MediaType.MULTIPART_FORM_DATA_TYPE)
            }

            Mono.from(httpClient.retrieve(request, ByteArray::class.java))
                .map<Try<IpfsDirectoryUploadResponse>> { response ->
                    val responses = String(response).split("\n")
                        .map(String::trim)
                        .filter(String::isNotEmpty)
                        .map(objectMapper::readTree)
                        .mapNotNull { json ->
                            val fileName = json["Name"]?.asText()
                            val ipfsHash = json["Hash"]?.asText()?.let(::IpfsHash)

                            if (fileName != null && ipfsHash != null) {
                                IpfsFileUploadResponse(fileName, ipfsHash)
                            } else {
                                null
                            }
                        }

                    val (directoryResponse, fileResponses) = responses.partition { r -> r.fileName.isEmpty() }

                    if (directoryResponse.size != 1) {
                        MissingUploadedIpfsDirectoryHash.left()
                    } else {
                        IpfsDirectoryUploadResponse(fileResponses, directoryResponse[0].ipfsHash).right()
                    }
                }
                .onErrorResume { e -> Mono.just(IpfsHttpError(e).left()) }
        }
    }

    private fun <R, F> fetchFile(hash: IpfsHash, bodyType: Class<R>, wrapper: (R) -> F): Try<F> =
        Either.catch {
            val fileUrl = "http://localhost:${ipfsProperties.localClientPort}/api/v0/cat?arg=${hash.value}"
            logger.info { "Fetching file from IPFS: POST $fileUrl" }
            val request = HttpRequest.POST(fileUrl, "")
            blockingHttpClient.retrieve(request, bodyType)
        }
            .mapLeft { IpfsHttpError(it) }
            .flatMap { it?.right() ?: IpfsEmptyResponseError(hash).left() }
            .map { wrapper(it) }

    private fun <R, F> fetchFileFromDirectory(
        directoryHash: IpfsHash,
        fileName: String,
        bodyType: Class<R>,
        wrapper: (R) -> F
    ): Try<F> =
        Either.catch {
            val port = ipfsProperties.localClientPort
            val fileUrl = "http://localhost:$port/api/v0/cat?arg=${directoryHash.value}/$fileName"
            logger.info { "Fetching file from IPFS folder: POST $fileUrl" }
            val request = HttpRequest.POST(fileUrl, "")
            blockingHttpClient.retrieve(request, bodyType)
        }
            .mapLeft { IpfsHttpError(it) }
            .flatMap { it?.right() ?: IpfsEmptyResponseError(directoryHash, fileName).left() }
            .map { wrapper(it) }
}
