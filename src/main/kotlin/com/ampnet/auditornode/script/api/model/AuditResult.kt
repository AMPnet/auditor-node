package com.ampnet.auditornode.script.api.model

import com.ampnet.auditornode.util.NativeReflection

@NativeReflection
enum class AuditStatus {
    SUCCESS, FAILURE, ABORTED
}

@NativeReflection
sealed class AuditResult(val status: AuditStatus)

@NativeReflection
object SuccessfulAudit : AuditResult(AuditStatus.SUCCESS)

@NativeReflection
data class FailedAudit(val message: String) : AuditResult(AuditStatus.FAILURE)

@NativeReflection
data class AbortedAudit(val message: String) : AuditResult(AuditStatus.ABORTED)
