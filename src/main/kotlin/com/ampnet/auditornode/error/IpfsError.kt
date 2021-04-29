package com.ampnet.auditornode.error

sealed class IpfsError(message: String, cause: Throwable? = null) : ApplicationError(message, cause) {

    class IpfsHttpError(cause: Throwable) : IpfsError(
        message = "HTTP error while fetching file via IPFS",
        cause = cause
    )

    class IpfsEmptyResponseError(hash: String) : IpfsError(
        "Could not fetch file from IPFS with hash: $hash"
    )
}
