package com.ampnet.auditornode.controller.websocket

import com.ampnet.auditornode.model.websocket.InitState
import com.ampnet.auditornode.model.websocket.WebSocketScriptState
import com.ampnet.auditornode.persistence.model.IpfsHash
import com.ampnet.auditornode.persistence.model.ScriptSource
import com.ampnet.auditornode.script.api.classes.WebSocketInput
import io.micronaut.websocket.WebSocketSession
import io.reactivex.disposables.Disposable
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

object WebSocketSessionHelper {

    private const val SCRIPT_ATTRIBUTE = "Script"
    private const val SCRIPT_INPUT_ATTRIBUTE = "ScriptInput"
    private const val SCRIPT_DIRECTORY_IPFS_HASH_ATTRIBUTE = "ScriptDirectoryIpfsHash"
    private const val SCRIPT_TASK_ATTRIBUTE = "ScriptTask"
    private const val SCRIPT_STATE_ATTRIBUTE = "ScriptState"

    var WebSocketSession.script: ScriptSource
        get() = attributes[SCRIPT_ATTRIBUTE, ScriptSource::class.java].get()
        set(value) {
            logger.debug { "Script source set: $value" }
            attributes.put(SCRIPT_ATTRIBUTE, value)
        }

    var WebSocketSession.scriptInput: WebSocketInput?
        get() = attributes[SCRIPT_INPUT_ATTRIBUTE, WebSocketInput::class.java].orElse(null)
        set(value) {
            logger.debug { "Script input set" }
            attributes.put(SCRIPT_INPUT_ATTRIBUTE, value)
        }

    var WebSocketSession.scriptIpfsDirectoryHash: IpfsHash?
        get() = attributes[SCRIPT_DIRECTORY_IPFS_HASH_ATTRIBUTE, IpfsHash::class.java].orElse(null)
        set(value) {
            logger.debug { "Script directory IPFS hash set: $value" }
            attributes.put(SCRIPT_DIRECTORY_IPFS_HASH_ATTRIBUTE, value)
        }

    var WebSocketSession.scriptTask: Disposable?
        get() = attributes[SCRIPT_TASK_ATTRIBUTE, Disposable::class.java].orElse(null)
        set(value) {
            logger.debug { "Script task set" }
            attributes.put(SCRIPT_TASK_ATTRIBUTE, value)
        }

    var WebSocketSession.scriptState: WebSocketScriptState
        get() = attributes[SCRIPT_STATE_ATTRIBUTE, WebSocketScriptState::class.java].orElse(InitState)
        set(value) {
            logger.debug { "Script state set: $value" }
            attributes.put(SCRIPT_STATE_ATTRIBUTE, value)
        }
}
