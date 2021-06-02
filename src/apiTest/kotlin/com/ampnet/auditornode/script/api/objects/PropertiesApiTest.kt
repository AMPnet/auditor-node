package com.ampnet.auditornode.script.api.objects

import assertk.assertThat
import com.ampnet.auditornode.ApiTestWithPropertiesBase
import com.ampnet.auditornode.isJsonEqualTo
import com.ampnet.auditornode.jsAssertions
import com.ampnet.auditornode.model.response.ExecuteScriptOkResponse
import com.ampnet.auditornode.script.api.model.SuccessfulAudit
import io.micronaut.http.HttpRequest
import io.micronaut.http.MediaType
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test

@MicronautTest(propertySources = ["script-properties-test.yaml"])
class PropertiesApiTest : ApiTestWithPropertiesBase("script-properties-test") {

    @Test
    fun `must correctly execute auditing script which uses script properties`() {
        verify("script properties are readable in the script") {
            @Language("JavaScript") val scriptSource = jsAssertions + """
                function audit(auditData) {
                    console.log(JSON.stringify(Properties));

                    assertEquals("Properties[\"test-property\"]", "example", Properties["test-property"]);
                    assertEquals("Properties[\"another-test-property\"]", "value", Properties["another-test-property"]);
                    assertEquals("Properties[\"number-property\"]", "123", Properties["number-property"]);

                    return AuditResult.success();
                }
            """.trimIndent()

            val result = client.toBlocking().retrieve(
                HttpRequest.POST("${serverPath()}/script/execute", scriptSource).apply {
                    contentType(MediaType.TEXT_PLAIN_TYPE)
                }
            )

            assertThat(result).isJsonEqualTo(ExecuteScriptOkResponse(SuccessfulAudit))
        }
    }
}
