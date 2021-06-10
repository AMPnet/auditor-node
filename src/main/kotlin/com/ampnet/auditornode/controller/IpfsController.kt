package com.ampnet.auditornode.controller

import com.ampnet.auditornode.persistence.model.IpfsHash
import com.ampnet.auditornode.persistence.repository.IpfsRepository
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import mu.KotlinLogging
import javax.inject.Inject

private val logger = KotlinLogging.logger {}

@Controller("/ipfs")
class IpfsController @Inject constructor(
    private val ipfsRepository: IpfsRepository
) {

    @Get(value = "/{hash}", produces = [MediaType.APPLICATION_OCTET_STREAM])
    fun getFile(@PathVariable("hash") hash: String): HttpResponse<ByteArray> {
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

    @Get(value = "/{directoryHash}/{fileName}", produces = [MediaType.APPLICATION_OCTET_STREAM])
    fun getFileFromDirectory(
        @PathVariable("directoryHash") directoryHash: String,
        @PathVariable("fileName") fileName: String
    ): HttpResponse<ByteArray> {
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
