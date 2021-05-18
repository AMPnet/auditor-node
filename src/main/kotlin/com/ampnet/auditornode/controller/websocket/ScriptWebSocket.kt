package com.ampnet.auditornode.controller.websocket

import com.ampnet.auditornode.persistence.model.ScriptId
import com.ampnet.auditornode.persistence.repository.ScriptRepository
import com.ampnet.auditornode.script.api.ExecutionContext
import com.ampnet.auditornode.script.api.classes.WebSocketInput
import com.ampnet.auditornode.script.api.classes.WebSocketOutput
import com.ampnet.auditornode.service.AuditingService
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
    @Named(TaskExecutors.IO) executorService: ExecutorService
) { // TODO refactor

    private val scheduler: Scheduler = Schedulers.from(executorService)

    @OnOpen
    fun onOpen(@PathVariable("scriptId") scriptId: UUID, session: WebSocketSession) {
        logger.info { "Connection opened" }
        session.sendSync("connected")
        val script = scriptRepository.load(ScriptId(scriptId))

        if (script == null) {
            session.sendSync("notFound")
            return
        }

        session.sendSync("executing")

        val input = WebSocketInput(session)
        session.attributes.put("ScriptInput", input)

        val output = WebSocketOutput(session)
        session.attributes.put("ScriptOutput", output)

        val executionContext = ExecutionContext(input, output)
        val scriptTask = scheduler.scheduleDirect {
            auditingService.evaluate(script.content, executionContext).fold(
                ifLeft = { session.sendSync("error:${it.message}") },
                ifRight = { session.sendSync("ok:{\"success\":\"${it.success}\"}") }
            )
            session.close(CloseReason.NORMAL)
        }

        session.attributes.put("ScriptTask", scriptTask)
    }

    @OnMessage
    fun onMessage(message: String, session: WebSocketSession) {
        logger.info { "Got message: $message" }
        session.attributes["ScriptInput", WebSocketInput::class.java].ifPresent {
            logger.info { "Queue.push" }
            it.push(message)
        }
        session.sendSync("ok")
    }

    @OnClose
    fun onClose(session: WebSocketSession) {
        session.attributes["ScriptTask", Disposable::class.java].ifPresent {
            it.dispose()
        }
        logger.info { "Connection closed" }
    }
}
