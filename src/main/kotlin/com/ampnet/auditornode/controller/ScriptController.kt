package com.ampnet.auditornode.controller

import com.ampnet.auditornode.controller.documentation.ScriptControllerDocumentation
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
import io.micronaut.http.annotation.Controller
import mu.KotlinLogging
import java.util.UUID
import javax.inject.Inject

private val logger = KotlinLogging.logger {}

@Controller("/script")
class ScriptController @Inject constructor(
    private val auditingService: AuditingService,
    private val scriptRepository: ScriptRepository
) : ScriptControllerDocumentation {

    override fun executeScript(scriptSource: String): ExecuteScriptResponse {
        val result = auditingService.evaluate(scriptSource, ExecutionContext.noOp)
        logger.info { "Evaluation result: $result" }
        return result.fold(
            ifLeft = { ExecuteScriptErrorResponse(it.message) },
            ifRight = { ExecuteScriptOkResponse(it) }
        )
    }

    override fun storeScript(scriptSource: String): StoreScriptResponse {
        val scriptId = scriptRepository.store(ScriptSource(scriptSource))
        logger.info { "Script stored under ID: $scriptId" }
        return StoreScriptResponse(scriptId)
    }

    override fun loadScript(scriptId: UUID): HttpResponse<String> {
        logger.info { "Load script request, script ID: $scriptId" }
        val scriptSource = scriptRepository.load(ScriptId(scriptId))
        return scriptSource?.let { HttpResponse.ok(it.content) } ?: HttpResponse.notFound()
    }
}
