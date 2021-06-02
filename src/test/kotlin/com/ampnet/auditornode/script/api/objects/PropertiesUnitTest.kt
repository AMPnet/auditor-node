package com.ampnet.auditornode.script.api.objects

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.ampnet.auditornode.TestBase
import com.ampnet.auditornode.configuration.properties.ScriptProperties
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

class PropertiesUnitTest : TestBase() {

    @Test
    fun `must correctly construct JavaScript API object`() {
        verify("correct API object is constructed") {
            val expectedObject = "{\"entry1\":\"value1\",\"entry2\":\"value2\"}"
            val scriptProperties = mock<ScriptProperties> {
                on { properties } doReturn mapOf(
                    "entry1" to "value1",
                    "entry2" to "value2"
                )
            }

            assertThat(Properties(scriptProperties).createJavaScriptApiObject())
                .isEqualTo("const Properties = $expectedObject;")
        }
    }
}
