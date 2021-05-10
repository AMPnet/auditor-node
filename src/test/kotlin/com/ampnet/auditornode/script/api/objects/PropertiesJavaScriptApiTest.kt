package com.ampnet.auditornode.script.api.objects

import assertk.assertThat
import com.ampnet.auditornode.TestBase
import com.ampnet.auditornode.configuration.properties.ScriptProperties
import com.ampnet.auditornode.isRightContaining
import com.ampnet.auditornode.script.api.classes.HttpClient
import com.ampnet.auditornode.script.api.model.AuditResult
import com.ampnet.auditornode.service.impl.JavaScriptAuditingService
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import org.mockito.kotlin.given
import org.mockito.kotlin.mock

class PropertiesJavaScriptApiTest : TestBase() {

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
            @Language("JavaScript") val scriptSource = """
                function audit() {
                    console.log(JSON.stringify(Properties));

                    if (Properties["test-key"] !== "testValue") {
                        console.log("Environment[\"test-key\"] mismatch");
                        return AuditResult.of(false);
                    }

                    if (Properties.another !== "one") {
                        console.log("Environment.another mismatch");
                        return AuditResult.of(false);
                    }
                    
                    if (Properties["some-number"] === "123") {
                        console.log("Success: " + JSON.stringify(Properties));
                        return AuditResult.of(true);
                    } else {
                        console.log("Environment[\"some-number\"] mismatch");
                        return AuditResult.of(false);
                    }
                }
            """.trimIndent()
            val result = service.evaluate(scriptSource)
            assertThat(result).isRightContaining(AuditResult(true))
        }
    }
}
