package com.ampnet.auditornode.script.api.objects

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.ampnet.auditornode.TestBase
import com.ampnet.auditornode.script.api.model.AbortedAudit
import com.ampnet.auditornode.script.api.model.FailedAudit
import com.ampnet.auditornode.script.api.model.SuccessfulAudit
import org.junit.jupiter.api.Test

class AuditResultApiUnitTest : TestBase() {

    @Test
    fun `must correctly construct successful audit result`() {
        verify("correct object is constructed") {
            assertThat(AuditResultApi.success())
                .isEqualTo(SuccessfulAudit)
        }
    }

    @Test
    fun `must correctly construct failed audit result`() {
        verify("correct object is constructed") {
            val message = "test message"
            assertThat(AuditResultApi.failure(message))
                .isEqualTo(FailedAudit(message))
        }
    }

    @Test
    fun `must correctly construct successful aborted result`() {
        verify("correct object is constructed") {
            val message = "test message"
            assertThat(AuditResultApi.aborted(message))
                .isEqualTo(AbortedAudit(message))
        }
    }
}
