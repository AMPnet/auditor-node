package com.ampnet.auditornode.model.websocket

import io.micronaut.core.serialize.ObjectSerializer
import io.micronaut.websocket.WebSocketSession
import java.nio.charset.StandardCharsets

class WebSocketApi(
    private val session: WebSocketSession,
    private val objectSerializer: ObjectSerializer
) {

    fun sendCommand(command: WebSocketCommand) = send(command)

    fun sendInfoMessage(message: WebSocketInfoMessage) = send(message)

    fun <T> sendResponse(response: WebSocketResponse<T>) = send(response)

    private fun send(message: WebSocketMessage) {
        objectSerializer.serialize(message)
            .map { String(it, StandardCharsets.UTF_8) }
            .ifPresent {
                session.sendSync(it)
            }
    }
}
