package com.ampnet.auditornode.model.error

import com.ampnet.auditornode.persistence.model.IpfsHash

sealed class IpfsError(message: String, cause: Throwable? = null) : ApplicationError(message, cause) {

    class IpfsHttpError(cause: Throwable) : IpfsError(
        message = "HTTP error while fetching file via IPFS",
        cause = cause
    )

    class IpfsEmptyResponseError(hash: IpfsHash) : IpfsError(
        "Could not fetch file from IPFS with hash: $hash"
    )
}
