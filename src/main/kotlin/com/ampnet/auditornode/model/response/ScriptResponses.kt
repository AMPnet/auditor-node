package com.ampnet.auditornode.model.response

import com.ampnet.auditornode.persistence.model.ScriptId
import com.ampnet.auditornode.script.api.model.AuditResult
import com.ampnet.auditornode.util.NativeReflection

@NativeReflection
data class StoreScriptResponse(val id: ScriptId)

@NativeReflection
sealed interface ExecuteScriptResponse

@NativeReflection
data class ExecuteScriptOkResponse(val payload: AuditResult) : ExecuteScriptResponse

@NativeReflection
data class ExecuteScriptErrorResponse(val error: String?) : ExecuteScriptResponse
