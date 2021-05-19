package com.ampnet.auditornode.controller.websocket

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import com.ampnet.auditornode.model.websocket.WebSocketMessage
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.SingletonSupport
import io.micronaut.jackson.serialize.JacksonObjectSerializer
import io.micronaut.websocket.WebSocketSession
import io.micronaut.websocket.annotation.ClientWebSocket
import io.micronaut.websocket.annotation.OnMessage
import io.micronaut.websocket.annotation.OnOpen
import java.nio.charset.StandardCharsets
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

@ClientWebSocket
abstract class WebSocketTestClient : AutoCloseable {

    private var session: WebSocketSession? = null
    private val messageQueue = LinkedBlockingQueue<String>()

    private val objectSerializer = JacksonObjectSerializer(
        ObjectMapper().apply {
            registerModule(KotlinModule(singletonSupport = SingletonSupport.CANONICALIZE))
        }
    )

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
        val deserializedMessage = queueMessage?.let {
            objectSerializer.deserialize(it.toByteArray(StandardCharsets.UTF_8), message.javaClass)
        }

        assertThat(deserializedMessage?.orElseGet(null))
            .isNotNull()
            .isEqualTo(message)
    }

    fun send(message: String) {
        session?.sendSync(message)
    }

    override fun close() {
        session?.close()
    }
}