package com.ampnet.auditornode.ipfs

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import com.ampnet.auditornode.error.IpfsError
import com.ampnet.auditornode.error.Try
import com.ampnet.auditornode.script.api.Http

object GatewayIpfsClient : IpfsClient {

    private const val URL = "https://ipfs.io/ipfs/{ipfsHash}"

    override fun fetchFile(hash: String): Try<String> =
        Either.catch {
            Http.get(URL.replace("{ipfsHash}", hash))
        }
            .mapLeft { IpfsError.IpfsHttpError(it) }
            .flatMap {
                when (it) {
                    null -> IpfsError.IpfsEmptyResponseError(hash).left()
                    else -> it.right()
                }
            }
}
