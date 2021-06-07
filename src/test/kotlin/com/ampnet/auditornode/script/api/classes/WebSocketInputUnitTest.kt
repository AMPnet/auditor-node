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

        fun fieldMock(type: String?, name: String?, description: String?): Value {
            val typeValue = mock<Value> {
                on { isString } doReturn true
                on { asString() } doReturn type
            }

            val nameValue = mock<Value> {
                on { isString } doReturn true
                on { asString() } doReturn name
            }

            val descriptionValue = mock<Value> {
                on { isString } doReturn true
                on { asString() } doReturn description
            }

            return mock {
                on { hasMember(TYPE) } doReturn (type != null)
                on { getMember(TYPE) } doReturn typeValue

                on { hasMember(NAME) } doReturn (name != null)
                on { getMember(NAME) } doReturn nameValue

                on { hasMember(DESCRIPTION) } doReturn (description != null)
                on { getMember(DESCRIPTION) } doReturn descriptionValue
            }
        }

        fun validFieldMock(type: InputType, name: String, description: String) =
            fieldMock(type = type.name, name = name, description = description)

        verify("correct web socket command is sent and values are correctly read") {
            val nonStringValue = mock<Value> {
                on { isString } doReturn false
            }
            val fieldWithNonStringType = mock<Value> {
                on { hasMember(TYPE) } doReturn true
                on { getMember(TYPE) } doReturn nonStringValue
            }

            val fieldWithUnknownType = fieldMock(type = "unknown", name = null, description = null)

            val fieldWithNoType = fieldMock(type = null, name = null, description = null)
            val fieldWithNoName = fieldMock(type = InputType.STRING.name, name = null, description = null)
            val fieldWithNoDescription = fieldMock(type = InputType.STRING.name, name = "unused", description = null)

            val booleanField = validFieldMock(type = InputType.BOOLEAN, name = "name1", description = "description1")
            val numberField = validFieldMock(type = InputType.NUMBER, name = "name2", description = "description2")
            val stringField = validFieldMock(type = InputType.STRING, name = "name3", description = "description3")

            val arrayMock = mock<Value> {
                on { hasArrayElements() } doReturn true
                on { arraySize } doReturn 9
                on { getArrayElement(1) } doReturn fieldWithNonStringType
                on { getArrayElement(2) } doReturn fieldWithUnknownType
                on { getArrayElement(0) } doReturn fieldWithNoType
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
        suppose("there are some string values in the queue") {
            service.push("value1")
            service.push("value2")
            service.push("value3")
        }

        verify("correct web socket command is sent") {
            val message = "test message"
            val queueValue1 = service.readString("")
            val queueValue2 = service.button(message)
            val queueValue3 = service.readString("")

            assertThat(queueValue1)
                .isEqualTo("value1")
            assertThat(queueValue2)
                .isEqualTo(Unit)
            assertThat(queueValue3)
                .isEqualTo("value3")

            then(webSocketApi)
                .should(times(1))
                .sendCommand(ButtonCommand(message))
        }
    }
}
