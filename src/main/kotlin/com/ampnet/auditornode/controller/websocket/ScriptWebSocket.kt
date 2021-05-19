package com.ampnet.auditornode.controller.websocket

import com.ampnet.auditornode.model.websocket.AuditResultResponse
import com.ampnet.auditornode.model.websocket.ConnectedInfoMessage
import com.ampnet.auditornode.model.websocket.ErrorResponse
import com.ampnet.auditornode.model.websocket.ExecutingInfoMessage
import com.ampnet.auditornode.model.websocket.NotFoundInfoMessage
import com.ampnet.auditornode.model.websocket.WebSocketApi
import com.ampnet.auditornode.persistence.model.ScriptId
import com.ampnet.auditornode.persistence.repository.ScriptRepository
import com.ampnet.auditornode.script.api.ExecutionContext
import com.ampnet.auditornode.script.api.classes.WebSocketInput
import com.ampnet.auditornode.script.api.classes.WebSocketOutput
import com.ampnet.auditornode.service.AuditingService
import io.micronaut.core.serialize.ObjectSerializer
import io.micronaut.http.annotation.PathVariable
import io.micronaut.scheduling.TaskExecutors
import io.micronaut.websocket.CloseReason
import io.micronaut.websocket.WebSocketSession
import io.micronaut.websocket.annotation.OnClose
import io.micronaut.websocket.annotation.OnMessage
import io.micronaut.websocket.annotation.OnOpen
import io.micronaut.websocket.annotation.ServerWebSocket
import io.reactivex.Scheduler
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import mu.KotlinLogging
import java.util.UUID
import java.util.concurrent.ExecutorService
import javax.inject.Inject
import javax.inject.Named

private val logger = KotlinLogging.logger {}

@ServerWebSocket("/script/interactive/{scriptId}")
class ScriptWebSocket @Inject constructor(
    private val auditingService: AuditingService,
    private val scriptRepository: ScriptRepository,
    private val objectSerializer: ObjectSerializer,
    @Named(TaskExecutors.IO) executorService: ExecutorService
) {

    companion object {
        private const val SCRIPT_INPUT_ATTRIBUTE = "ScriptInput"
        private const val SCRIPT_TASK_ATTRIBUTE = "ScriptTask"
    }

    private val scheduler: Scheduler = Schedulers.from(executorService)

    @OnOpen
    fun onOpen(@PathVariable("scriptId") scriptId: UUID, session: WebSocketSession) {
        logger.info { "WebSocket connection opened" }
        val webSocketApi = WebSocketApi(session, objectSerializer)

        webSocketApi.sendInfoMessage(ConnectedInfoMessage)

        val script = scriptRepository.load(ScriptId(scriptId))

        if (script == null) {
            webSocketApi.sendInfoMessage(NotFoundInfoMessage)
            session.close(CloseReason.NORMAL)
            return
        }

        webSocketApi.sendInfoMessage(ExecutingInfoMessage)

        val input = WebSocketInput(webSocketApi)
        session.attributes.put(SCRIPT_INPUT_ATTRIBUTE, input)

        val output = WebSocketOutput(webSocketApi)

        val executionContext = ExecutionContext(input, output)
        val scriptTask = scheduler.scheduleDirect {
            auditingService.evaluate(script.content, executionContext).fold(
                ifLeft = {
                    webSocketApi.sendResponse(
                        ErrorResponse(
                            message = "Script execution error",
                            payload = it.message ?: ""
                        )
                    )
                },
                ifRight = {
                    webSocketApi.sendResponse(
                        AuditResultResponse(
                            message = "Script execution finished",
                            payload = it
                        )
                    )
                }
            )
            session.close(CloseReason.NORMAL)
        }

        session.attributes.put(SCRIPT_TASK_ATTRIBUTE, scriptTask)
    }

    @OnMessage
    fun onMessage(message: String, session: WebSocketSession) {
        logger.info { "WebSocket message: $message" }
        session.attributes[SCRIPT_INPUT_ATTRIBUTE, WebSocketInput::class.java].ifPresent {
            it.push(message)
        }
    }

    @OnClose
    fun onClose(session: WebSocketSession) {
        logger.info { "WebSocket connection closed" }
        session.attributes[SCRIPT_TASK_ATTRIBUTE, Disposable::class.java].ifPresent {
            it.dispose()
        }
    }
}
