package com.ampnet.auditornode.controller

import com.ampnet.auditornode.service.AuditingService
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
import mu.KotlinLogging
import javax.inject.Inject

private val logger = KotlinLogging.logger {}

@Controller("/script")
class ScriptController @Inject constructor(
    private val auditingService: AuditingService
) {

    @Post(value = "/execute", produces = [MediaType.TEXT_PLAIN], consumes = [MediaType.TEXT_PLAIN])
    fun executeScript(@Body scriptSource: String): String { // TODO may want to return JSON here
        val result = auditingService.evaluate(scriptSource)
        logger.info { "Evaluation result: $result" }
        return result.toString()
    }
}
