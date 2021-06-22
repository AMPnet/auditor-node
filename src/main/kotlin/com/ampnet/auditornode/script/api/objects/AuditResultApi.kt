package com.ampnet.auditornode.script.api.objects

import com.ampnet.auditornode.script.api.model.AbortedAudit
import com.ampnet.auditornode.script.api.model.AuditResult
import com.ampnet.auditornode.script.api.model.FailedAudit
import com.ampnet.auditornode.script.api.model.SuccessfulAudit
import com.ampnet.auditornode.util.NativeReflection
import com.amptnet.auditornode.documentation.annotation.ScriptApi
import com.amptnet.auditornode.documentation.annotation.ScriptApiCategory
import com.amptnet.auditornode.documentation.annotation.ScriptFunction
import org.graalvm.polyglot.HostAccess.Export

@ScriptApi(
    description = "Model of the result that should be returned from the `audit()` function.",
    category = ScriptApiCategory.MODEL,
    hasStaticApi = true,
    apiObjectName = "AuditResult"
)
@NativeReflection
object AuditResultApi : JavaScriptApiObject {

    override fun apiObjectName(): String = "AuditResult"

    @Export
    @JvmStatic
    @ScriptFunction(
        description = "Used to create a successful `{apiObjectName}` object.",
        exampleCall = "`{apiObjectName}.success();`"
    )
    fun success(): AuditResult = SuccessfulAudit

    @Export
    @JvmStatic
    @ScriptFunction(
        description = "Used to create a failed `{apiObjectName}` object with provided message.",
        exampleCall = "`{apiObjectName}.failure(\"Owner mismatch\");`"
    )
    fun failure(message: String): AuditResult = FailedAudit(message)

    @Export
    @JvmStatic
    @ScriptFunction(
        description = "Used to create an aborted `{apiObjectName}` object with provided message.",
        exampleCall = "`{apiObjectName}.aborted(\"Will be audited later\");`"
    )
    fun aborted(message: String): AuditResult = AbortedAudit(message)
}
