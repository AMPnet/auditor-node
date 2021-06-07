package com.ampnet.auditornode.model.websocket

sealed interface WebSocketScriptState

object InitState : WebSocketScriptState

object ReadyState : WebSocketScriptState

object ExecutingState : WebSocketScriptState

object FinishedState : WebSocketScriptState
