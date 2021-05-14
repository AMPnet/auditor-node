package com.ampnet.auditornode.controller

import com.ampnet.auditornode.persistence.model.ScriptId
import com.ampnet.auditornode.persistence.model.ScriptSource
import com.ampnet.auditornode.persistence.repository.ScriptRepository
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

    @Post(value = "/execute", produces = [MediaType.TEXT_PLAIN], consumes = [MediaType.TEXT_PLAIN])
    fun executeScript(@Body scriptSource: String): String { // TODO may want to return JSON here
        val result = auditingService.evaluate(scriptSource)
        logger.info { "Evaluation result: $result" }
        return result.toString()
    }

    @Post(value = "/store", produces = [MediaType.APPLICATION_JSON], consumes = [MediaType.TEXT_PLAIN])
    fun storeScript(@Body scriptSource: String): String {
        val scriptId = scriptRepository.store(ScriptSource(scriptSource))
        return """{"id":"${scriptId.value}"}"""
    }

    @Get(value = "/load/{scriptId}", produces = [MediaType.TEXT_PLAIN])
    fun loadScript(@PathVariable("scriptId") scriptId: UUID): HttpResponse<String> {
        val scriptSource = scriptRepository.load(ScriptId(scriptId))
        return scriptSource?.let { HttpResponse.ok(it.content) } ?: HttpResponse.notFound()
    }
}
