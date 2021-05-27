package com.ampnet.auditornode.service

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import com.ampnet.auditornode.TestBase
import com.ampnet.auditornode.configuration.properties.AuditorProperties
import com.ampnet.auditornode.persistence.model.AssetContractAddress
import com.ampnet.auditornode.persistence.model.UnsignedTransaction
import com.ampnet.auditornode.script.api.model.AbortedAudit
import com.ampnet.auditornode.script.api.model.FailedAudit
import com.ampnet.auditornode.script.api.model.SuccessfulAudit
import com.ampnet.auditornode.service.impl.KethabiAuditRegistryContractTransactionService
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

class KethabiAuditRegistryContractTransactionServiceTest : TestBase() {

    private val auditorProperties = mock<AuditorProperties> {
        on { auditRegistryContractAddress } doReturn "0xTestContractAddress"
    }
    private val service = KethabiAuditRegistryContractTransactionService(auditorProperties)

    private val assetContractAddress = AssetContractAddress("0x1111111111111111111111111111111111111111")
    private val encodedCastVoteMethodCall = "1a419c0c000000000000000000000000"
    private val encodedAssetContractAddress = assetContractAddress.value.removePrefix("0x")
    private val encodedTrueBoolean = "0000000000000000000000000000000000000000000000000000000000000001"
    private val encodedFalseBoolean = "0000000000000000000000000000000000000000000000000000000000000000"

    @Test
    fun `must correctly generate transaction for successful audit result`() {
        verify("correct transaction is generated for successful audit result") {
            val transaction = service.castAuditVoteForAsset(assetContractAddress, SuccessfulAudit)

            assertThat(transaction)
                .isNotNull()
                .isEqualTo(
                    UnsignedTransaction(
                        to = auditorProperties.auditRegistryContractAddress,
                        data = "0x$encodedCastVoteMethodCall$encodedAssetContractAddress$encodedTrueBoolean"
                    )
                )
        }
    }

    @Test
    fun `must correctly generate transaction for failed audit result`() {
        verify("correct transaction is generated for failed audit result") {
            val transaction = service.castAuditVoteForAsset(assetContractAddress, FailedAudit("failed"))

            assertThat(transaction)
                .isNotNull()
                .isEqualTo(
                    UnsignedTransaction(
                        to = auditorProperties.auditRegistryContractAddress,
                        data = "0x$encodedCastVoteMethodCall$encodedAssetContractAddress$encodedFalseBoolean"
                    )
                )
        }
    }

    @Test
    fun `must not generate transaction for aborted audit result`() {
        verify("no transaction is generated for aborted audit result") {
            val transaction = service.castAuditVoteForAsset(assetContractAddress, AbortedAudit("aborted"))

            assertThat(transaction)
                .isNull()
        }
    }
}
