package com.ampnet.auditornode.script.api.classes

import assertk.assertThat
import assertk.assertions.isNull
import com.ampnet.auditornode.TestBase
import org.junit.jupiter.api.Test

class NoOpIpfsUnitTest : TestBase() {

    @Test
    fun `must return null for getFile() call`() {
        verify("null is returned") {
            assertThat(NoOpIpfs.getFile(""))
                .isNull()
        }
    }
}
