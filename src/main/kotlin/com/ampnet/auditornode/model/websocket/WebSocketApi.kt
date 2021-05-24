package com.ampnet.auditornode.model.websocket

import com.fasterxml.jackson.databind.ObjectMapper
import io.micronaut.websocket.WebSocketSession
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class WebSocketApi(
    private val session: WebSocketSession,
    private val objectMapper: ObjectMapper
) {

    fun sendCommand(command: WebSocketCommand) = send(command)

    fun sendInfoMessage(message: WebSocketInfoMessage) = send(message)

    fun sendResponse(response: WebSocketResponse) = send(response)

    private fun send(message: WebSocketMessage) {
        logger.debug { "Sending web socket message: $message" }
        session.sendSync(objectMapper.writeValueAsString(message))
    }
}
