package com.ampnet.auditornode.service

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import com.ampnet.auditornode.TestBase
import com.ampnet.auditornode.UnitTestUtils.mocks
import com.ampnet.auditornode.UnitTestUtils.web3jMockResponse
import com.ampnet.auditornode.configuration.properties.AuditorProperties
import com.ampnet.auditornode.configuration.properties.RpcProperties
import com.ampnet.auditornode.isLeftContaining
import com.ampnet.auditornode.isRightContaining
import com.ampnet.auditornode.isRightSatisfying
import com.ampnet.auditornode.model.contract.AssetAuditGapDuration
import com.ampnet.auditornode.model.contract.AssetId
import com.ampnet.auditornode.model.contract.Auditor
import com.ampnet.auditornode.model.contract.AuditorPool
import com.ampnet.auditornode.model.contract.AuditorPoolId
import com.ampnet.auditornode.model.contract.ContractAddress
import com.ampnet.auditornode.model.contract.EthereumAddress
import com.ampnet.auditornode.model.contract.UnsignedTransaction
import com.ampnet.auditornode.model.contract.UsdcPerAudit
import com.ampnet.auditornode.model.contract.UsdcPerList
import com.ampnet.auditornode.model.error.RpcError.ContractReadError
import com.ampnet.auditornode.persistence.model.IpfsHash
import com.ampnet.auditornode.script.api.model.AbortedAudit
import com.ampnet.auditornode.script.api.model.FailedAudit
import com.ampnet.auditornode.script.api.model.SuccessfulAudit
import com.ampnet.auditornode.service.impl.Web3jApxCoordinatorContractService
import com.ampnet.auditornode.service.impl.Web3jAssetListHolderContractService
import com.ampnet.auditornode.service.impl.Web3jERC20ContractService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.web3j.protocol.Web3j
import org.web3j.tx.exceptions.ContractCallException
import java.math.BigInteger

class Web3jApxCoordinatorContractServiceUnitTest : TestBase() {

    private val rpcProperties = mock<RpcProperties> {
        on { url } doReturn "http://localhost:8080/test-url"
    }
    private val auditorProperties = mock<AuditorProperties> {
        on { apxCoordinatorContractAddress } doReturn "0xTestContractAddress"
    }
    private val web3j = mock<Web3j>()
    private val service = Web3jApxCoordinatorContractService(web3j, rpcProperties, auditorProperties)

    private val encodedPerformAuditMethodCall = "0a171092"

    private val assetId = AssetId(BigInteger.valueOf(123L))
    private val encodedAssetId = "000000000000000000000000000000000000000000000000000000000000007b"

    private val encodedOffset = "0000000000000000000000000000000000000000000000000000000000000020"
    private val encodedStringOffset1 = "00000000000000000000000000000000000000000000000000000000000000a0"
    private val encodedStringOffset2 = "00000000000000000000000000000000000000000000000000000000000000e0"
    private val encodedArraySize = "0000000000000000000000000000000000000000000000000000000000000002"
    private val encodedAuditorPoolFirstElementOffset =
        "0000000000000000000000000000000000000000000000000000000000000040"
    private val encodedAuditorPoolSecondElementOffset =
        "0000000000000000000000000000000000000000000000000000000000000160"
    private val encodedAuditorPoolMembersFirstElementOffset =
        "0000000000000000000000000000000000000000000000000000000000000040"
    private val encodedAuditorPoolMembersSecondElementOffset =
        "0000000000000000000000000000000000000000000000000000000000000100"
    private val encodedStringLength = "0000000000000000000000000000000000000000000000000000000000000080"

    private val testContractAddress = ContractAddress("0x0000000000000000000000000000000000000001")
    private val encodedTestContractAddress = "0x0000000000000000000000000000000000000000000000000000000000000001"

    private val assetAuditGapDuration = AssetAuditGapDuration(BigInteger.valueOf(123L))
    private val encodedAssetAuditGapDuration = "000000000000000000000000000000000000000000000000000000000000007b"

    private val usdcPerAudit = UsdcPerAudit(BigInteger.valueOf(123L))
    private val encodedUsdcPerAudit = "000000000000000000000000000000000000000000000000000000000000007b"

    private val usdcPerList = UsdcPerList(BigInteger.valueOf(123L))
    private val encodedUsdcPerList = "000000000000000000000000000000000000000000000000000000000000007b"

    private val auditorPoolId1 = AuditorPoolId(BigInteger.valueOf(123L))
    private val encodedAuditorPoolId1 = "000000000000000000000000000000000000000000000000000000000000007b"

    private val auditorPoolId2 = AuditorPoolId(BigInteger.valueOf(456L))
    private val encodedAuditorPoolId2 = "00000000000000000000000000000000000000000000000000000000000001c8"

    private val auditorPoolName1 = "a"
    private val encodedAuditorPoolName1 =
        "000000000000000000000000000000000000000000000000000000000000000161000000000000000000000000000000000000000000" +
            "00000000000000000000"

    private val auditorPoolInfoIpfsHash1 = IpfsHash("b")
    private val encodedAuditorPoolInfoIpfsHash1 =
        "000000000000000000000000000000000000000000000000000000000000000162000000000000000000000000000000000000000000" +
            "00000000000000000000"

    private val auditorPoolActiveMembers1 = BigInteger.valueOf(456L)
    private val encodedAuditorPoolActiveMembers1 =
        "00000000000000000000000000000000000000000000000000000000000001c8"

    private val auditorPoolName2 = "c"
    private val encodedAuditorPoolName2 =
        "000000000000000000000000000000000000000000000000000000000000000163000000000000000000000000000000000000000000" +
            "00000000000000000000"

    private val auditorPoolInfoIpfsHash2 = IpfsHash("d")
    private val encodedAuditorPoolInfoIpfsHash2 =
        "000000000000000000000000000000000000000000000000000000000000000164000000000000000000000000000000000000000000" +
            "00000000000000000000"

    private val auditorPoolActiveMembers2 = BigInteger.valueOf(789L)
    private val encodedAuditorPoolActiveMembers2 =
        "0000000000000000000000000000000000000000000000000000000000000315"

    private val auditorAddress1 = EthereumAddress("0x0000000000000000000000000000000000000001")
    private val encodedAuditorAddress1 = "0000000000000000000000000000000000000000000000000000000000000001"

    private val totalAuditsPerformed1 = BigInteger.valueOf(123L)
    private val encodedTotalAuditsPerformed1 = "000000000000000000000000000000000000000000000000000000000000007b"

    private val totalListingsPerformed1 = BigInteger.valueOf(456L)
    private val encodedTotalListingsPerformed1 = "00000000000000000000000000000000000000000000000000000000000001c8"

    private val auditorInfoIpfsHash1 = IpfsHash("a")
    private val encodedAuditorInfoIpfsHash1 =
        "000000000000000000000000000000000000000000000000000000000000000161000000000000000000000000000000000000000000" +
            "00000000000000000000"

    private val auditorAddress2 = EthereumAddress("0x0000000000000000000000000000000000000002")
    private val encodedAuditorAddress2 = "0000000000000000000000000000000000000000000000000000000000000002"

    private val totalAuditsPerformed2 = BigInteger.valueOf(124L)
    private val encodedTotalAuditsPerformed2 = "000000000000000000000000000000000000000000000000000000000000007c"

    private val totalListingsPerformed2 = BigInteger.valueOf(457L)
    private val encodedTotalListingsPerformed2 = "00000000000000000000000000000000000000000000000000000000000001c9"

    private val auditorInfoIpfsHash2 = IpfsHash("b")
    private val encodedAuditorInfoIpfsHash2 =
        "000000000000000000000000000000000000000000000000000000000000000162000000000000000000000000000000000000000000" +
            "00000000000000000000"

    private val directoryIpfsHash = IpfsHash("test")
    private val encodedDirectoryIpfsHash = "0000000000000000000000000000000000000000000000000000000000000060000000000" +
        "000000000000000000000000000000000000000000000000000000474657374000000000000000000000000000000000000000000000" +
        "00000000000"

    private val encodedTrueBoolean = "0000000000000000000000000000000000000000000000000000000000000001"
    private val encodedFalseBoolean = "0".repeat(64)

    @BeforeEach
    fun beforeEach() {
        reset(web3j)
    }

    @Test
    fun `must correctly return stable coin contract`() {
        suppose("Web3j client will return some stable coin contract address") {
            val response = web3jMockResponse(encodedTestContractAddress)
            web3j.mocks()
                .willReturn(response)
        }

        verify("correct stable coin contract is returned") {
            val result = service.getStableCoinContract()
            assertThat(result)
                .isRightSatisfying {
                    assertThat(it).isInstanceOf(Web3jERC20ContractService::class)
                    assertThat(it.contractAddress).isEqualTo(testContractAddress)
                }
        }
    }

    @Test
    fun `must correctly return stable coin contract when first fetch fails`() {
        val localService = Web3jApxCoordinatorContractService(web3j, rpcProperties, auditorProperties)

        suppose("Web3j client will throw an exception") {
            web3j.mocks()
                .willThrow(ContractCallException("contract call error"))
        }

        verify("error is returned when fetching stable coin contract address") {
            val result = localService.getStableCoinContract()
            assertThat(result)
                .isLeftContaining(
                    ContractReadError(
                        "Could not fetch stable coin contract address from APX coordinator contract"
                    )
                )
        }

        suppose("Web3j client will return some stable coin contract address") {
            val response = web3jMockResponse(encodedTestContractAddress)
            given(web3j.ethCall(any(), any()))
                .willReturn(response)
        }

        verify("correct stable coin contract is returned") {
            val result = localService.getStableCoinContract()
            assertThat(result)
                .isRightSatisfying {
                    assertThat(it).isInstanceOf(Web3jERC20ContractService::class)
                    assertThat(it.contractAddress).isEqualTo(testContractAddress)
                }
        }
    }

    @Test
    fun `must correctly return asset list holder contract`() {
        suppose("Web3j client will return some asset list holder contract address") {
            val response = web3jMockResponse(encodedTestContractAddress)
            web3j.mocks()
                .willReturn(response)
        }

        verify("correct asset list holder contract is returned") {
            val result = service.getAssetListHolderContract()
            assertThat(result)
                .isRightSatisfying {
                    assertThat(it).isInstanceOf(Web3jAssetListHolderContractService::class)
                    assertThat(it.contractAddress).isEqualTo(testContractAddress)
                }
        }
    }

    @Test
    fun `must correctly return asset list holder contract when first fetch fails`() {
        val localService = Web3jApxCoordinatorContractService(web3j, rpcProperties, auditorProperties)

        suppose("Web3j client will throw an exception") {
            web3j.mocks()
                .willThrow(ContractCallException("contract call error"))
        }

        verify("error is returned when fetching asset list holder contract address") {
            val result = localService.getAssetListHolderContract()
            assertThat(result)
                .isLeftContaining(
                    ContractReadError(
                        "Could not fetch asset list holder contract address from APX coordinator contract"
                    )
                )
        }

        suppose("Web3j client will return some asset list holder contract address") {
            val response = web3jMockResponse(encodedTestContractAddress)
            given(web3j.ethCall(any(), any()))
                .willReturn(response)
        }

        verify("correct asset list holder contract is returned") {
            val result = localService.getAssetListHolderContract()
            assertThat(result)
                .isRightSatisfying {
                    assertThat(it).isInstanceOf(Web3jAssetListHolderContractService::class)
                    assertThat(it.contractAddress).isEqualTo(testContractAddress)
                }
        }
    }

    @Test
    fun `must correctly return asset audit gap duration`() {
        suppose("Web3j client will return some audit gap duration") {
            val response = web3jMockResponse(encodedAssetAuditGapDuration)
            web3j.mocks()
                .willReturn(response)
        }

        verify("correct audit gap duration is returned") {
            val result = service.getAssetAuditGapDuration()
            assertThat(result)
                .isRightContaining(assetAuditGapDuration)
        }
    }

    @Test
    fun `must correctly return USDC per audit`() {
        suppose("Web3j client will return some USDC per audit") {
            val response = web3jMockResponse(encodedUsdcPerAudit)
            web3j.mocks()
                .willReturn(response)
        }

        verify("correct USDC per audit is returned") {
            val result = service.getUsdcPerAudit()
            assertThat(result)
                .isRightContaining(usdcPerAudit)
        }
    }

    @Test
    fun `must correctly return USDC per list`() {
        suppose("Web3j client will return some USDC per list") {
            val response = web3jMockResponse(encodedUsdcPerList)
            web3j.mocks()
                .willReturn(response)
        }

        verify("correct USDC per list is returned") {
            val result = service.getUsdcPerList()
            assertThat(result)
                .isRightContaining(usdcPerList)
        }
    }

    @Test
    fun `must correctly return auditor pool memberships`() {
        suppose("Web3j client will return some auditor pool memberships") {
            val response = web3jMockResponse(
                "$encodedOffset$encodedArraySize$encodedAuditorPoolId1$encodedAuditorPoolId2"
            )
            web3j.mocks()
                .willReturn(response)
        }

        verify("correct auditor pool memberships are returned") {
            val result = service.getAuditorPoolMemberships(testContractAddress.asEthereumAddress())
            assertThat(result)
                .isRightContaining(
                    listOf(
                        auditorPoolId1,
                        auditorPoolId2
                    )
                )
        }
    }

    @Test
    fun `must correctly return auditor pools`() {
        suppose("Web3j client will return some auditor pools") {
            val response = web3jMockResponse(
                "0x$encodedOffset$encodedArraySize$encodedAuditorPoolFirstElementOffset" +
                    encodedAuditorPoolSecondElementOffset +
                    "$encodedAuditorPoolId1$encodedStringOffset1$encodedStringOffset2$encodedTrueBoolean" +
                    "$encodedAuditorPoolActiveMembers1$encodedAuditorPoolName1$encodedAuditorPoolInfoIpfsHash1" +
                    "$encodedAuditorPoolId2$encodedStringOffset1$encodedStringOffset2$encodedFalseBoolean" +
                    "$encodedAuditorPoolActiveMembers2$encodedAuditorPoolName2$encodedAuditorPoolInfoIpfsHash2"
            )
            web3j.mocks()
                .willReturn(response)
        }

        verify("correct auditor pools are returned") {
            val result = service.getAuditorPools()
            assertThat(result)
                .isRightContaining(
                    listOf(
                        AuditorPool(
                            id = auditorPoolId1,
                            name = auditorPoolName1,
                            info = auditorPoolInfoIpfsHash1,
                            active = true,
                            activeMembers = auditorPoolActiveMembers1
                        ),
                        AuditorPool(
                            id = auditorPoolId2,
                            name = auditorPoolName2,
                            info = auditorPoolInfoIpfsHash2,
                            active = false,
                            activeMembers = auditorPoolActiveMembers2
                        )
                    )
                )
        }
    }

    @Test
    fun `must correctly return auditor pool by ID`() {
        suppose("Web3j client will return some auditor pool by ID") {
            val response = web3jMockResponse(
                "0x$encodedOffset$encodedAuditorPoolId1$encodedStringOffset1$encodedStringOffset2$encodedTrueBoolean" +
                    "$encodedAuditorPoolActiveMembers1$encodedAuditorPoolName1$encodedAuditorPoolInfoIpfsHash1"
            )
            web3j.mocks()
                .willReturn(response)
        }

        verify("correct auditor pool by ID is returned") {
            val result = service.getAuditorPoolById(AuditorPoolId(BigInteger.valueOf(123L)))
            assertThat(result)
                .isRightContaining(
                    AuditorPool(
                        id = auditorPoolId1,
                        name = auditorPoolName1,
                        info = auditorPoolInfoIpfsHash1,
                        active = true,
                        activeMembers = auditorPoolActiveMembers1
                    )
                )
        }
    }

    @Test
    fun `must correctly return auditor pool members`() {
        suppose("Web3j client will return some auditor pool members") {
            val response = web3jMockResponse(
                "0x$encodedOffset$encodedArraySize$encodedAuditorPoolMembersFirstElementOffset" +
                    encodedAuditorPoolMembersSecondElementOffset +
                    "$encodedAuditorAddress1$encodedTotalAuditsPerformed1$encodedTotalListingsPerformed1" +
                    "$encodedStringLength$encodedAuditorInfoIpfsHash1" +
                    "$encodedAuditorAddress2$encodedTotalAuditsPerformed2$encodedTotalListingsPerformed2" +
                    "$encodedStringLength$encodedAuditorInfoIpfsHash2"
            )
            web3j.mocks()
                .willReturn(response)
        }

        verify("correct auditor pool members are returned") {
            val result = service.getAuditorPoolMembers(AuditorPoolId(BigInteger.valueOf(123L)))
            assertThat(result)
                .isRightContaining(
                    listOf(
                        Auditor(
                            address = auditorAddress1,
                            totalAuditsPerformed = totalAuditsPerformed1,
                            totalListingsPerformed = totalListingsPerformed1,
                            info = auditorInfoIpfsHash1
                        ),
                        Auditor(
                            address = auditorAddress2,
                            totalAuditsPerformed = totalAuditsPerformed2,
                            totalListingsPerformed = totalListingsPerformed2,
                            info = auditorInfoIpfsHash2
                        )
                    )
                )
        }
    }

    @Test
    fun `must correctly generate transaction for successful audit result`() {
        suppose("Web3j client is reachable") {
            web3j.mocks()
        }

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
        suppose("Web3j client is reachable") {
            web3j.mocks()
        }

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
        suppose("Web3j client is reachable") {
            web3j.mocks()
        }

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
