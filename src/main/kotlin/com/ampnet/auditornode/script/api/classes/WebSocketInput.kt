package com.ampnet.auditornode.script.api.classes

import com.ampnet.auditornode.script.api.model.MapApi
import io.micronaut.websocket.WebSocketSession
import org.graalvm.polyglot.HostAccess.Export
import org.graalvm.polyglot.Value
import java.util.concurrent.LinkedBlockingQueue

private enum class InputType {
    BOOLEAN, NUMBER, STRING;

    companion object {
        fun parse(value: String): InputType? = values().find { it.name == value.toUpperCase() }
    }
}

private data class InputField(
    val type: InputType,
    val name: String,
    val description: String
) {
    fun asWebSocketMessage(): String = "field:$type:$name:$description"
}

class WebSocketInput(private val session: WebSocketSession) : Input { // TODO refactor WebSocket commands
// TODO test
    private val queue = LinkedBlockingQueue<String>()

    private fun Value.getStringMember(name: String): String? {
        if (!hasMember(name)) {
            return null
        }

        val member = getMember(name)

        if (!member.isString) {
            return null
        }

        return member.asString()
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
        session.sendSync("readBoolean:$message")
        return queue.take().toBoolean()
    }

    @Export
    override fun readNumber(message: String): Double? {
        session.sendSync("readNumber:$message")
        return queue.take().toDoubleOrNull()
    }

    @Export
    override fun readString(message: String): String? {
        session.sendSync("readString:$message")
        return queue.take()
    }

    @Export
    override fun readFields(fields: Value, message: String): MapApi<String, Any>? {
        val inputFields = fields.asList()
            ?.mapNotNull { it.asInputField() } ?: return null

        session.sendSync("readFields:${inputFields.size}:$message")
        inputFields.forEach { session.sendSync(it.asWebSocketMessage()) }

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
