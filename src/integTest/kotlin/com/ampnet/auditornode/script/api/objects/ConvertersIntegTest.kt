package com.ampnet.auditornode.script.api.objects

import assertk.assertThat
import com.ampnet.auditornode.TestBase
import com.ampnet.auditornode.isRightContaining
import com.ampnet.auditornode.jsAssertions
import com.ampnet.auditornode.script.api.ExecutionContext
import com.ampnet.auditornode.script.api.classes.HttpClient
import com.ampnet.auditornode.script.api.model.SuccessfulAudit
import com.ampnet.auditornode.service.impl.JavaScriptAuditingService
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock

class ConvertersIntegTest : TestBase() {

    private val httpClient = HttpClient(mock())
    private val environment = Properties(mock())
    private val service = JavaScriptAuditingService(httpClient, environment)

    @Test
    fun `must correctly convert JS array into a list and vice-versa`() {
        verify("JS array is correctly converted into a list and then back into an array") {
            @Language("JavaScript") val scriptSource = jsAssertions + """
                function audit(auditData) {
                    const array = [1, 2, "three"];
                    const list = Converters.arrayToList(array);

                    assertEquals("list.length", array.length, list.length);

                    for (let i = 0; i < list.length; i++) {
                        assertEquals("list.get(" + i + ")", array[i], list.get(i));
                    }

                    const newArray = Converters.listToArray(list);

                    assertEquals("newArray.length", array.length, newArray.length);

                    for (let i = 0; i < newArray.length; i++) {
                        assertEquals("newArray[" + i + "]", array[i], newArray[i]);
                    }

                    return AuditResult.success();
                }
            """.trimIndent()
            val result = service.evaluate(scriptSource, ExecutionContext.noOp)
            assertThat(result).isRightContaining(SuccessfulAudit)
        }
    }

    @Test
    fun `must correctly convert JS object into a map and vice-versa`() {
        verify("JS object is correctly converted into a map and then back into an object") {
            @Suppress("JSUnfilteredForInLoop")
            @Language("JavaScript") val scriptSource = jsAssertions + """
                function audit(auditData) {
                    const object = {
                        prop1: "string",
                        prop2: 123,
                        "other-prop": "test"
                    };
                    const map = Converters.objectToMap(object);

                    assertEquals("map.size", Object.getOwnPropertyNames(object).length, map.size);

                    const keys = map.keys();

                    assertEquals("keys.length", map.size, keys.length);

                    for (let i = 0; i < keys.length; i++) {
                        const key = keys.get(i);
                        assertEquals("map.get(\"" + key + "\")", map.get(key), object[key]);
                    }

                    const newObject = Converters.mapToObject(map);

                    assertEquals(
                        "Object.getOwnPropertyNames(newObject).length",
                        Object.getOwnPropertyNames(object).length,
                        Object.getOwnPropertyNames(newObject).length
                    );

                    for (const property in newObject) {
                        assertEquals("newObject[\"" + property + "\"]", object[property], newObject[property]);
                    }

                    return AuditResult.success();
                }
            """.trimIndent()
            val result = service.evaluate(scriptSource, ExecutionContext.noOp)
            assertThat(result).isRightContaining(SuccessfulAudit)
        }
    }
}
