package com.ampnet.auditornode.model.websocket

import com.ampnet.auditornode.model.contract.UnsignedTransaction
import com.ampnet.auditornode.script.api.model.AuditResult
import com.ampnet.auditornode.util.NativeReflection
import com.fasterxml.jackson.annotation.JsonInclude

@NativeReflection
enum class MessageType {
    COMMAND, INFO, RESPONSE, ERROR
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
data class SpecifyIpfsDirectoryHashCommand(val payload: AuditResult) : WebSocketCommand("specifyIpfsDirectoryHash")

@NativeReflection
enum class InputType {
    BOOLEAN, NUMBER, STRING;

    companion object {
        fun parse(value: String): InputType? = values().find { it.name == value.uppercase() }
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
object ExecutingInfoMessage : WebSocketInfoMessage("executing")

/* Response messages */
@NativeReflection
sealed class WebSocketResponse(val success: Boolean) : WebSocketMessage(MessageType.RESPONSE)

@JsonInclude(JsonInclude.Include.ALWAYS)
@NativeReflection
data class AuditResultResponse(val payload: AuditResult, val transaction: UnsignedTransaction?) :
    WebSocketResponse(success = true)

@NativeReflection
data class ErrorResponse(val payload: String) : WebSocketResponse(success = false)

/* Error messages */
@NativeReflection
sealed class WebSocketErrorMessage(val error: String) : WebSocketMessage(MessageType.ERROR)

@NativeReflection
data class NotFoundErrorMessage(val message: String? = null) : WebSocketErrorMessage(error = "notFound")

@NativeReflection
data class InvalidInputJsonErrorMessage(val message: String? = null) : WebSocketErrorMessage(error = "invalidInputJson")

@NativeReflection
data class IpfsReadErrorMessage(val message: String? = null) : WebSocketErrorMessage(error = "ipfsReadError")

@NativeReflection
data class RpcErrorMessage(val message: String? = null) : WebSocketErrorMessage(error = "rpcError")
