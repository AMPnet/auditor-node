package com.ampnet.auditornode.script.api.objects

import com.ampnet.auditornode.script.api.model.AbortedAudit
import com.ampnet.auditornode.script.api.model.AuditResult
import com.ampnet.auditornode.script.api.model.FailedAudit
import com.ampnet.auditornode.script.api.model.SuccessfulAudit
import org.graalvm.polyglot.HostAccess.Export

object AuditResultApi : JavaScriptApiObject {

    override fun apiObjectName(): String = "AuditResult"

    @Export
    @JvmStatic
    fun success(): AuditResult = SuccessfulAudit

    @Export
    @JvmStatic
    fun failure(message: String): AuditResult = FailedAudit(message)

    @Export
    @JvmStatic
    fun aborted(message: String): AuditResult = AbortedAudit(message)
}
