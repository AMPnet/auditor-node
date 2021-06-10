package com.ampnet.auditornode.script.api.classes

import com.ampnet.auditornode.util.NativeReflection
import org.graalvm.polyglot.HostAccess.Export

@NativeReflection
object NoOpIpfs : Ipfs {

    @Export
    override fun getFile(fileName: String): String? = null

    @Export
    override fun linkToFile(fileName: String): String? = null
}
