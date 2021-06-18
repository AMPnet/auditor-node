package com.ampnet.auditornode.controller

import com.ampnet.auditornode.controller.documentation.IpfsControllerDocumentation
import com.ampnet.auditornode.persistence.model.IpfsHash
import com.ampnet.auditornode.persistence.repository.IpfsRepository
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Controller
import mu.KotlinLogging
import javax.inject.Inject

private val logger = KotlinLogging.logger {}

@Controller("/ipfs")
class IpfsController @Inject constructor(
    private val ipfsRepository: IpfsRepository
) : IpfsControllerDocumentation {

    override fun getFile(hash: String): HttpResponse<ByteArray> {
        logger.info { "IPFS file request: $hash" }
        return ipfsRepository.fetchBinaryFile(IpfsHash(hash))
            .fold(
                ifLeft = {
                    logger.error { "IPFS file not found: $hash" }
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
                    logger.error { "IPFS file from directory not found: $directoryHash/$fileName" }
                    HttpResponse.notFound()
                },
                ifRight = { HttpResponse.ok(it.content) }
            )
    }
}
