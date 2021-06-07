package com.ampnet.auditornode.model.websocket

import com.ampnet.auditornode.TestBase
import com.fasterxml.jackson.databind.ObjectMapper
import io.micronaut.websocket.WebSocketSession
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.then
import org.mockito.kotlin.times

class WebSocketApiUnitTest : TestBase() {

    private val session = mock<WebSocketSession>()
    private val objectMapper = mock<ObjectMapper>()
    private val service = WebSocketApi(session, objectMapper)

    @BeforeEach
    fun beforeEach() {
        reset(session, objectMapper)
    }

    @Test
    fun `must correctly send web socket command`() {
        val command = ReadBooleanCommand("example")
        val serializedCommand = "{\"value\":\"test\"}"

        suppose("command object will be serialized into JSON") {
            given(objectMapper.writeValueAsString(command))
                .willReturn(serializedCommand)
        }

        verify("command is correctly sent") {
            service.sendCommand(command)

            then(session)
                .should(times(1))
                .sendSync(serializedCommand)
        }
    }

    @Test
    fun `must correctly send web socket info message`() {
        val infoMessage = ConnectedInfoMessage
        val serializedInfoMessage = "{\"value\":\"test\"}"

        suppose("info message object will be serialized into JSON") {
            given(objectMapper.writeValueAsString(infoMessage))
                .willReturn(serializedInfoMessage)
        }

        verify("info message is correctly sent") {
            service.sendInfoMessage(infoMessage)

            then(session)
                .should(times(1))
                .sendSync(serializedInfoMessage)
        }
    }

    @Test
    fun `must correctly send web socket response`() {
        val response = ErrorResponse("example")
        val serializedResponse = "{\"value\":\"test\"}"

        suppose("response object will be serialized into JSON") {
            given(objectMapper.writeValueAsString(response))
                .willReturn(serializedResponse)
        }

        verify("response is correctly sent") {
            service.sendResponse(response)

            then(session)
                .should(times(1))
                .sendSync(serializedResponse)
        }
    }
}
