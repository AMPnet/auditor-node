package com.ampnet.auditornode.controller.websocket

import com.ampnet.auditornode.controller.websocket.WebSocketSessionHelper.script
import com.ampnet.auditornode.controller.websocket.WebSocketSessionHelper.scriptInput
import com.ampnet.auditornode.controller.websocket.WebSocketSessionHelper.scriptIpfsDirectoryHash
import com.ampnet.auditornode.controller.websocket.WebSocketSessionHelper.scriptState
import com.ampnet.auditornode.controller.websocket.WebSocketSessionHelper.scriptTask
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
import com.ampnet.auditornode.model.websocket.WebSocketApi
import com.ampnet.auditornode.persistence.model.IpfsHash
import com.ampnet.auditornode.persistence.model.ScriptId
import com.ampnet.auditornode.persistence.repository.IpfsRepository
import com.ampnet.auditornode.persistence.repository.ScriptRepository
import com.ampnet.auditornode.script.api.ExecutionContext
import com.ampnet.auditornode.script.api.classes.DirectoryBasedIpfs
import com.ampnet.auditornode.script.api.classes.NoOpIpfs
import com.ampnet.auditornode.script.api.classes.WebSocketInput
import com.ampnet.auditornode.script.api.classes.WebSocketOutput
import com.ampnet.auditornode.service.AuditingService
import com.fasterxml.jackson.core.JacksonException
import com.fasterxml.jackson.databind.ObjectMapper
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.QueryValue
import io.micronaut.scheduling.TaskExecutors
import io.micronaut.websocket.CloseReason
import io.micronaut.websocket.WebSocketSession
import io.micronaut.websocket.annotation.OnClose
import io.micronaut.websocket.annotation.OnMessage
import io.micronaut.websocket.annotation.OnOpen
import io.micronaut.websocket.annotation.ServerWebSocket
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import mu.KotlinLogging
import java.util.UUID
import java.util.concurrent.ExecutorService
import javax.inject.Inject
import javax.inject.Named

private val logger = KotlinLogging.logger {}

@ServerWebSocket("/script/interactive/{scriptId}")
class InteractiveScriptWebSocket @Inject constructor(
    private val auditingService: AuditingService,
    private val scriptRepository: ScriptRepository,
    private val ipfsRepository: IpfsRepository,
    private val objectMapper: ObjectMapper,
    @Named(TaskExecutors.IO) executorService: ExecutorService
) {

    private val scheduler: Scheduler = Schedulers.from(executorService)

    @OnOpen
    fun onOpen(
        @PathVariable("scriptId") scriptId: UUID,
        @QueryValue("ipfs-directory") ipfsDirectoryHash: String?,
        session: WebSocketSession
    ) {
        logger.info { "WebSocket connection opened, script ID: $scriptId, IPFS directory hash: $ipfsDirectoryHash" }
        val webSocketApi = WebSocketApi(session, objectMapper)

        webSocketApi.sendInfoMessage(ConnectedInfoMessage)

        val script = scriptRepository.load(ScriptId(scriptId))

        if (script == null) {
            logger.error { "Script not found for ID: $scriptId" }
            webSocketApi.sendInfoMessage(NotFoundInfoMessage)
            session.close(CloseReason.ABNORMAL_CLOSURE)
            return
        }

        session.script = script
        session.scriptIpfsDirectoryHash = ipfsDirectoryHash?.let { IpfsHash(it) }
        session.scriptState = ReadyState
        webSocketApi.sendCommand(ReadInputJsonCommand())
        logger.info { "Script is in ready state" }
    }

    @OnMessage
    fun onMessage(message: String, session: WebSocketSession) {
        logger.info { "WebSocket message: $message" }

        when (session.scriptState) {
            is InitState, is FinishedState -> Unit
            is ReadyState -> startScript(message, session)
            is ExecutingState -> session.scriptInput?.push(message)
        }
    }

    private fun startScript(message: String, session: WebSocketSession) {
        session.scriptState = ExecutingState
        val webSocketApi = WebSocketApi(session, objectMapper)

        val input = WebSocketInput(webSocketApi)
        session.scriptInput = input

        val output = WebSocketOutput(webSocketApi)
        val ipfs = session.scriptIpfsDirectoryHash?.let {
            DirectoryBasedIpfs(it, ipfsRepository)
        } ?: NoOpIpfs

        val auditDataJson = try {
            objectMapper.readTree(message)
        } catch (e: JacksonException) {
            logger.error(e) { "Error parsing input JSON" }
            webSocketApi.sendInfoMessage(InvalidInputJsonInfoMessage)
            session.close(CloseReason.ABNORMAL_CLOSURE)
            return
        }

        logger.info { "Starting interactive script" }
        webSocketApi.sendInfoMessage(ExecutingInfoMessage)

        val executionContext = ExecutionContext(input, output, ipfs, auditDataJson)
        val scriptTask = scheduler.scheduleDirect {
            auditingService.evaluate(session.script.content, executionContext).fold(
                ifLeft = {
                    logger.warn { "Script execution finished with error: ${it.message}" }
                    webSocketApi.sendResponse(ErrorResponse(it.message ?: ""))
                },
                ifRight = {
                    logger.info { "Script execution finished successfully, result: $it" }
                    webSocketApi.sendResponse(AuditResultResponse(it, null))
                }
            )
            session.scriptState = FinishedState
            session.close(CloseReason.NORMAL)
        }

        session.scriptTask = scriptTask
    }

    @OnClose
    fun onClose(session: WebSocketSession) {
        logger.info { "WebSocket connection closed" }
        session.scriptTask?.dispose()
    }
}
