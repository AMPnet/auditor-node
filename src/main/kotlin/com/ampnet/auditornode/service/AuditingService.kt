package com.ampnet.auditornode.service

import com.ampnet.auditornode.model.error.Try
import com.ampnet.auditornode.script.api.ExecutionContext
import com.ampnet.auditornode.script.api.model.AuditResult

interface AuditingService {
    fun evaluate(auditingScript: String, executionContext: ExecutionContext): Try<AuditResult>
}
