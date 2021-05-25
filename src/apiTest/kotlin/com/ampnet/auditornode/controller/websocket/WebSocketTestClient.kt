package com.ampnet.auditornode.controller.websocket

import assertk.assertThat
import assertk.assertions.isNotNull
import com.ampnet.auditornode.isJsonEqualTo
import com.ampnet.auditornode.model.websocket.WebSocketMessage
import io.micronaut.websocket.WebSocketSession
import io.micronaut.websocket.annotation.ClientWebSocket
import io.micronaut.websocket.annotation.OnMessage
import io.micronaut.websocket.annotation.OnOpen
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

@ClientWebSocket
abstract class WebSocketTestClient : AutoCloseable {

    private var session: WebSocketSession? = null
    private val messageQueue = LinkedBlockingQueue<String>()

    @OnOpen
    fun onOpen(session: WebSocketSession) {
        this.session = session
    }

    @OnMessage
    fun onMessage(message: String) {
        messageQueue.add(message)
    }

    fun assertNextMessage(message: WebSocketMessage, timeoutInSeconds: Long = 5L) {
        val queueMessage = messageQueue.poll(timeoutInSeconds, TimeUnit.SECONDS)

        assertThat(queueMessage)
            .isNotNull()
            .isJsonEqualTo(message)
    }

    fun send(message: String) {
        session?.sendSync(message)
    }

    override fun close() {
        session?.close()
    }
}
