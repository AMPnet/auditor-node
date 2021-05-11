package com.ampnet.auditornode.script.api.objects

import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import com.ampnet.auditornode.ApiTestBase
import com.ampnet.auditornode.jsAssertions
import com.ampnet.auditornode.script.api.model.AuditResult
import io.micronaut.http.HttpRequest
import io.micronaut.http.MediaType
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test

@MicronautTest
class ConvertersJavaScriptApiTest : ApiTestBase() {

    @Test
    fun `must correctly execute auditing script which uses Converters list API`() {
        @Language("JavaScript") val scriptSource = jsAssertions + """
            function audit() {
                let array = [1, 2, "three"];
                let list = Converters.arrayToList(array);

                assertEquals("list.length", array.length, list.length);

                for (var i = 0; i < list.length; i++) {
                    assertEquals("list.get(" + i + ")", array[i], list.get(i));
                }

                let newArray = Converters.listToArray(list);

                assertEquals("newArray.length", array.length, newArray.length);

                for (i = 0; i < newArray.length; i++) {
                    assertEquals("newArray[" + i + "]", array[i], newArray[i]);
                }

                return AuditResult.of(true);
            }
        """.trimIndent()

        val result = client.toBlocking().retrieve(
            HttpRequest.POST("/script/execute", scriptSource).apply {
                contentType(MediaType.TEXT_PLAIN_TYPE)
            }
        )

        assertThat(result).isEqualTo(AuditResult(true).right().toString())
    }

    @Test
    fun `must correctly execute auditing script which uses Converters map API`() {
        @Suppress("JSUnfilteredForInLoop")
        @Language("JavaScript") val scriptSource = jsAssertions + """
            function audit() {
                let object = {
                    prop1: "string",
                    prop2: 123,
                    "other-prop": "test"
                };
                let map = Converters.objectToMap(object);

                assertEquals("map.size", Object.getOwnPropertyNames(object).length, map.size);

                let keys = map.keys();

                assertEquals("keys.length", map.size, keys.length);

                for (var i = 0; i < keys.length; i++) {
                    let key = keys.get(i);
                    assertEquals("map.get(\"" + key + "\")", map.get(key), object[key]);
                }

                let newObject = Converters.mapToObject(map);

                assertEquals(
                    "Object.getOwnPropertyNames(newObject).length",
                    Object.getOwnPropertyNames(object).length,
                    Object.getOwnPropertyNames(newObject).length
                );

                for (property in newObject) {
                    assertEquals("newObject[\"" + property + "\"]", object[property], newObject[property]);
                }

                return AuditResult.of(true);
            }
        """.trimIndent()

        val result = client.toBlocking().retrieve(
            HttpRequest.POST("/script/execute", scriptSource).apply {
                contentType(MediaType.TEXT_PLAIN_TYPE)
            }
        )

        assertThat(result).isEqualTo(AuditResult(true).right().toString())
    }
}
