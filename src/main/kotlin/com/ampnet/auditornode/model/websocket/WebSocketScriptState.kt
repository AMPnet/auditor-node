package com.ampnet.auditornode.model.websocket

sealed class WebSocketScriptState

object InitState : WebSocketScriptState()

object ReadyState : WebSocketScriptState()

object ExecutingState : WebSocketScriptState()

object FinishedState : WebSocketScriptState()
