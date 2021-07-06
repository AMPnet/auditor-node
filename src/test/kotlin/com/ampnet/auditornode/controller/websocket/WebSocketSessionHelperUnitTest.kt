package com.ampnet.auditornode.controller.websocket

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isSameAs
import com.ampnet.auditornode.TestBase
import com.ampnet.auditornode.controller.websocket.WebSocketSessionHelper.script
import com.ampnet.auditornode.controller.websocket.WebSocketSessionHelper.scriptInput
import com.ampnet.auditornode.controller.websocket.WebSocketSessionHelper.scriptIpfsDirectoryHash
import com.ampnet.auditornode.controller.websocket.WebSocketSessionHelper.scriptState
import com.ampnet.auditornode.controller.websocket.WebSocketSessionHelper.scriptTask
import com.ampnet.auditornode.model.websocket.InitState
import com.ampnet.auditornode.model.websocket.ReadyState
import com.ampnet.auditornode.model.websocket.WebSocketScriptState
import com.ampnet.auditornode.persistence.model.IpfsHash
import com.ampnet.auditornode.persistence.model.ScriptSource
import com.ampnet.auditornode.script.api.classes.WebSocketInput
import io.micronaut.core.convert.value.MutableConvertibleValues
import io.micronaut.websocket.WebSocketSession
import io.reactivex.disposables.Disposable
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.then
import org.mockito.kotlin.times
import java.util.Optional

class WebSocketSessionHelperUnitTest : TestBase() {

    private val attributes = mock<MutableConvertibleValues<Any>>()
    private val webSocketSession = mock<WebSocketSession> {
        on { attributes } doReturn attributes
    }

    @BeforeEach
    fun beforeEach() {
        reset(attributes)
    }

    @Test
    fun `must correctly store and read 'script' variable`() {
        val script = ScriptSource("test")

        verify("value is correctly stored") {
            webSocketSession.script = script

            then(attributes)
                .should(times(1))
                .put("Script", script)
        }

        verify("value is correctly read") {
            given(attributes["Script", ScriptSource::class.java])
                .willReturn(Optional.of(script))

            assertThat(webSocketSession.script)
                .isEqualTo(script)
        }
    }

    @Test
    fun `must correctly store and read 'scriptInput' variable`() {
        val scriptInput = WebSocketInput(mock())

        verify("value is correctly stored") {
            webSocketSession.scriptInput = scriptInput

            then(attributes)
                .should(times(1))
                .put("ScriptInput", scriptInput)
        }

        verify("value is correctly read") {
            given(attributes["ScriptInput", WebSocketInput::class.java])
                .willReturn(Optional.of(scriptInput))

            assertThat(webSocketSession.scriptInput)
                .isSameAs(scriptInput)
        }
    }

    @Test
    fun `must correctly store and read 'scriptIpfsDirectoryHash' variable`() {
        val scriptIpfsDirectoryHash = IpfsHash("test")

        verify("value is correctly stored") {
            webSocketSession.scriptIpfsDirectoryHash = scriptIpfsDirectoryHash

            then(attributes)
                .should(times(1))
                .put("ScriptDirectoryIpfsHash", scriptIpfsDirectoryHash)
        }

        verify("value is correctly read") {
            given(attributes["ScriptDirectoryIpfsHash", IpfsHash::class.java])
                .willReturn(Optional.of(scriptIpfsDirectoryHash))

            assertThat(webSocketSession.scriptIpfsDirectoryHash)
                .isEqualTo(scriptIpfsDirectoryHash)
        }
    }

    @Test
    fun `must correctly store and read 'scriptTask' variable`() {
        val scriptTask = mock<Disposable>()

        verify("value is correctly stored") {
            webSocketSession.scriptTask = scriptTask

            then(attributes)
                .should(times(1))
                .put("ScriptTask", scriptTask)
        }

        verify("value is correctly read") {
            given(attributes["ScriptTask", Disposable::class.java])
                .willReturn(Optional.of(scriptTask))

            assertThat(webSocketSession.scriptTask)
                .isSameAs(scriptTask)
        }
    }

    @Test
    fun `must correctly store and read 'scriptState' variable`() {
        val scriptState = ReadyState

        verify("value is correctly stored") {
            webSocketSession.scriptState = scriptState

            then(attributes)
                .should(times(1))
                .put("ScriptState", scriptState)
        }

        verify("value is correctly read") {
            given(attributes["ScriptState", WebSocketScriptState::class.java])
                .willReturn(Optional.of(scriptState))

            assertThat(webSocketSession.scriptState)
                .isSameAs(scriptState)
        }
    }

    @Test
    fun `must must return correct default value for 'scriptState' variable when value was not set`() {
        verify("value is correctly read") {
            given(attributes["ScriptState", WebSocketScriptState::class.java])
                .willReturn(Optional.empty())

            assertThat(webSocketSession.scriptState)
                .isSameAs(InitState)
        }
    }
}
