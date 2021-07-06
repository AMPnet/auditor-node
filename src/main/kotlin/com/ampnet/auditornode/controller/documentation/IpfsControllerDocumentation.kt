package com.ampnet.auditornode.controller.documentation

import com.ampnet.auditornode.model.response.IpfsDirectoryUploadResponse
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.Post
import io.micronaut.http.multipart.CompletedFileUpload
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.reactivestreams.Publisher

@Tag(name = "IPFS")
interface IpfsControllerDocumentation {

    @Get(value = "/{hash}", produces = [MediaType.APPLICATION_OCTET_STREAM])
    @Operation(
        summary = "Get a file from IPFS",
        description = "Fetches the file with provided hash from IPFS"
    )
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "Returns file content from IPFS as an octet stream",
            content = [
                Content(mediaType = MediaType.APPLICATION_OCTET_STREAM, schema = Schema(type = "string"))
            ]
        ),
        ApiResponse(
            responseCode = "404",
            description = "Returned when file with specified hash cannot be found on IPFS"
        )
    )
    fun getFile(@PathVariable("hash") @Parameter(description = "IPFS file hash") hash: String): HttpResponse<ByteArray>

    @Get(value = "/{directoryHash}/{fileName}", produces = [MediaType.APPLICATION_OCTET_STREAM])
    @Operation(
        summary = "Get a file from IPFS directory",
        description = "Fetches the file with provided name from IPFS directory with provided hash"
    )
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "Returns file content from IPFS directory as an octet stream",
            content = [
                Content(mediaType = MediaType.APPLICATION_OCTET_STREAM, schema = Schema(type = "string"))
            ]
        ),
        ApiResponse(
            responseCode = "404",
            description = "Returned when file with specified name cannot be found in specified IPFS directory"
        )
    )
    fun getFileFromDirectory(
        @PathVariable("directoryHash") @Parameter(description = "IPFS directory hash") directoryHash: String,
        @PathVariable("fileName") @Parameter(description = "IPFS file name") fileName: String
    ): HttpResponse<ByteArray>

    @Post(value = "/upload", consumes = [MediaType.MULTIPART_FORM_DATA], produces = [MediaType.APPLICATION_JSON])
    @Operation(
        summary = "Upload files to IPFS",
        description = "Uploads multiple files into an IPFS directory and returns the resulting IPFS hashes"
    )
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "Returns IPFS hashes of uploaded files and the IPFS hash for wrapping directory"
        ),
        ApiResponse(
            responseCode = "500",
            description = "Returned when file upload to IPFS fails for any reason"
        )
    )
    fun uploadFilesToDirectory(
        @Parameter(
            description = "Multipart request body which contains files which will be uploaded to IPFS"
        ) files: Publisher<CompletedFileUpload>
    ): Publisher<HttpResponse<IpfsDirectoryUploadResponse>>
}
