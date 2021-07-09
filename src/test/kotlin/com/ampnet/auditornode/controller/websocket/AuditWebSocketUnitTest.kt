package com.ampnet.auditornode.controller.websocket

import arrow.core.left
import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import com.ampnet.auditornode.TestBase
import com.ampnet.auditornode.configuration.properties.AuditorProperties
import com.ampnet.auditornode.model.contract.AssetId
import com.ampnet.auditornode.model.contract.ContractAddress
import com.ampnet.auditornode.model.contract.UnsignedTransaction
import com.ampnet.auditornode.model.error.EvaluationError.ScriptExecutionError
import com.ampnet.auditornode.model.error.IpfsError.IpfsEmptyResponseError
import com.ampnet.auditornode.model.error.ParseError.JsonParseError
import com.ampnet.auditornode.model.error.RpcError.ContractReadError
import com.ampnet.auditornode.model.websocket.AuditResultResponse
import com.ampnet.auditornode.model.websocket.ConnectedInfoMessage
import com.ampnet.auditornode.model.websocket.ErrorResponse
import com.ampnet.auditornode.model.websocket.ExecutingInfoMessage
import com.ampnet.auditornode.model.websocket.ExecutingState
import com.ampnet.auditornode.model.websocket.FinishedState
import com.ampnet.auditornode.model.websocket.InitState
import com.ampnet.auditornode.model.websocket.InvalidInputJsonErrorMessage
import com.ampnet.auditornode.model.websocket.IpfsReadErrorMessage
import com.ampnet.auditornode.model.websocket.ReadyState
import com.ampnet.auditornode.model.websocket.RpcErrorMessage
import com.ampnet.auditornode.model.websocket.SpecifyIpfsDirectoryHashCommand
import com.ampnet.auditornode.model.websocket.WaitingForIpfsHash
import com.ampnet.auditornode.model.websocket.WebSocketScriptState
import com.ampnet.auditornode.persistence.model.IpfsHash
import com.ampnet.auditornode.persistence.model.IpfsTextFile
import com.ampnet.auditornode.persistence.repository.IpfsRepository
import com.ampnet.auditornode.script.api.classes.WebSocketInput
import com.ampnet.auditornode.script.api.model.AbortedAudit
import com.ampnet.auditornode.script.api.model.SuccessfulAudit
import com.ampnet.auditornode.service.ApxCoordinatorContractService
import com.ampnet.auditornode.service.AssetHolderContractService
import com.ampnet.auditornode.service.AuditingService
import com.ampnet.auditornode.service.ContractProvider
import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import io.micronaut.core.convert.value.MutableConvertibleValues
import io.micronaut.websocket.CloseReason
import io.micronaut.websocket.WebSocketSession
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.runBlocking
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
import java.math.BigInteger
import java.util.Optional
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService

class AuditWebSocketUnitTest : TestBase() {

    private val apxCoordinatorContractService = mock<ApxCoordinatorContractService>()
    private val assetAddress = ContractAddress("0xTestContractAddress")
    private val assetHolderContractService = mock<AssetHolderContractService>()
    private val contractProvider = mock<ContractProvider> {
        on { getAssetHolderContract(assetAddress) } doReturn assetHolderContractService
    }
    private val auditingService = mock<AuditingService>()
    private val ipfsRepository = mock<IpfsRepository>()
    private val objectMapper = mock<ObjectMapper>()
    private val auditorProperties = mock<AuditorProperties>()
    private val executorService = mock<ExecutorService>()
    private val controller = AuditWebSocket(
        apxCoordinatorContractService,
        contractProvider,
        auditingService,
        ipfsRepository,
        objectMapper,
        auditorProperties,
        executorService
    )

    private val assetId = AssetId(BigInteger.valueOf(123L))
    private val procedureHash = IpfsHash("procedureHash")

    @BeforeEach
    fun beforeEach() {
        reset(
            apxCoordinatorContractService,
            assetHolderContractService,
            auditingService,
            ipfsRepository,
            objectMapper,
            auditorProperties,
            executorService
        )
    }

    @Test
    fun `must return error when asset ID cannot be fetched`() {
        val sessionAttributes = mock<MutableConvertibleValues<Any>>()
        val session = mock<WebSocketSession> {
            on { attributes } doReturn sessionAttributes
        }

        val connectedMessage = "connected"
        val responseMessage = "error"
        val exception = ContractReadError("test")

        suppose("web socket messages will be serialized") {
            given(objectMapper.writeValueAsString(ConnectedInfoMessage))
                .willReturn(connectedMessage)
            given(objectMapper.writeValueAsString(RpcErrorMessage(exception.message)))
                .willReturn(responseMessage)
        }

        suppose("asset ID will not be fetched") {
            given(assetHolderContractService.getAssetId())
                .willReturn(exception.left())
        }

        verify("correct web socket messages are sent") {
            runBlocking {
                controller.onOpen(assetAddress.value, session)
            }

            then(session)
                .should(times(1))
                .sendSync(connectedMessage)
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
    fun `must return error when asset info IPFS hash cannot be fetched`() {
        val sessionAttributes = mock<MutableConvertibleValues<Any>>()
        val session = mock<WebSocketSession> {
            on { attributes } doReturn sessionAttributes
        }

        val connectedMessage = "connected"
        val responseMessage = "error"
        val exception = ContractReadError("test")

        suppose("web socket messages will be serialized") {
            given(objectMapper.writeValueAsString(ConnectedInfoMessage))
                .willReturn(connectedMessage)
            given(objectMapper.writeValueAsString(RpcErrorMessage(exception.message)))
                .willReturn(responseMessage)
        }

        suppose("asset ID will be fetched") {
            given(assetHolderContractService.getAssetId())
                .willReturn(assetId.right())
        }

        suppose("asset info IPFS hash will not be fetched") {
            given(assetHolderContractService.getAssetInfoIpfsHash())
                .willReturn(exception.left())
        }

        verify("correct web socket messages are sent") {
            runBlocking {
                controller.onOpen(assetAddress.value, session)
            }

            then(session)
                .should(times(1))
                .sendSync(connectedMessage)
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
    fun `must return error when asset info file cannot be fetched`() {
        val sessionAttributes = mock<MutableConvertibleValues<Any>>()
        val session = mock<WebSocketSession> {
            on { attributes } doReturn sessionAttributes
        }

        val connectedMessage = "connected"
        val responseMessage = "error"
        val exception = IpfsEmptyResponseError(IpfsHash("testHash"), "fileName")

        suppose("web socket messages will be serialized") {
            given(objectMapper.writeValueAsString(ConnectedInfoMessage))
                .willReturn(connectedMessage)
            given(objectMapper.writeValueAsString(IpfsReadErrorMessage(exception.message)))
                .willReturn(responseMessage)
        }

        suppose("asset ID will be fetched") {
            given(assetHolderContractService.getAssetId())
                .willReturn(assetId.right())
        }

        suppose("asset info IPFS hash will be fetched") {
            given(assetHolderContractService.getAssetInfoIpfsHash())
                .willReturn(IpfsHash("testHash").right())
        }

        suppose("asset info file will not be fetched") {
            given(ipfsRepository.fetchTextFile(IpfsHash("testHash")))
                .willReturn(exception.left())
        }

        verify("correct web socket messages are sent") {
            runBlocking {
                controller.onOpen(assetAddress.value, session)
            }

            then(session)
                .should(times(1))
                .sendSync(connectedMessage)
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
    fun `must return error when asset info JSON cannot be parsed`() {
        val sessionAttributes = mock<MutableConvertibleValues<Any>>()
        val session = mock<WebSocketSession> {
            on { attributes } doReturn sessionAttributes
        }

        val connectedMessage = "connected"
        val responseMessage = "error"
        val exception = JsonParseException(null, "test")
        val json = "{invalidJson}"
        val wrappedException = JsonParseError(json, exception)

        suppose("web socket messages will be serialized") {
            given(objectMapper.writeValueAsString(ConnectedInfoMessage))
                .willReturn(connectedMessage)
            given(objectMapper.writeValueAsString(InvalidInputJsonErrorMessage(wrappedException.message)))
                .willReturn(responseMessage)
        }

        suppose("asset ID will be fetched") {
            given(assetHolderContractService.getAssetId())
                .willReturn(assetId.right())
        }

        suppose("asset info IPFS hash will be fetched") {
            given(assetHolderContractService.getAssetInfoIpfsHash())
                .willReturn(IpfsHash("testHash").right())
        }

        suppose("asset info file will be fetched") {
            given(ipfsRepository.fetchTextFile(IpfsHash("testHash")))
                .willReturn(IpfsTextFile(json).right())
        }

        suppose("asset info JSON is invalid") {
            given(objectMapper.readTree(json))
                .willThrow(exception)
        }

        verify("correct web socket messages are sent") {
            runBlocking {
                controller.onOpen(assetAddress.value, session)
            }

            then(session)
                .should(times(1))
                .sendSync(connectedMessage)
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
    fun `must return error when auditing procedure script file cannot be fetched`() {
        val sessionAttributes = mock<MutableConvertibleValues<Any>>()
        val session = mock<WebSocketSession> {
            on { attributes } doReturn sessionAttributes
        }

        val connectedMessage = "connected"
        val responseMessage = "error"
        val exception = IpfsEmptyResponseError(IpfsHash("procedureHash"), "audit.js")

        suppose("web socket messages will be serialized") {
            given(objectMapper.writeValueAsString(ConnectedInfoMessage))
                .willReturn(connectedMessage)
            given(objectMapper.writeValueAsString(IpfsReadErrorMessage(exception.message)))
                .willReturn(responseMessage)
        }

        suppose("asset ID will be fetched") {
            given(assetHolderContractService.getAssetId())
                .willReturn(assetId.right())
        }

        suppose("asset info IPFS hash will be fetched") {
            given(assetHolderContractService.getAssetInfoIpfsHash())
                .willReturn(IpfsHash("testHash").right())
        }

        suppose("asset info file will be fetched") {
            given(ipfsRepository.fetchTextFile(IpfsHash("testHash")))
                .willReturn(IpfsTextFile("{}").right())
        }

        suppose("asset info JSON is valid") {
            given(objectMapper.readTree("{}"))
                .willReturn(ObjectNode(null))
        }

        suppose("auditing procedure will not be fetched") {
            given(auditorProperties.auditingProcedureDirectoryIpfsHash)
                .willReturn(procedureHash.value)
            given(ipfsRepository.fetchTextFileFromDirectory(procedureHash, "audit.js"))
                .willReturn(exception.left())
        }

        verify("correct web socket messages are sent") {
            runBlocking {
                controller.onOpen(assetAddress.value, session)
            }

            then(session)
                .should(times(1))
                .sendSync(connectedMessage)
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
    fun `must successfully start auditing script`() {
        val sessionAttributes = mock<MutableConvertibleValues<Any>>()
        val session = mock<WebSocketSession> {
            on { attributes } doReturn sessionAttributes
        }

        val connectedMessage = "connected"
        val executingMessage = "executing"

        suppose("web socket messages will be serialized") {
            given(objectMapper.writeValueAsString(ConnectedInfoMessage))
                .willReturn(connectedMessage)
            given(objectMapper.writeValueAsString(ExecutingInfoMessage))
                .willReturn(executingMessage)
        }

        suppose("asset ID will be fetched") {
            given(assetHolderContractService.getAssetId())
                .willReturn(assetId.right())
        }

        suppose("asset info IPFS hash will be fetched") {
            given(assetHolderContractService.getAssetInfoIpfsHash())
                .willReturn(IpfsHash("testHash").right())
        }

        suppose("asset info file will be fetched") {
            given(ipfsRepository.fetchTextFile(IpfsHash("testHash")))
                .willReturn(IpfsTextFile("{}").right())
        }

        suppose("asset info JSON is valid") {
            given(objectMapper.readTree("{}"))
                .willReturn(ObjectNode(null))
        }

        suppose("auditing procedure will be fetched") {
            given(auditorProperties.auditingProcedureDirectoryIpfsHash)
                .willReturn(procedureHash.value)
            given(ipfsRepository.fetchTextFileFromDirectory(procedureHash, "audit.js"))
                .willReturn(IpfsTextFile("script").right())
        }

        verify("correct web socket messages are sent") {
            runBlocking {
                controller.onOpen(assetAddress.value, session)
            }

            then(session)
                .should(times(1))
                .sendSync(connectedMessage)
            then(session)
                .should(times(1))
                .sendSync(executingMessage)
        }

        verify("correct session variables are set") {
            then(sessionAttributes)
                .should(times(1))
                .put(eq("ScriptInput"), any())
            then(sessionAttributes)
                .should(times(1))
                .put("ScriptState", ReadyState)
            then(sessionAttributes)
                .should(times(1))
                .put("ScriptState", ExecutingState)
            then(sessionAttributes)
                .should(times(1))
                .put(eq("ScriptTask"), any())
        }

        verify("script task was scheduled") {
            then(executorService)
                .should(times(1))
                .submit(any<Callable<Any>>())
        }
    }

    @Test
    fun `must send correct response for aborted auditing script`() {
        val sessionAttributes = mock<MutableConvertibleValues<Any>>()
        val session = mock<WebSocketSession> {
            on { attributes } doReturn sessionAttributes
        }

        val connectedMessage = "connected"
        val executingMessage = "executing"
        val responseMessage = "response"
        val auditResult = AbortedAudit("test")

        suppose("web socket messages will be serialized") {
            given(objectMapper.writeValueAsString(ConnectedInfoMessage))
                .willReturn(connectedMessage)
            given(objectMapper.writeValueAsString(ExecutingInfoMessage))
                .willReturn(executingMessage)
            given(objectMapper.writeValueAsString(AuditResultResponse(auditResult, null)))
                .willReturn(responseMessage)
        }

        suppose("asset ID will be fetched") {
            given(assetHolderContractService.getAssetId())
                .willReturn(assetId.right())
        }

        suppose("asset info IPFS hash will be fetched") {
            given(assetHolderContractService.getAssetInfoIpfsHash())
                .willReturn(IpfsHash("testHash").right())
        }

        suppose("asset info file will be fetched") {
            given(ipfsRepository.fetchTextFile(IpfsHash("testHash")))
                .willReturn(IpfsTextFile("{}").right())
        }

        suppose("asset info JSON is valid") {
            given(objectMapper.readTree("{}"))
                .willReturn(ObjectNode(null))
        }

        suppose("auditing procedure will be fetched") {
            given(auditorProperties.auditingProcedureDirectoryIpfsHash)
                .willReturn(procedureHash.value)
            given(ipfsRepository.fetchTextFileFromDirectory(IpfsHash("procedureHash"), "audit.js"))
                .willReturn(IpfsTextFile("script").right())
        }

        suppose("script task will be executed immediately") {
            given(auditingService.evaluate(any(), any()))
                .willReturn(auditResult.right())
            given(executorService.submit(any<Callable<Any>>()))
                .willAnswer { it.getArgument(0, Callable::class.java).call() } // call scheduled task directly
        }

        verify("correct web socket messages are sent") {
            runBlocking {
                controller.onOpen(assetAddress.value, session)
            }

            then(session)
                .should(times(1))
                .sendSync(connectedMessage)
            then(session)
                .should(times(1))
                .sendSync(executingMessage)
            then(session)
                .should(times(1))
                .sendSync(responseMessage)
        }

        verify("correct session variables are set") {
            then(sessionAttributes)
                .should(times(1))
                .put(eq("ScriptInput"), any())
            then(sessionAttributes)
                .should(times(1))
                .put("ScriptState", ReadyState)
            then(sessionAttributes)
                .should(times(1))
                .put("ScriptState", ExecutingState)
            then(sessionAttributes)
                .should(times(1))
                .put("ScriptState", FinishedState)
            then(sessionAttributes)
                .should(times(1))
                .put(eq("ScriptTask"), any())
        }

        verify("web socket session was closed") {
            then(session)
                .should(times(1))
                .close(CloseReason.NORMAL)
        }
    }

    @Test
    fun `must send correct response for failed auditing script`() {
        val sessionAttributes = mock<MutableConvertibleValues<Any>>()
        val session = mock<WebSocketSession> {
            on { attributes } doReturn sessionAttributes
        }

        val connectedMessage = "connected"
        val executingMessage = "executing"
        val responseMessage = "response"
        val exception = ScriptExecutionError(RuntimeException("error message"))

        suppose("web socket messages will be serialized") {
            given(objectMapper.writeValueAsString(ConnectedInfoMessage))
                .willReturn(connectedMessage)
            given(objectMapper.writeValueAsString(ExecutingInfoMessage))
                .willReturn(executingMessage)
            given(objectMapper.writeValueAsString(ErrorResponse(exception.message ?: "")))
                .willReturn(responseMessage)
        }

        suppose("asset ID will be fetched") {
            given(assetHolderContractService.getAssetId())
                .willReturn(assetId.right())
        }

        suppose("asset info IPFS hash will be fetched") {
            given(assetHolderContractService.getAssetInfoIpfsHash())
                .willReturn(IpfsHash("testHash").right())
        }

        suppose("asset info file will be fetched") {
            given(ipfsRepository.fetchTextFile(IpfsHash("testHash")))
                .willReturn(IpfsTextFile("{}").right())
        }

        suppose("asset info JSON is valid") {
            given(objectMapper.readTree("{}"))
                .willReturn(ObjectNode(null))
        }

        suppose("auditing procedure will be fetched") {
            given(auditorProperties.auditingProcedureDirectoryIpfsHash)
                .willReturn(procedureHash.value)
            given(ipfsRepository.fetchTextFileFromDirectory(IpfsHash("procedureHash"), "audit.js"))
                .willReturn(IpfsTextFile("script").right())
        }

        suppose("script task will be executed immediately") {
            given(auditingService.evaluate(any(), any()))
                .willReturn(exception.left())
            given(executorService.submit(any<Callable<Any>>()))
                .willAnswer { it.getArgument(0, Callable::class.java).call() } // call scheduled task directly
        }

        verify("correct web socket messages are sent") {
            runBlocking {
                controller.onOpen(assetAddress.value, session)
            }

            then(session)
                .should(times(1))
                .sendSync(connectedMessage)
            then(session)
                .should(times(1))
                .sendSync(executingMessage)
            then(session)
                .should(times(1))
                .sendSync(responseMessage)
        }

        verify("correct session variables are set") {
            then(sessionAttributes)
                .should(times(1))
                .put(eq("ScriptInput"), any())
            then(sessionAttributes)
                .should(times(1))
                .put("ScriptState", ReadyState)
            then(sessionAttributes)
                .should(times(1))
                .put("ScriptState", ExecutingState)
            then(sessionAttributes)
                .should(times(1))
                .put("ScriptState", FinishedState)
            then(sessionAttributes)
                .should(times(1))
                .put(eq("ScriptTask"), any())
        }

        verify("web socket session was closed") {
            then(session)
                .should(times(1))
                .close(CloseReason.NORMAL)
        }
    }

    @Test
    fun `must send correct response for successful auditing script`() {
        val sessionAttributes = mock<MutableConvertibleValues<Any>>()
        val session = mock<WebSocketSession> {
            on { attributes } doReturn sessionAttributes
        }

        val connectedMessage = "connected"
        val executingMessage = "executing"
        val specifyIpfsDirectoryHashMessage = "specify"
        val responseMessage = "response"
        val transaction = UnsignedTransaction(
            to = "to",
            data = "data"
        )
        val auditResult = SuccessfulAudit

        suppose("web socket messages will be serialized") {
            given(objectMapper.writeValueAsString(ConnectedInfoMessage))
                .willReturn(connectedMessage)
            given(objectMapper.writeValueAsString(ExecutingInfoMessage))
                .willReturn(executingMessage)
            given(objectMapper.writeValueAsString(SpecifyIpfsDirectoryHashCommand(auditResult)))
                .willReturn(specifyIpfsDirectoryHashMessage)
            given(objectMapper.writeValueAsString(AuditResultResponse(auditResult, transaction)))
                .willReturn(responseMessage)
        }

        suppose("asset ID will be fetched") {
            given(assetHolderContractService.getAssetId())
                .willReturn(assetId.right())
        }

        suppose("asset info IPFS hash will be fetched") {
            given(assetHolderContractService.getAssetInfoIpfsHash())
                .willReturn(IpfsHash("testHash").right())
        }

        suppose("asset info file will be fetched") {
            given(ipfsRepository.fetchTextFile(IpfsHash("testHash")))
                .willReturn(IpfsTextFile("{}").right())
        }

        suppose("asset info JSON is valid") {
            given(objectMapper.readTree("{}"))
                .willReturn(ObjectNode(null))
        }

        suppose("auditing procedure will be fetched") {
            given(auditorProperties.auditingProcedureDirectoryIpfsHash)
                .willReturn(procedureHash.value)
            given(ipfsRepository.fetchTextFileFromDirectory(IpfsHash("procedureHash"), "audit.js"))
                .willReturn(IpfsTextFile("script").right())
        }

        val ipfsHash = "ipfsHash"

        suppose("script task will be executed immediately") {
            given(auditingService.evaluate(any(), any()))
                .willReturn(auditResult.right())
            given(
                apxCoordinatorContractService.generateTxForPerformAudit(
                    assetId,
                    auditResult,
                    IpfsHash(ipfsHash)
                )
            )
                .willReturn(transaction)
            given(sessionAttributes.get("ScriptState", WebSocketScriptState::class.java))
                .willReturn(Optional.of(WaitingForIpfsHash(auditResult, assetId)))
            given(executorService.submit(any<Callable<Any>>()))
                .willAnswer { it.getArgument(0, Callable::class.java).call() } // call scheduled task directly
        }

        verify("correct web socket messages are sent") {
            runBlocking {
                controller.onOpen(assetAddress.value, session)
            }

            then(session)
                .should(times(1))
                .sendSync(connectedMessage)
            then(session)
                .should(times(1))
                .sendSync(executingMessage)
            then(session)
                .should(times(1))
                .sendSync(specifyIpfsDirectoryHashMessage)

            controller.onMessage(ipfsHash, session)

            then(session)
                .should(times(1))
                .sendSync(responseMessage)
        }

        verify("correct session variables are set") {
            then(sessionAttributes)
                .should(times(1))
                .put(eq("ScriptInput"), any())
            then(sessionAttributes)
                .should(times(1))
                .put("ScriptState", ReadyState)
            then(sessionAttributes)
                .should(times(1))
                .put("ScriptState", ExecutingState)
            then(sessionAttributes)
                .should(times(1))
                .put("ScriptState", WaitingForIpfsHash(auditResult, assetId))
            then(sessionAttributes)
                .should(times(1))
                .put("ScriptState", FinishedState)
            then(sessionAttributes)
                .should(times(1))
                .put(eq("ScriptTask"), any())
        }

        verify("web socket session was closed") {
            then(session)
                .should(times(1))
                .close(CloseReason.NORMAL)
        }
    }

    @Test
    fun `must not throw exception when receiving a message in InitState, ReadyState or FinishedState`() {
        val sessionAttributes = mock<MutableConvertibleValues<Any>>()
        val session = mock<WebSocketSession> {
            on { attributes } doReturn sessionAttributes
        }

        suppose("web socket session is in InitState") {
            given(sessionAttributes["ScriptState", WebSocketScriptState::class.java])
                .willReturn(Optional.of(InitState))
        }

        verify("exception is not thrown for InitState") {
            assertThat(controller.onMessage("test", session))
                .isEqualTo(Unit)
        }

        suppose("web socket session is in ReadyState") {
            given(sessionAttributes["ScriptState", WebSocketScriptState::class.java])
                .willReturn(Optional.of(ReadyState))
        }

        verify("exception is not thrown for ReadyState") {
            assertThat(controller.onMessage("test", session))
                .isEqualTo(Unit)
        }

        suppose("web socket session is in FinishedState") {
            given(sessionAttributes["ScriptState", WebSocketScriptState::class.java])
                .willReturn(Optional.of(FinishedState))
        }

        verify("exception is not thrown for FinishedState") {
            assertThat(controller.onMessage("test", session))
                .isEqualTo(Unit)
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
            assertThat(controller.onMessage("test", session))
                .isEqualTo(Unit)
            then(mockInput)
                .should(times(1))
                .push("test")
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
