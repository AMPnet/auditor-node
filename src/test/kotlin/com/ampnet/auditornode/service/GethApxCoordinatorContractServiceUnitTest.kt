package com.ampnet.auditornode.service

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import com.ampnet.auditornode.TestBase
import com.ampnet.auditornode.configuration.properties.AuditorProperties
import com.ampnet.auditornode.model.contract.AssetId
import com.ampnet.auditornode.model.contract.UnsignedTransaction
import com.ampnet.auditornode.persistence.model.IpfsHash
import com.ampnet.auditornode.script.api.model.AbortedAudit
import com.ampnet.auditornode.script.api.model.FailedAudit
import com.ampnet.auditornode.script.api.model.SuccessfulAudit
import com.ampnet.auditornode.service.impl.GethApxCoordinatorContractService
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import java.math.BigInteger

class GethApxCoordinatorContractServiceUnitTest : TestBase() {

    private val auditorProperties = mock<AuditorProperties> {
        on { apxCoordinatorContractAddress } doReturn "0xTestContractAddress"
    }
    private val service = GethApxCoordinatorContractService(auditorProperties)

    private val encodedPerformAuditMethodCall = "0a171092"

    private val assetId = AssetId(BigInteger.valueOf(123))
    private val encodedAssetId = "000000000000000000000000000000000000000000000000000000000000007b"

    private val directoryIpfsHash = IpfsHash("test")
    private val encodedDirectoryIpfsHash = "0000000000000000000000000000000000000000000000000000000000000060000000000" +
        "000000000000000000000000000000000000000000000000000000474657374000000000000000000000000000000000000000000000" +
        "00000000000"

    private val encodedTrueBoolean = "0000000000000000000000000000000000000000000000000000000000000001"
    private val encodedFalseBoolean = "0000000000000000000000000000000000000000000000000000000000000000"

    @Test
    fun `must correctly generate transaction for successful audit result`() {
        verify("correct transaction is generated for successful audit result") {
            val transaction = service.generateTxForPerformAudit(
                assetId = assetId,
                auditResult = SuccessfulAudit,
                directoryIpfsHash = directoryIpfsHash
            )

            assertThat(transaction)
                .isNotNull()
                .isEqualTo(
                    UnsignedTransaction(
                        to = auditorProperties.apxCoordinatorContractAddress,
                        data = "0x$encodedPerformAuditMethodCall$encodedAssetId$encodedTrueBoolean" +
                            encodedDirectoryIpfsHash
                    )
                )
        }
    }

    @Test
    fun `must correctly generate transaction for failed audit result`() {
        verify("correct transaction is generated for failed audit result") {
            val transaction = service.generateTxForPerformAudit(
                assetId = assetId,
                auditResult = FailedAudit("failed"),
                directoryIpfsHash = directoryIpfsHash
            )

            assertThat(transaction)
                .isNotNull()
                .isEqualTo(
                    UnsignedTransaction(
                        to = auditorProperties.apxCoordinatorContractAddress,
                        data = "0x$encodedPerformAuditMethodCall$encodedAssetId$encodedFalseBoolean" +
                            encodedDirectoryIpfsHash
                    )
                )
        }
    }

    @Test
    fun `must not generate transaction for aborted audit result`() {
        verify("no transaction is generated for aborted audit result") {
            val transaction = service.generateTxForPerformAudit(
                assetId = assetId,
                auditResult = AbortedAudit("aborted"),
                directoryIpfsHash = directoryIpfsHash
            )

            assertThat(transaction)
                .isNull()
        }
    }
}
