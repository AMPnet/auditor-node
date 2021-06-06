package com.ampnet.auditornode.script.api.classes

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.ampnet.auditornode.TestBase
import org.junit.jupiter.api.Test

class NoOpOutputUnitTest : TestBase() {

    @Test
    fun `must not throw exception for renderText() call`() {
        verify("call is successful") {
            assertThat(NoOpOutput.renderText(""))
                .isEqualTo(Unit)
        }
    }

    @Test
    fun `must not throw exception for renderHtml() call`() {
        verify("call is successful") {
            assertThat(NoOpOutput.renderHtml(""))
                .isEqualTo(Unit)
        }
    }

    @Test
    fun `must not throw exception for renderMarkdown() call`() {
        verify("call is successful") {
            assertThat(NoOpOutput.renderMarkdown(""))
                .isEqualTo(Unit)
        }
    }
}
