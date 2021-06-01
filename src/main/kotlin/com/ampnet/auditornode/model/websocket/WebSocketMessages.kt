package com.ampnet.auditornode.model.websocket

import com.ampnet.auditornode.persistence.model.UnsignedTransaction
import com.ampnet.auditornode.script.api.model.AuditResult
import com.ampnet.auditornode.util.NativeReflection
import com.fasterxml.jackson.annotation.JsonInclude

@NativeReflection
enum class MessageType {
    COMMAND, INFO, RESPONSE
}

@NativeReflection
sealed class WebSocketMessage(val messageType: MessageType)

/* WebSocket commands */
@NativeReflection
sealed class WebSocketCommand(val command: String) : WebSocketMessage(MessageType.COMMAND)

@NativeReflection
data class ReadInputJsonCommand(val message: String = "Please provide script input JSON") :
    WebSocketCommand("readInputJson")

@NativeReflection
data class ReadBooleanCommand(val message: String) : WebSocketCommand("readBoolean")

@NativeReflection
data class ReadNumberCommand(val message: String) : WebSocketCommand("readNumber")

@NativeReflection
data class ReadStringCommand(val message: String) : WebSocketCommand("readString")

@NativeReflection
enum class InputType {
    BOOLEAN, NUMBER, STRING;

    companion object {
        fun parse(value: String): InputType? = values().find { it.name == value.toUpperCase() }
    }
}

@NativeReflection
data class InputField(
    val type: InputType,
    val name: String,
    val description: String
)

@NativeReflection
data class ReadFieldsCommand(val message: String, val fields: List<InputField>) : WebSocketCommand("readFields")

@NativeReflection
data class ButtonCommand(val message: String) : WebSocketCommand("button")

@NativeReflection
data class RenderTextCommand(val text: String) : WebSocketCommand("renderText")

@NativeReflection
data class RenderHtmlCommand(val html: String) : WebSocketCommand("renderHtml")

@NativeReflection
data class RenderMarkdownCommand(val markdown: String) : WebSocketCommand("renderMarkdown")

/* Info messages */
@NativeReflection
sealed class WebSocketInfoMessage(val message: String) : WebSocketMessage(MessageType.INFO)

@NativeReflection
object ConnectedInfoMessage : WebSocketInfoMessage("connected")

@NativeReflection
object NotFoundInfoMessage : WebSocketInfoMessage("notFound")

@NativeReflection
object InvalidInputJsonInfoMessage : WebSocketInfoMessage("invalidInputJson")

@NativeReflection
object ExecutingInfoMessage : WebSocketInfoMessage("executing")

@NativeReflection
object IpfsReadErrorInfoMessage : WebSocketInfoMessage("ipfsReadError")

@NativeReflection
object RpcErrorInfoMessage : WebSocketInfoMessage("rpcError")

/* Response messages */
@NativeReflection
sealed class WebSocketResponse(val success: Boolean) : WebSocketMessage(MessageType.RESPONSE)

@JsonInclude(JsonInclude.Include.ALWAYS)
@NativeReflection
data class AuditResultResponse(val payload: AuditResult, val transaction: UnsignedTransaction?) :
    WebSocketResponse(success = true)

@NativeReflection
data class ErrorResponse(val payload: String) : WebSocketResponse(success = false)
