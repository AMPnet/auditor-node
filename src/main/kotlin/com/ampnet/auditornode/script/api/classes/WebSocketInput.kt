package com.ampnet.auditornode.script.api.classes

import com.ampnet.auditornode.model.websocket.InputField
import com.ampnet.auditornode.model.websocket.InputType
import com.ampnet.auditornode.model.websocket.ReadBooleanCommand
import com.ampnet.auditornode.model.websocket.ReadFieldsCommand
import com.ampnet.auditornode.model.websocket.ReadNumberCommand
import com.ampnet.auditornode.model.websocket.ReadStringCommand
import com.ampnet.auditornode.model.websocket.WebSocketApi
import com.ampnet.auditornode.script.api.model.MapApi
import org.graalvm.polyglot.HostAccess.Export
import org.graalvm.polyglot.Value
import java.util.concurrent.LinkedBlockingQueue

class WebSocketInput(private val webSocketApi: WebSocketApi) : Input {

    private val queue = LinkedBlockingQueue<String>()

    private fun Value.getStringMember(name: String): String? {
        if (!hasMember(name)) {
            return null
        }

        val member = getMember(name)

        return if (member.isString) {
            member.asString()
        } else {
            null
        }
    }

    private fun Value.asInputField(): InputField? {
        val type = getStringMember("type")?.let { InputType.parse(it) }
        val name = getStringMember("name")
        val description = getStringMember("description")

        if (type == null || name == null || description == null) {
            return null
        }

        return InputField(type, name, description)
    }

    private fun Value.asList(): List<Value>? {
        if (!hasArrayElements()) {
            return null
        }

        return (0 until arraySize).map { getArrayElement(it) }
    }

    fun push(value: String) {
        queue.add(value)
    }

    @Export
    override fun readBoolean(message: String): Boolean {
        webSocketApi.sendCommand(ReadBooleanCommand(message))
        return queue.take().toBoolean()
    }

    @Export
    override fun readNumber(message: String): Double? {
        webSocketApi.sendCommand(ReadNumberCommand(message))
        return queue.take().toDoubleOrNull()
    }

    @Export
    override fun readString(message: String): String? {
        webSocketApi.sendCommand(ReadStringCommand(message))
        return queue.take()
    }

    @Export
    override fun readFields(fields: Value, message: String): MapApi<String, Any>? {
        val inputFields = fields.asList()
            ?.mapNotNull { it.asInputField() } ?: return null

        webSocketApi.sendCommand(ReadFieldsCommand(message, inputFields))

        val inputs = inputFields.mapNotNull { field ->
            val fieldInput = queue.take()
            val fieldValue: Any? = when (field.type) {
                InputType.BOOLEAN -> fieldInput.toBoolean()
                InputType.NUMBER -> fieldInput.toDoubleOrNull()
                InputType.STRING -> fieldInput
            }
            fieldValue?.let { Pair(field.name, it) }
        }.toMap()

        return MapApi(inputs)
    }
}
