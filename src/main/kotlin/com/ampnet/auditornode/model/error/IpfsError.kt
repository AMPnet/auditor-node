package com.ampnet.auditornode.model.error

import com.ampnet.auditornode.persistence.model.IpfsHash

sealed class IpfsError(message: String, cause: Throwable? = null) : ApplicationError(message, cause) {

    data class IpfsHttpError(override val cause: Throwable) : IpfsError(
        message = "HTTP error while communicating with IPFS",
        cause = cause
    )

    data class IpfsEmptyResponseError(val hash: IpfsHash, val fileName: String? = null) : IpfsError(
        "Could not fetch file from IPFS: ${hash.value}" + (fileName?.let { "/$it" } ?: "")
    )

    object MissingUploadedIpfsDirectoryHash : IpfsError("Unable to determine IPFS directory hash from upload response")

    object UnsupportedIpfsOperationError : IpfsError("Requested operation is not supported by current IPFS provider")
}
