package com.ampnet.auditornode.model.response

import com.ampnet.auditornode.persistence.model.ScriptId
import com.ampnet.auditornode.script.api.model.AuditResult
import com.ampnet.auditornode.util.NativeReflection
import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID

@Schema
@NativeReflection
data class StoreScriptResponse(
    val id: ScriptId
) {
    // Swagger does not handle Kotlin value classes at all, so this is needed
    // to trick it into generating the documentation properly
    @Suppress("unused")
    @Schema(type = "string", format = "uuid")
    @Deprecated("Use id field instead.", replaceWith = ReplaceWith("id"))
    fun id(): UUID = id.value
}

@Schema(oneOf = [ExecuteScriptOkResponse::class, ExecuteScriptErrorResponse::class])
@NativeReflection
sealed interface ExecuteScriptResponse

@Schema
@NativeReflection
data class ExecuteScriptOkResponse(val payload: AuditResult) : ExecuteScriptResponse

@Schema
@NativeReflection
data class ExecuteScriptErrorResponse(val error: String?) : ExecuteScriptResponse
