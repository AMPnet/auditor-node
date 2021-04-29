package com.ampnet.auditornode.ipfs

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import com.ampnet.auditornode.error.IpfsError.IpfsEmptyResponseError
import com.ampnet.auditornode.error.IpfsError.IpfsHttpError
import com.ampnet.auditornode.error.Try
import com.ampnet.auditornode.script.api.Http

object LocalIpfsClient : IpfsClient {

    private const val URL = "http://localhost:5001/api/v0/cat?arg={ipfsHash}"

    override fun fetchFile(hash: String): Try<String> =
        Either.catch {
            Http.post(URL.replace("{ipfsHash}", hash))
        }
            .mapLeft { IpfsHttpError(it) }
            .flatMap {
                when (it) {
                    null -> IpfsEmptyResponseError(hash).left()
                    else -> it.right()
                }
            }
}
