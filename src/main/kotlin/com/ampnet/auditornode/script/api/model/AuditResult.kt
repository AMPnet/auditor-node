package com.ampnet.auditornode.script.api.model

import com.ampnet.auditornode.util.NativeReflection
import io.swagger.v3.oas.annotations.media.Schema

@Schema
@NativeReflection
enum class AuditStatus {
    SUCCESS, FAILURE, ABORTED
}

@Schema(oneOf = [SuccessfulAudit::class, FailedAudit::class, AbortedAudit::class])
@NativeReflection
sealed class AuditResult(val status: AuditStatus)

@Schema
@NativeReflection
object SuccessfulAudit : AuditResult(AuditStatus.SUCCESS)

@Schema
@NativeReflection
data class FailedAudit(val message: String) : AuditResult(AuditStatus.FAILURE)

@Schema
@NativeReflection
data class AbortedAudit(val message: String) : AuditResult(AuditStatus.ABORTED)
