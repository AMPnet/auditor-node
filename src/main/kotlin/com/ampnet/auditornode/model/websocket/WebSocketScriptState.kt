package com.ampnet.auditornode.model.websocket

import com.ampnet.auditornode.script.api.model.AuditResult

sealed interface WebSocketScriptState

object InitState : WebSocketScriptState

object ReadyState : WebSocketScriptState

object ExecutingState : WebSocketScriptState

data class WaitingForIpfsHash(val auditResult: AuditResult) : WebSocketScriptState

object FinishedState : WebSocketScriptState
