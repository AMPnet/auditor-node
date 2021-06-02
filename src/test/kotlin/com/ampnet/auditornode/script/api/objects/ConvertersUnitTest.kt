package com.ampnet.auditornode.script.api.objects

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.ampnet.auditornode.TestBase
import com.ampnet.auditornode.script.api.model.ListApi
import com.ampnet.auditornode.script.api.model.MapApi
import org.graalvm.polyglot.Value
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

class ConvertersUnitTest : TestBase() {

    @Test
    fun `must correctly transform Value which has array elements into a list`() {
        verify("list with some values is returned") {
            val value1 = mock<Value>()
            val value2 = mock<Value>()
            val arrayMock = mock<Value> {
                on { hasArrayElements() } doReturn true
                on { arraySize } doReturn 2
                on { getArrayElement(0) } doReturn value1
                on { getArrayElement(1) } doReturn value2
            }

            assertThat(Converters.arrayToList(arrayMock))
                .isEqualTo(ListApi(listOf(value1, value2)))
        }
    }

    @Test
    fun `must correctly transform Value which has no array elements into an empty list`() {
        verify("empty list is returned") {
            val arrayMock = mock<Value> {
                on { hasArrayElements() } doReturn false
            }

            assertThat(Converters.arrayToList(arrayMock))
                .isEqualTo(ListApi(emptyList()))
        }
    }

    @Test
    fun `must correctly transform Value which has members into a map`() {
        verify("map with some values is returned") {
            val value1 = mock<Value>()
            val value2 = mock<Value>()
            val objectMock = mock<Value> {
                on { hasMembers() } doReturn true
                on { memberKeys } doReturn setOf("key1", "key2")
                on { getMember("key1") } doReturn value1
                on { getMember("key2") } doReturn value2
            }

            assertThat(Converters.objectToMap(objectMock))
                .isEqualTo(
                    MapApi(
                        mapOf(
                            "key1" to value1,
                            "key2" to value2
                        )
                    )
                )
        }
    }

    @Test
    fun `must correctly transform Value which has no members into an empty map`() {
        verify("empty map is returned") {
            val objectMock = mock<Value> {
                on { hasMembers() } doReturn false
            }

            assertThat(Converters.objectToMap(objectMock))
                .isEqualTo(MapApi(emptyMap()))
        }
    }
}
