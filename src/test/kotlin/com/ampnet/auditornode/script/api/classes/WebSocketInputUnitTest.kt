package com.ampnet.auditornode.script.api.classes

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import com.ampnet.auditornode.TestBase
import com.ampnet.auditornode.model.websocket.ButtonCommand
import com.ampnet.auditornode.model.websocket.InputField
import com.ampnet.auditornode.model.websocket.InputType
import com.ampnet.auditornode.model.websocket.ReadBooleanCommand
import com.ampnet.auditornode.model.websocket.ReadFieldsCommand
import com.ampnet.auditornode.model.websocket.ReadNumberCommand
import com.ampnet.auditornode.model.websocket.ReadStringCommand
import com.ampnet.auditornode.model.websocket.WebSocketApi
import com.ampnet.auditornode.script.api.model.MapApi
import org.graalvm.polyglot.Value
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.then
import org.mockito.kotlin.times

class WebSocketInputUnitTest : TestBase() {

    private val webSocketApi = mock<WebSocketApi>()
    private val service = WebSocketInput(webSocketApi)

    companion object {
        const val TYPE = "type"
        const val NAME = "name"
        const val DESCRIPTION = "description"
    }

    @BeforeEach
    fun beforeEach() {
        reset(webSocketApi)
    }

    @Test
    fun `must correctly send web socket command for readBoolean() call and take value from queue`() {
        val value = true

        suppose("there is some boolean value in the queue") {
            service.push(value.toString())
        }

        verify("correct web socket command is sent and value is correctly read") {
            val message = "test message"
            val result = service.readBoolean(message)

            assertThat(result).isEqualTo(value)
            then(webSocketApi)
                .should(times(1))
                .sendCommand(ReadBooleanCommand(message))
        }
    }

    @Test
    fun `must correctly send web socket command for readNumber() call and take value from queue`() {
        val value = 123.0

        suppose("there is some number value in the queue") {
            service.push(value.toString())
        }

        verify("correct web socket command is sent and value is correctly read") {
            val message = "test message"
            val result = service.readNumber(message)

            assertThat(result).isEqualTo(value)
            then(webSocketApi)
                .should(times(1))
                .sendCommand(ReadNumberCommand(message))
        }
    }

    @Test
    fun `must correctly send web socket command for readString() call and take value from queue`() {
        val value = "test string"

        suppose("there is some string value in the queue") {
            service.push(value)
        }

        verify("correct web socket command is sent and value is correctly read") {
            val message = "test message"
            val result = service.readString(message)

            assertThat(result).isEqualTo(value)
            then(webSocketApi)
                .should(times(1))
                .sendCommand(ReadStringCommand(message))
        }
    }

    @Test
    fun `must not send web socket command and return null when calling readFields() with non-array value`() {
        verify("no web socket command message is send and null is returned") {
            val arrayMock = mock<Value> {
                on { hasArrayElements() } doReturn false
            }
            val result = service.readFields(arrayMock, "")

            assertThat(result).isNull()
            then(webSocketApi)
                .shouldHaveNoInteractions()
        }
    }

    @Test
    fun `must correctly send web socket command and return values for readFields() call`() {
        val booleanValue = true
        val numberValue = 123.0
        val stringValue = "test value"

        suppose("there are some values in the queue") {
            service.push(booleanValue.toString())
            service.push(numberValue.toString())
            service.push(stringValue)
        }

        verify("correct web socket command is sent and values are correctly read") {
            val fieldWithNoType = mock<Value> {
                on { hasMember(TYPE) } doReturn false
            }

            val nonStringValue = mock<Value> {
                on { isString } doReturn false
            }
            val fieldNonStringType = mock<Value> {
                on { hasMember(TYPE) } doReturn true
                on { getMember(TYPE) } doReturn nonStringValue
            }

            val unknownTypeValue = mock<Value> {
                on { isString } doReturn true
                on { asString() } doReturn "unknown"
            }
            val fieldWithUnknownType = mock<Value> {
                on { hasMember(TYPE) } doReturn true
                on { getMember(TYPE) } doReturn unknownTypeValue
            }

            val booleanTypeValue = mock<Value> {
                on { isString } doReturn true
                on { asString() } doReturn InputType.BOOLEAN.name
            }
            val fieldWithNoName = mock<Value> {
                on { hasMember(TYPE) } doReturn true
                on { getMember(TYPE) } doReturn booleanTypeValue
                on { hasMember(NAME) } doReturn false
            }

            val unusedNameValue = mock<Value> {
                on { isString } doReturn true
                on { asString() } doReturn "unusedName"
            }
            val fieldWithNoDescription = mock<Value> {
                on { hasMember(TYPE) } doReturn true
                on { getMember(TYPE) } doReturn booleanTypeValue
                on { hasMember(NAME) } doReturn true
                on { getMember(NAME) } doReturn unusedNameValue
                on { hasMember(DESCRIPTION) } doReturn false
            }

            val name1Value = mock<Value> {
                on { isString } doReturn true
                on { asString() } doReturn "name1"
            }
            val description1Value = mock<Value> {
                on { isString } doReturn true
                on { asString() } doReturn "description1"
            }
            val booleanField = mock<Value> {
                on { hasMember(TYPE) } doReturn true
                on { getMember(TYPE) } doReturn booleanTypeValue
                on { hasMember(NAME) } doReturn true
                on { getMember(NAME) } doReturn name1Value
                on { hasMember(DESCRIPTION) } doReturn true
                on { getMember(DESCRIPTION) } doReturn description1Value
            }

            val numberTypeValue = mock<Value> {
                on { isString } doReturn true
                on { asString() } doReturn InputType.NUMBER.name
            }
            val name2Value = mock<Value> {
                on { isString } doReturn true
                on { asString() } doReturn "name2"
            }
            val description2Value = mock<Value> {
                on { isString } doReturn true
                on { asString() } doReturn "description2"
            }
            val numberField = mock<Value> {
                on { hasMember(TYPE) } doReturn true
                on { getMember(TYPE) } doReturn numberTypeValue
                on { hasMember(NAME) } doReturn true
                on { getMember(NAME) } doReturn name2Value
                on { hasMember(DESCRIPTION) } doReturn true
                on { getMember(DESCRIPTION) } doReturn description2Value
            }

            val stringTypeValue = mock<Value> {
                on { isString } doReturn true
                on { asString() } doReturn InputType.STRING.name
            }
            val name3Value = mock<Value> {
                on { isString } doReturn true
                on { asString() } doReturn "name3"
            }
            val description3Value = mock<Value> {
                on { isString } doReturn true
                on { asString() } doReturn "description3"
            }
            val stringField = mock<Value> {
                on { hasMember(TYPE) } doReturn true
                on { getMember(TYPE) } doReturn stringTypeValue
                on { hasMember(NAME) } doReturn true
                on { getMember(NAME) } doReturn name3Value
                on { hasMember(DESCRIPTION) } doReturn true
                on { getMember(DESCRIPTION) } doReturn description3Value
            }

            val arrayMock = mock<Value> {
                on { hasArrayElements() } doReturn true
                on { arraySize } doReturn 9
                on { getArrayElement(0) } doReturn fieldWithNoType
                on { getArrayElement(1) } doReturn fieldNonStringType
                on { getArrayElement(2) } doReturn fieldWithUnknownType
                on { getArrayElement(3) } doReturn fieldWithNoName
                on { getArrayElement(4) } doReturn fieldWithNoDescription
                on { getArrayElement(5) } doReturn booleanField
                on { getArrayElement(6) } doReturn numberField
                on { getArrayElement(7) } doReturn stringField
                on { getArrayElement(8) } doReturn null
            }

            val message = "test message"
            val result = service.readFields(arrayMock, message)

            assertThat(result)
                .isNotNull()
                .isEqualTo(
                    MapApi(
                        mapOf(
                            "name1" to booleanValue,
                            "name2" to numberValue,
                            "name3" to stringValue
                        )
                    )
                )
            then(webSocketApi)
                .should(times(1))
                .sendCommand(
                    ReadFieldsCommand(
                        message,
                        listOf(
                            InputField(InputType.BOOLEAN, "name1", "description1"),
                            InputField(InputType.NUMBER, "name2", "description2"),
                            InputField(InputType.STRING, "name3", "description3")
                        )
                    )
                )
        }
    }

    @Test
    fun `must correctly send web socket command for button() call and take value from queue`() {
        suppose("there is some string value in the queue") {
            service.push("test value")
        }

        verify("correct web socket command is sent") {
            val message = "test message"
            service.button(message)

            then(webSocketApi)
                .should(times(1))
                .sendCommand(ButtonCommand(message))
        }
    }
}
