package com.ampnet.auditornode.controller

import com.ampnet.auditornode.model.response.ExecuteScriptErrorResponse
import com.ampnet.auditornode.model.response.ExecuteScriptOkResponse
import com.ampnet.auditornode.model.response.ExecuteScriptResponse
import com.ampnet.auditornode.model.response.StoreScriptResponse
import com.ampnet.auditornode.persistence.model.ScriptId
import com.ampnet.auditornode.persistence.model.ScriptSource
import com.ampnet.auditornode.persistence.repository.ScriptRepository
import com.ampnet.auditornode.script.api.ExecutionContext
import com.ampnet.auditornode.service.AuditingService
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.Post
import mu.KotlinLogging
import java.util.UUID
import javax.inject.Inject

private val logger = KotlinLogging.logger {}

@Controller("/script")
class ScriptController @Inject constructor(
    private val auditingService: AuditingService,
    private val scriptRepository: ScriptRepository
) {

    @Post(value = "/execute", produces = [MediaType.APPLICATION_JSON], consumes = [MediaType.TEXT_PLAIN])
    fun executeScript(@Body scriptSource: String): ExecuteScriptResponse {
        val result = auditingService.evaluate(scriptSource, ExecutionContext.noOp)
        logger.info { "Evaluation result: $result" }
        return result.fold(
            ifLeft = { ExecuteScriptErrorResponse(it.message) },
            ifRight = { ExecuteScriptOkResponse(it) }
        )
    }

    @Post(value = "/store", produces = [MediaType.APPLICATION_JSON], consumes = [MediaType.TEXT_PLAIN])
    fun storeScript(@Body scriptSource: String): StoreScriptResponse {
        val scriptId = scriptRepository.store(ScriptSource(scriptSource))
        return StoreScriptResponse(scriptId)
    }

    @Get(value = "/load/{scriptId}", produces = [MediaType.TEXT_PLAIN])
    fun loadScript(@PathVariable("scriptId") scriptId: UUID): HttpResponse<String> {
        val scriptSource = scriptRepository.load(ScriptId(scriptId))
        return scriptSource?.let { HttpResponse.ok(it.content) } ?: HttpResponse.notFound()
    }
}
