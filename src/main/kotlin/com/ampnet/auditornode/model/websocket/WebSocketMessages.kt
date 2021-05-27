package com.ampnet.auditornode.model.websocket

import com.ampnet.auditornode.persistence.model.UnsignedTransaction
import com.ampnet.auditornode.script.api.model.AuditResult
import com.fasterxml.jackson.annotation.JsonInclude

enum class MessageType {
    COMMAND, INFO, RESPONSE
}

sealed class WebSocketMessage(val messageType: MessageType)

/* WebSocket commands */
sealed class WebSocketCommand(val command: String) : WebSocketMessage(MessageType.COMMAND)

data class ReadInputJsonCommand(val message: String = "Please provide script input JSON") :
    WebSocketCommand("readInputJson")

data class ReadBooleanCommand(val message: String) : WebSocketCommand("readBoolean")

data class ReadNumberCommand(val message: String) : WebSocketCommand("readNumber")

data class ReadStringCommand(val message: String) : WebSocketCommand("readString")

enum class InputType {
    BOOLEAN, NUMBER, STRING;

    companion object {
        fun parse(value: String): InputType? = values().find { it.name == value.toUpperCase() }
    }
}

data class InputField(
    val type: InputType,
    val name: String,
    val description: String
)

data class ReadFieldsCommand(val message: String, val fields: List<InputField>) : WebSocketCommand("readFields")

data class ButtonCommand(val message: String) : WebSocketCommand("button")

data class RenderTextCommand(val text: String) : WebSocketCommand("renderText")

data class RenderHtmlCommand(val html: String) : WebSocketCommand("renderHtml")

data class RenderMarkdownCommand(val markdown: String) : WebSocketCommand("renderMarkdown")

/* Info messages */
sealed class WebSocketInfoMessage(val message: String) : WebSocketMessage(MessageType.INFO)

object ConnectedInfoMessage : WebSocketInfoMessage("connected")

object NotFoundInfoMessage : WebSocketInfoMessage("notFound")

object InvalidInputJsonInfoMessage : WebSocketInfoMessage("invalidInputJson")

object ExecutingInfoMessage : WebSocketInfoMessage("executing")

object IpfsReadErrorInfoMessage : WebSocketInfoMessage("ipfsReadError")

object RpcErrorInfoMessage : WebSocketInfoMessage("rpcError")

/* Response messages */
sealed class WebSocketResponse(val success: Boolean) : WebSocketMessage(MessageType.RESPONSE)

@JsonInclude(JsonInclude.Include.ALWAYS)
data class AuditResultResponse(val payload: AuditResult, val transaction: UnsignedTransaction?) :
    WebSocketResponse(success = true)

data class ErrorResponse(val payload: String) : WebSocketResponse(success = false)
