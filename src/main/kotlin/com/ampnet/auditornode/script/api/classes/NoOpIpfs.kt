package com.ampnet.auditornode.script.api.classes

import org.graalvm.polyglot.HostAccess.Export

object NoOpIpfs : Ipfs {

    @Export
    override fun getFile(fileName: String): String? = null
}
