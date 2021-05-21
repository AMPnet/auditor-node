package com.ampnet.auditornode.model.websocket

import io.micronaut.core.serialize.ObjectSerializer
import io.micronaut.websocket.WebSocketSession
import mu.KotlinLogging
import java.nio.charset.StandardCharsets

private val logger = KotlinLogging.logger {}

class WebSocketApi(
    private val session: WebSocketSession,
    private val objectSerializer: ObjectSerializer
) {

    fun sendCommand(command: WebSocketCommand) = send(command)

    fun sendInfoMessage(message: WebSocketInfoMessage) = send(message)

    fun sendResponse(response: WebSocketResponse) = send(response)

    private fun send(message: WebSocketMessage) {
        logger.debug { "Sending web socket message: $message" }
        objectSerializer.serialize(message)
            .map { String(it, StandardCharsets.UTF_8) }
            .ifPresent {
                session.sendSync(it)
            }
    }
}
