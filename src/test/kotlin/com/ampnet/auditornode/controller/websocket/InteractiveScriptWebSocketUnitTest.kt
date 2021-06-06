package com.ampnet.auditornode.controller.websocket

import arrow.core.left
import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import com.ampnet.auditornode.TestBase
import com.ampnet.auditornode.model.error.EvaluationError.ScriptExecutionError
import com.ampnet.auditornode.model.websocket.AuditResultResponse
import com.ampnet.auditornode.model.websocket.ConnectedInfoMessage
import com.ampnet.auditornode.model.websocket.ErrorResponse
import com.ampnet.auditornode.model.websocket.ExecutingInfoMessage
import com.ampnet.auditornode.model.websocket.ExecutingState
import com.ampnet.auditornode.model.websocket.FinishedState
import com.ampnet.auditornode.model.websocket.InitState
import com.ampnet.auditornode.model.websocket.InvalidInputJsonInfoMessage
import com.ampnet.auditornode.model.websocket.NotFoundInfoMessage
import com.ampnet.auditornode.model.websocket.ReadInputJsonCommand
import com.ampnet.auditornode.model.websocket.ReadyState
import com.ampnet.auditornode.model.websocket.WebSocketScriptState
import com.ampnet.auditornode.persistence.model.IpfsHash
import com.ampnet.auditornode.persistence.model.ScriptId
import com.ampnet.auditornode.persistence.model.ScriptSource
import com.ampnet.auditornode.persistence.repository.IpfsRepository
import com.ampnet.auditornode.persistence.repository.ScriptRepository
import com.ampnet.auditornode.script.api.classes.WebSocketInput
import com.ampnet.auditornode.script.api.model.SuccessfulAudit
import com.ampnet.auditornode.service.AuditingService
import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import io.micronaut.core.convert.value.MutableConvertibleValues
import io.micronaut.websocket.CloseReason
import io.micronaut.websocket.WebSocketSession
import io.reactivex.disposables.Disposable
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.then
import org.mockito.kotlin.times
import java.util.Optional
import java.util.UUID
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService

class InteractiveScriptWebSocketUnitTest : TestBase() {

    private val auditingService = mock<AuditingService>()
    private val scriptRepository = mock<ScriptRepository>()
    private val ipfsRepository = mock<IpfsRepository>()
    private val objectMapper = mock<ObjectMapper>()
    private val executorService = mock<ExecutorService>()
    private val controller = InteractiveScriptWebSocket(
        auditingService,
        scriptRepository,
        ipfsRepository,
        objectMapper,
        executorService
    )

    @BeforeEach
    fun beforeEach() {
        reset(auditingService, scriptRepository, ipfsRepository, objectMapper, executorService)
    }

    @Test
    fun `must correctly open web socket connection`() {
        val sessionAttributes = mock<MutableConvertibleValues<Any>>()
        val session = mock<WebSocketSession> {
            on { attributes } doReturn sessionAttributes
        }

        val connectedMessage = "connected"
        val readInputJsonMessage = "readInputJson"

        suppose("web socket messages will be serialized") {
            given(objectMapper.writeValueAsString(ConnectedInfoMessage))
                .willReturn(connectedMessage)
            given(objectMapper.writeValueAsString(ReadInputJsonCommand()))
                .willReturn(readInputJsonMessage)
        }

        val scriptId = ScriptId(UUID.randomUUID())
        val scriptSource = ScriptSource("test script")
        val ipfsDirectoryHash = IpfsHash("testHash")

        suppose("script will be returned from the repository") {
            given(scriptRepository.load(scriptId))
                .willAnswer { scriptSource.content } // for value classes we must return value with willAnswer
        }

        verify("correct web socket messages are sent") {
            controller.onOpen(scriptId.value, ipfsDirectoryHash.value, session)

            then(session)
                .should(times(1))
                .sendSync(connectedMessage)
            then(session)
                .should(times(1))
                .sendSync(readInputJsonMessage)
        }

        verify("correct web socket session variables are set") {
            then(sessionAttributes)
                .should(times(1))
                .put("Script", scriptSource)
            then(sessionAttributes)
                .should(times(1))
                .put("ScriptIpfsDirectoryHash", ipfsDirectoryHash)
            then(sessionAttributes)
                .should(times(1))
                .put("ScriptState", ReadyState)
        }
    }

    @Test
    fun `must send error message and close web socket connection when script cannot be found`() {
        val connectedMessage = "connected"
        val notFoundMessage = "notFound"
        val sessionAttributes = mock<MutableConvertibleValues<Any>>()
        val session = mock<WebSocketSession> {
            on { attributes } doReturn sessionAttributes
        }

        suppose("web socket messages will be serialized") {
            given(objectMapper.writeValueAsString(ConnectedInfoMessage))
                .willReturn(connectedMessage)
            given(objectMapper.writeValueAsString(NotFoundInfoMessage))
                .willReturn(notFoundMessage)
        }

        val scriptId = ScriptId(UUID.randomUUID())

        suppose("script will not be returned from the repository") {
            given(scriptRepository.load(scriptId))
                .willReturn(null)
        }

        verify("correct web socket messages are sent") {
            controller.onOpen(scriptId.value, null, session)

            then(session)
                .should(times(1))
                .sendSync(connectedMessage)
            then(session)
                .should(times(1))
                .sendSync(notFoundMessage)
        }

        verify("web socket session was closed and no session variables are set") {
            then(session)
                .should(times(1))
                .close(CloseReason.ABNORMAL_CLOSURE)
            then(sessionAttributes)
                .shouldHaveNoInteractions()
        }
    }

    @Test
    fun `must not throw exception when message is received in InitState or FinishedState`() {
        val sessionAttributes = mock<MutableConvertibleValues<Any>>()
        val session = mock<WebSocketSession> {
            on { attributes } doReturn sessionAttributes
        }

        suppose("web socket session is in InitState") {
            given(sessionAttributes["ScriptState", WebSocketScriptState::class.java])
                .willReturn(Optional.of(InitState))
        }

        val message = "test"

        verify("exception is not thrown for InitState") {
            assertThat(controller.onMessage(message, session))
                .isEqualTo(Unit)
        }

        suppose("web socket session is in FinishedState") {
            given(sessionAttributes["ScriptState", WebSocketScriptState::class.java])
                .willReturn(Optional.of(FinishedState))
        }

        verify("exception is not thrown for FinishedState") {
            assertThat(controller.onMessage(message, session))
                .isEqualTo(Unit)
        }
    }

    @Test
    fun `must correctly start the script when message is received in ReadyState with some IPFS hash`() {
        val sessionAttributes = mock<MutableConvertibleValues<Any>>()
        val session = mock<WebSocketSession> {
            on { attributes } doReturn sessionAttributes
        }

        suppose("web socket session is in ReadyState") {
            given(sessionAttributes["ScriptState", WebSocketScriptState::class.java])
                .willReturn(Optional.of(ReadyState))
        }

        val ipfsHash = IpfsHash("testHash")

        suppose("IPFS directory hash session variable is set") {
            given(sessionAttributes["ScriptIpfsDirectoryHash", IpfsHash::class.java])
                .willReturn(Optional.of(ipfsHash))
        }

        val message = "{}"

        suppose("input JSON is deserialized") {
            given(objectMapper.readTree(message))
                .willReturn(ObjectNode(null))
        }

        val executingMessage = "executing"

        suppose("web socket messages will be serialized") {
            given(objectMapper.writeValueAsString(ExecutingInfoMessage))
                .willReturn(executingMessage)
        }

        verify("correct session variables are set") {
            controller.onMessage(message, session)

            then(sessionAttributes)
                .should(times(1))
                .put("ScriptState", ExecutingState)
            then(sessionAttributes)
                .should(times(1))
                .put(eq("ScriptInput"), any())
            then(sessionAttributes)
                .should(times(1))
                .put(eq("ScriptTask"), any())
        }

        verify("correct web socket messages are sent") {
            then(session)
                .should(times(1))
                .sendSync(executingMessage)
        }

        verify("script task was scheduled") {
            then(executorService)
                .should(times(1))
                .submit(any<Callable<Any>>())
        }
    }

    @Test
    fun `must correctly start the script when message is received in ReadyState with no IPFS hash`() {
        val sessionAttributes = mock<MutableConvertibleValues<Any>>()
        val session = mock<WebSocketSession> {
            on { attributes } doReturn sessionAttributes
        }

        suppose("web socket session is in ReadyState") {
            given(sessionAttributes["ScriptState", WebSocketScriptState::class.java])
                .willReturn(Optional.of(ReadyState))
        }

        suppose("IPFS directory hash session variable is not set") {
            given(sessionAttributes["ScriptIpfsDirectoryHash", IpfsHash::class.java])
                .willReturn(Optional.empty())
        }

        val message = "{}"

        suppose("input JSON is deserialized") {
            given(objectMapper.readTree(message))
                .willReturn(ObjectNode(null))
        }

        val executingMessage = "executing"

        suppose("web socket messages will be serialized") {
            given(objectMapper.writeValueAsString(ExecutingInfoMessage))
                .willReturn(executingMessage)
        }

        verify("correct session variables are set") {
            controller.onMessage(message, session)

            then(sessionAttributes)
                .should(times(1))
                .put("ScriptState", ExecutingState)
            then(sessionAttributes)
                .should(times(1))
                .put(eq("ScriptInput"), any())
            then(sessionAttributes)
                .should(times(1))
                .put(eq("ScriptTask"), any())
        }

        verify("correct web socket messages are sent") {
            then(session)
                .should(times(1))
                .sendSync(executingMessage)
        }

        verify("script task was scheduled") {
            then(executorService)
                .should(times(1))
                .submit(any<Callable<Any>>())
        }
    }

    @Test
    fun `must send error message and close web socket connection when input JSON cannot be parsed`() {
        val sessionAttributes = mock<MutableConvertibleValues<Any>>()
        val session = mock<WebSocketSession> {
            on { attributes } doReturn sessionAttributes
        }

        suppose("web socket session is in ReadyState") {
            given(sessionAttributes["ScriptState", WebSocketScriptState::class.java])
                .willReturn(Optional.of(ReadyState))
        }

        suppose("IPFS directory hash session variable is not set") {
            given(sessionAttributes["ScriptIpfsDirectoryHash", IpfsHash::class.java])
                .willReturn(Optional.empty())
        }

        val message = "{invalidJson}"

        suppose("input JSON is invalid") {
            given(objectMapper.readTree(message))
                .willThrow(JsonParseException::class.java)
        }

        val invalidJsonMessage = "invalidJson"

        suppose("web socket messages will be serialized") {
            given(objectMapper.writeValueAsString(InvalidInputJsonInfoMessage))
                .willReturn(invalidJsonMessage)
        }

        verify("correct session variables are set") {
            controller.onMessage(message, session)

            then(sessionAttributes)
                .should(times(1))
                .put("ScriptState", ExecutingState)
            then(sessionAttributes)
                .should(times(1))
                .put(eq("ScriptInput"), any())
        }

        verify("correct web socket messages are sent") {
            then(session)
                .should(times(1))
                .sendSync(invalidJsonMessage)
        }

        verify("web socket session was closed") {
            then(session)
                .should(times(1))
                .close(CloseReason.ABNORMAL_CLOSURE)
        }
    }

    @Test
    fun `must correctly send response for successful script`() {
        val sessionAttributes = mock<MutableConvertibleValues<Any>>()
        val session = mock<WebSocketSession> {
            on { attributes } doReturn sessionAttributes
        }

        suppose("web socket session is in ReadyState") {
            given(sessionAttributes["ScriptState", WebSocketScriptState::class.java])
                .willReturn(Optional.of(ReadyState))
        }

        suppose("IPFS directory hash session variable is not set") {
            given(sessionAttributes["ScriptIpfsDirectoryHash", IpfsHash::class.java])
                .willReturn(Optional.empty())
        }

        suppose("script session variable is set") {
            given(sessionAttributes["Script", ScriptSource::class.java])
                .willReturn(Optional.of(ScriptSource("example")))
        }

        val message = "{}"

        suppose("input JSON is deserialized") {
            given(objectMapper.readTree(message))
                .willReturn(ObjectNode(null))
        }

        val executingMessage = "executing"
        val responseMessage = "response"

        suppose("web socket messages will be serialized") {
            given(objectMapper.writeValueAsString(ExecutingInfoMessage))
                .willReturn(executingMessage)
            given(objectMapper.writeValueAsString(AuditResultResponse(SuccessfulAudit, null)))
                .willReturn(responseMessage)
        }

        suppose("script task will be executed immediately") {
            given(auditingService.evaluate(any(), any()))
                .willReturn(SuccessfulAudit.right())
            given(executorService.submit(any<Callable<Any>>()))
                .willAnswer { it.getArgument(0, Callable::class.java).call() } // call scheduled task directly
        }

        verify("correct session variables are set") {
            controller.onMessage(message, session)

            then(sessionAttributes)
                .should(times(1))
                .put("ScriptState", ExecutingState)
            then(sessionAttributes)
                .should(times(1))
                .put(eq("ScriptInput"), any())
            then(sessionAttributes)
                .should(times(1))
                .put(eq("ScriptTask"), any())
            then(sessionAttributes)
                .should(times(1))
                .put("ScriptState", FinishedState)
        }

        verify("correct web socket messages are sent") {
            then(session)
                .should(times(1))
                .sendSync(executingMessage)
            then(session)
                .should(times(1))
                .sendSync(responseMessage)
        }

        verify("web socket session was closed") {
            then(session)
                .should(times(1))
                .close(CloseReason.NORMAL)
        }
    }

    @Test
    fun `must correctly send response for failed script`() {
        val sessionAttributes = mock<MutableConvertibleValues<Any>>()
        val session = mock<WebSocketSession> {
            on { attributes } doReturn sessionAttributes
        }

        suppose("web socket session is in ReadyState") {
            given(sessionAttributes["ScriptState", WebSocketScriptState::class.java])
                .willReturn(Optional.of(ReadyState))
        }

        suppose("IPFS directory hash session variable is not set") {
            given(sessionAttributes["ScriptIpfsDirectoryHash", IpfsHash::class.java])
                .willReturn(Optional.empty())
        }

        suppose("script session variable is set") {
            given(sessionAttributes["Script", ScriptSource::class.java])
                .willReturn(Optional.of(ScriptSource("example")))
        }

        val message = "{}"

        suppose("input JSON is deserialized") {
            given(objectMapper.readTree(message))
                .willReturn(ObjectNode(null))
        }

        val executingMessage = "executing"
        val responseMessage = "response"
        val exception = ScriptExecutionError(RuntimeException("error message"))

        suppose("web socket messages will be serialized") {
            given(objectMapper.writeValueAsString(ExecutingInfoMessage))
                .willReturn(executingMessage)
            given(objectMapper.writeValueAsString(ErrorResponse(exception.message ?: "")))
                .willReturn(responseMessage)
        }

        suppose("script task will be executed immediately") {
            given(auditingService.evaluate(any(), any()))
                .willReturn(exception.left())
            given(executorService.submit(any<Callable<Any>>()))
                .willAnswer { it.getArgument(0, Callable::class.java).call() } // call scheduled task directly
        }

        verify("correct session variables are set") {
            controller.onMessage(message, session)

            then(sessionAttributes)
                .should(times(1))
                .put("ScriptState", ExecutingState)
            then(sessionAttributes)
                .should(times(1))
                .put(eq("ScriptInput"), any())
            then(sessionAttributes)
                .should(times(1))
                .put(eq("ScriptTask"), any())
            then(sessionAttributes)
                .should(times(1))
                .put("ScriptState", FinishedState)
        }

        verify("correct web socket messages are sent") {
            then(session)
                .should(times(1))
                .sendSync(executingMessage)
            then(session)
                .should(times(1))
                .sendSync(responseMessage)
        }

        verify("web socket session was closed") {
            then(session)
                .should(times(1))
                .close(CloseReason.NORMAL)
        }
    }

    @Test
    fun `must correctly push received message when in ExecutingState`() {
        val sessionAttributes = mock<MutableConvertibleValues<Any>>()
        val session = mock<WebSocketSession> {
            on { attributes } doReturn sessionAttributes
        }

        suppose("web socket session is in ExecutingState") {
            given(sessionAttributes["ScriptState", WebSocketScriptState::class.java])
                .willReturn(Optional.of(ExecutingState))
        }

        val mockInput = mock<WebSocketInput>()

        suppose("some script input is set") {
            given(sessionAttributes["ScriptInput", WebSocketInput::class.java])
                .willReturn(Optional.of(mockInput))
        }

        verify("message was successfully pushed") {
            val message = "test"

            assertThat(controller.onMessage(message, session))
                .isEqualTo(Unit)
            then(mockInput)
                .should(times(1))
                .push(message)
        }
    }

    @Test
    fun `must dispose script task when closing web socket session`() {
        val sessionAttributes = mock<MutableConvertibleValues<Any>>()
        val session = mock<WebSocketSession> {
            on { attributes } doReturn sessionAttributes
        }
        val mockTask = mock<Disposable>()

        suppose("some script task is set") {
            given(sessionAttributes["ScriptTask", Disposable::class.java])
                .willReturn(Optional.of(mockTask))
        }

        verify("task was disposed") {
            assertThat(controller.onClose(session))
                .isEqualTo(Unit)
            then(mockTask)
                .should(times(1))
                .dispose()
        }
    }
}
