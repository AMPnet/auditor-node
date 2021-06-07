package com.ampnet.auditornode.script.api.classes

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import com.ampnet.auditornode.TestBase
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock

class NoOpInputUnitTest : TestBase() {

    @Test
    fun `must return null for readBoolean() call`() {
        verify("null is returned") {
            assertThat(NoOpInput.readBoolean(""))
                .isNull()
        }
    }

    @Test
    fun `must return null for readNumber() call`() {
        verify("null is returned") {
            assertThat(NoOpInput.readNumber(""))
                .isNull()
        }
    }

    @Test
    fun `must return null for readString() call`() {
        verify("null is returned") {
            assertThat(NoOpInput.readString(""))
                .isNull()
        }
    }

    @Test
    fun `must return null for readFields() call`() {
        verify("null is returned") {
            assertThat(NoOpInput.readFields(mock(), ""))
                .isNull()
        }
    }

    @Test
    fun `must return not throw exception for button() call`() {
        verify("call is successful") {
            assertThat(NoOpInput.button(""))
                .isEqualTo(Unit)
        }
    }
}
