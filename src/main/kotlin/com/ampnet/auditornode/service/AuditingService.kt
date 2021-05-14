package com.ampnet.auditornode.service

import com.ampnet.auditornode.model.error.Try
import com.ampnet.auditornode.script.api.model.AuditResult

interface AuditingService {
    fun evaluate(auditingScript: String): Try<AuditResult>
}
