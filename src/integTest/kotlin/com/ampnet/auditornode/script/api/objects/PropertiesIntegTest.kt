package com.ampnet.auditornode.script.api.objects

import assertk.assertThat
import com.ampnet.auditornode.TestBase
import com.ampnet.auditornode.configuration.properties.ScriptProperties
import com.ampnet.auditornode.isRightContaining
import com.ampnet.auditornode.jsAssertions
import com.ampnet.auditornode.script.api.ExecutionContext
import com.ampnet.auditornode.script.api.classes.HttpClient
import com.ampnet.auditornode.script.api.model.SuccessfulAudit
import com.ampnet.auditornode.service.impl.JavaScriptAuditingService
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import org.mockito.kotlin.given
import org.mockito.kotlin.mock

class PropertiesIntegTest : TestBase() {

    private val httpClient = HttpClient(mock())

    @Test
    fun `must correctly read provided environment API object`() {
        val mockProperties = mock<ScriptProperties>()

        suppose("some JavaScript properties variables are set through application properties") {
            given(mockProperties.properties)
                .willReturn(
                    mapOf(
                        "test-key" to "testValue",
                        "another" to "one",
                        "some-number" to "123"
                    )
                )
        }

        val properties = Properties(mockProperties)
        val service = JavaScriptAuditingService(httpClient, properties)

        verify("JavaScript properties variables are accessible in the script") {
            @Language("JavaScript") val scriptSource = jsAssertions + """
                function audit(auditData) {
                    console.log(JSON.stringify(Properties));

                    assertEquals("Properties[\"test-key\"]", "testValue", Properties["test-key"]);
                    assertEquals("Properties.another", "one", Properties.another);
                    assertEquals("Properties[\"some-number\"]", "123", Properties["some-number"]);

                    return AuditResult.success();
                }
            """.trimIndent()
            val result = service.evaluate(scriptSource, ExecutionContext.noOp)
            assertThat(result).isRightContaining(SuccessfulAudit)
        }
    }
}
