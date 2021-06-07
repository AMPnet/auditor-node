package com.ampnet.auditornode.controller

import arrow.core.left
import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import com.ampnet.auditornode.TestBase
import com.ampnet.auditornode.model.error.EvaluationError.ScriptExecutionError
import com.ampnet.auditornode.model.response.ExecuteScriptErrorResponse
import com.ampnet.auditornode.model.response.ExecuteScriptOkResponse
import com.ampnet.auditornode.model.response.StoreScriptResponse
import com.ampnet.auditornode.persistence.model.ScriptId
import com.ampnet.auditornode.persistence.model.ScriptSource
import com.ampnet.auditornode.persistence.repository.ScriptRepository
import com.ampnet.auditornode.script.api.ExecutionContext
import com.ampnet.auditornode.script.api.model.SuccessfulAudit
import com.ampnet.auditornode.service.AuditingService
import io.micronaut.http.HttpStatus
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import java.util.UUID

class ScriptControllerUnitTest : TestBase() {

    private val auditingService = mock<AuditingService>()
    private val scriptRepository = mock<ScriptRepository>()
    private val controller = ScriptController(auditingService, scriptRepository)

    @BeforeEach
    fun beforeEach() {
        reset(auditingService, scriptRepository)
    }

    @Test
    fun `must return ok response for successful script execution`() {
        val result = SuccessfulAudit
        val scriptSource = "example script"

        suppose("some script will be successfully executed") {
            given(auditingService.evaluate(scriptSource, ExecutionContext.noOp))
                .willReturn(result.right())
        }

        verify("ok response is returned") {
            val response = controller.executeScript(scriptSource)

            assertThat(response)
                .isEqualTo(ExecuteScriptOkResponse(result))
        }
    }

    @Test
    fun `must return error response for failed script execution`() {
        val errorMessage = "message"
        val result = ScriptExecutionError(RuntimeException(errorMessage))
        val scriptSource = "example script"

        suppose("some script will be unsuccessfully executed") {
            given(auditingService.evaluate(scriptSource, ExecutionContext.noOp))
                .willReturn(result.left())
        }

        verify("error response is returned") {
            val response = controller.executeScript(scriptSource)

            assertThat(response)
                .isEqualTo(ExecuteScriptErrorResponse(result.message))
        }
    }

    @Test
    fun `must return script ID when storing a script`() {
        val scriptId = ScriptId(UUID.randomUUID())
        val scriptSource = "example script"

        suppose("script source is stored") {
            given(scriptRepository.store(ScriptSource(scriptSource)))
                .willAnswer { scriptId.value } // for value classes we must return value with willAnswer
        }

        verify("script ID is returned") {
            val response = controller.storeScript(scriptSource)

            assertThat(response)
                .isEqualTo(StoreScriptResponse(scriptId))
        }
    }

    @Test
    fun `must return script source when loading script by ID`() {
        val scriptId = UUID.randomUUID()
        val scriptSource = "example script"

        suppose("script source is loaded from the repository") {
            given(scriptRepository.load(ScriptId(scriptId)))
                .willAnswer { scriptSource } // for value classes we must return value with willAnswer
        }

        verify("ok response with script source is returned") {
            val response = controller.loadScript(scriptId)

            assertThat(response.status)
                .isEqualTo(HttpStatus.OK)
            assertThat(response.body())
                .isEqualTo(scriptSource)
        }
    }

    @Test
    fun `must return 404 when loading non existent script`() {
        val scriptId = UUID.randomUUID()

        suppose("script source is not found in the repository") {
            given(scriptRepository.load(ScriptId(scriptId)))
                .willReturn(null)
        }

        verify("not found response is returned") {
            val response = controller.loadScript(scriptId)

            assertThat(response.status)
                .isEqualTo(HttpStatus.NOT_FOUND)
        }
    }
}
