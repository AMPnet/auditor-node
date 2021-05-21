package com.ampnet.auditornode.model.response

import com.ampnet.auditornode.persistence.model.ScriptId
import com.ampnet.auditornode.script.api.model.AuditResult

data class StoreScriptResponse(val id: ScriptId)

sealed class ExecuteScriptResponse

data class ExecuteScriptOkResponse(val payload: AuditResult) : ExecuteScriptResponse()

data class ExecuteScriptErrorResponse(val error: String?) : ExecuteScriptResponse()
