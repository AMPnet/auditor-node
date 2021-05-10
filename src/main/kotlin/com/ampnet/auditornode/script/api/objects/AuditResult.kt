package com.ampnet.auditornode.script.api.objects

import org.graalvm.polyglot.HostAccess.Export

data class AuditResult(val success: Boolean)

object AuditResultApi : JavaScriptApiObject {

    override fun apiObjectName(): String = "AuditResult"

    @Export
    @JvmStatic
    fun of(success: Boolean): AuditResult = AuditResult(success)
}
