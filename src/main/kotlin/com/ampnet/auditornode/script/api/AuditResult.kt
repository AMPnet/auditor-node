package com.ampnet.auditornode.script.api

import org.graalvm.polyglot.HostAccess.Export

data class AuditResult(val success: Boolean)

object AuditResultApi : JavaScriptApi {

    override fun apiObjectName(): String = "AuditResult"

    @Export
    @JvmStatic
    fun of(success: Boolean): AuditResult = AuditResult(success)
}
