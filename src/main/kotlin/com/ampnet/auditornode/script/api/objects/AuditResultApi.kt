package com.ampnet.auditornode.script.api.objects

import com.ampnet.auditornode.script.api.model.AuditResult
import org.graalvm.polyglot.HostAccess.Export

object AuditResultApi : JavaScriptApiObject {

    override fun apiObjectName(): String = "AuditResult"

    @Export
    @JvmStatic
    fun of(success: Boolean): AuditResult = AuditResult(success)
}
