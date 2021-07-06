package com.ampnet.auditornode.model.response

import com.ampnet.auditornode.persistence.model.IpfsHash
import com.ampnet.auditornode.util.NativeReflection
import io.swagger.v3.oas.annotations.media.Schema

@Schema
@NativeReflection
data class IpfsFileUploadResponse(
    val fileName: String,
    val ipfsHash: IpfsHash
) {
    // Swagger does not handle Kotlin value classes at all, so this is needed
    // to trick it into generating the documentation properly
    @Suppress("unused")
    @Schema(type = "string")
    fun ipfsHash(): String = ipfsHash.value
}

@Schema
@NativeReflection
data class IpfsDirectoryUploadResponse(
    val files: List<IpfsFileUploadResponse>,
    val directoryIpfsHash: IpfsHash
) {
    // Swagger does not handle Kotlin value classes at all, so this is needed
    // to trick it into generating the documentation properly
    @Suppress("unused")
    @Schema(type = "string")
    fun directoryIpfsHash(): String = directoryIpfsHash.value
}
