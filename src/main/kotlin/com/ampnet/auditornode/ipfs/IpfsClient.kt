package com.ampnet.auditornode.ipfs

import com.ampnet.auditornode.error.Try

interface IpfsClient {

    fun fetchFile(hash: String): Try<String>
}
