package com.ampnet.auditornode.controller

import com.ampnet.auditornode.controller.documentation.IpfsControllerDocumentation
import com.ampnet.auditornode.model.response.IpfsDirectoryUploadResponse
import com.ampnet.auditornode.persistence.model.IpfsHash
import com.ampnet.auditornode.persistence.model.NamedIpfsFile
import com.ampnet.auditornode.persistence.repository.IpfsRepository
import com.ampnet.auditornode.util.UuidProvider
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.multipart.CompletedFileUpload
import mu.KotlinLogging
import org.reactivestreams.Publisher
import reactor.core.publisher.Flux
import javax.inject.Inject

private val logger = KotlinLogging.logger {}

@Controller("/ipfs")
class IpfsController @Inject constructor(
    private val ipfsRepository: IpfsRepository,
    private val uuidProvider: UuidProvider
) : IpfsControllerDocumentation {

    override fun getFile(hash: String): HttpResponse<ByteArray> {
        logger.info { "IPFS file request: $hash" }
        return ipfsRepository.fetchBinaryFile(IpfsHash(hash))
            .fold(
                ifLeft = {
                    logger.error(it) { "IPFS file not found: $hash" }
                    HttpResponse.notFound()
                },
                ifRight = { HttpResponse.ok(it.content) }
            )
    }

    override fun getFileFromDirectory(directoryHash: String, fileName: String): HttpResponse<ByteArray> {
        logger.info { "IPFS file from directory request: $directoryHash/$fileName" }
        return ipfsRepository.fetchBinaryFileFromDirectory(IpfsHash(directoryHash), fileName)
            .fold(
                ifLeft = {
                    logger.error(it) { "IPFS file from directory not found: $directoryHash/$fileName" }
                    HttpResponse.notFound()
                },
                ifRight = { HttpResponse.ok(it.content) }
            )
    }

    override fun uploadFilesToDirectory(
        files: Publisher<CompletedFileUpload>
    ): Publisher<HttpResponse<IpfsDirectoryUploadResponse>> {
        logger.info { "Uploading files to IPFS" }

        val filesFlux = Flux.from(files).map {
            val namedFile = NamedIpfsFile(it.bytes, it.filename ?: uuidProvider.getUuid().toString())
            logger.debug { "File to upload: ${namedFile.fileName}" }
            namedFile
        }

        return ipfsRepository.uploadFilesToDirectory(filesFlux)
            .map { either ->
                either.fold(
                    ifLeft = {
                        logger.error(it) { "Cannot upload files to IPFS" }
                        HttpResponse.serverError()
                    },
                    ifRight = { HttpResponse.ok(it) }
                )
            }
    }
}
