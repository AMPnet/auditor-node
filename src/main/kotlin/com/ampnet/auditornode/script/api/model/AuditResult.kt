package com.ampnet.auditornode.script.api.model

enum class AuditStatus {
    SUCCESS, FAILURE, ABORTED
}

sealed class AuditResult(val status: AuditStatus)

object SuccessfulAudit : AuditResult(AuditStatus.SUCCESS)

data class FailedAudit(val message: String) : AuditResult(AuditStatus.FAILURE)

data class AbortedAudit(val message: String) : AuditResult(AuditStatus.ABORTED)
