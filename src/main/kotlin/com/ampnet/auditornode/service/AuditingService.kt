package com.ampnet.auditornode.service

import com.ampnet.auditornode.model.error.Try
import com.ampnet.auditornode.model.script.AuditResult

interface AuditingService {
    fun evaluate(auditingScript: String): Try<AuditResult>
}
