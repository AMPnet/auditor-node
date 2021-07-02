package com.ampnet.auditornode.service

import assertk.assertThat
import com.ampnet.auditornode.TestBase
import com.ampnet.auditornode.UnitTestUtils.mocks
import com.ampnet.auditornode.UnitTestUtils.web3jMockResponse
import com.ampnet.auditornode.configuration.properties.RpcProperties
import com.ampnet.auditornode.isRightContaining
import com.ampnet.auditornode.model.contract.AssetDescriptor
import com.ampnet.auditornode.model.contract.AssetId
import com.ampnet.auditornode.model.contract.AssetTypeId
import com.ampnet.auditornode.model.contract.ContractAddress
import com.ampnet.auditornode.service.impl.Web3jAssetListHolderContractService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.web3j.protocol.Web3j
import java.math.BigInteger

class Web3jAssetListHolderContractServiceUnitTest : TestBase() {

    private val contractAddress = ContractAddress("0xTestContractAddress")
    private val rpcProperties = mock<RpcProperties> {
        on { url } doReturn "http://localhost:8080/test-url"
    }
    private val web3j = mock<Web3j>()
    private val service = Web3jAssetListHolderContractService(web3j, rpcProperties, contractAddress)

    private val encodedOffset = "0000000000000000000000000000000000000000000000000000000000000020"

    private val encodedArraySize = "0000000000000000000000000000000000000000000000000000000000000002"
    private val encodedFirstElementOffset = "0000000000000000000000000000000000000000000000000000000000000040"
    private val encodedSecondElementOffset = "0000000000000000000000000000000000000000000000000000000000000180"

    private val assetHolderAddress1 = ContractAddress("0x0000000000000000000000000000000000000001")
    private val tokenizedAssetAddress1 = ContractAddress("0x0000000000000000000000000000000000000002")
    private val assetId1 = AssetId(BigInteger.valueOf(3L))
    private val assetTypeId1 = AssetTypeId(BigInteger.valueOf(4L))
    private val assetName1 = "a"
    private val assetTicker1 = "b"

    private val encodedAssetHolderAddress1 = "0000000000000000000000000000000000000000000000000000000000000001"
    private val encodedTokenizedAssetAddress1 = "0000000000000000000000000000000000000000000000000000000000000002"
    private val encodedAssetId1 = "0000000000000000000000000000000000000000000000000000000000000003"
    private val encodedAssetTypeId1 = "0000000000000000000000000000000000000000000000000000000000000004"
    private val encodedAssetNameOffset1 = "00000000000000000000000000000000000000000000000000000000000000c0"
    private val encodedAssetTickerOffset1 = "0000000000000000000000000000000000000000000000000000000000000100"
    private val encodedAssetName1 =
        "000000000000000000000000000000000000000000000000000000000000000161000000000000000000000000000000000000000000" +
            "00000000000000000000"
    private val encodedAssetTicker1 =
        "000000000000000000000000000000000000000000000000000000000000000162000000000000000000000000000000000000000000" +
            "00000000000000000000"

    private val assetHolderAddress2 = ContractAddress("0x0000000000000000000000000000000000000005")
    private val tokenizedAssetAddress2 = ContractAddress("0x0000000000000000000000000000000000000006")
    private val assetId2 = AssetId(BigInteger.valueOf(7L))
    private val assetTypeId2 = AssetTypeId(BigInteger.valueOf(8L))
    private val assetName2 = "c"
    private val assetTicker2 = "d"

    private val encodedAssetHolderAddress2 = "0000000000000000000000000000000000000000000000000000000000000005"
    private val encodedTokenizedAssetAddress2 = "0000000000000000000000000000000000000000000000000000000000000006"
    private val encodedAssetId2 = "0000000000000000000000000000000000000000000000000000000000000007"
    private val encodedAssetTypeId2 = "0000000000000000000000000000000000000000000000000000000000000008"
    private val encodedAssetNameOffset2 = "00000000000000000000000000000000000000000000000000000000000000c0"
    private val encodedAssetTickerOffset2 = "0000000000000000000000000000000000000000000000000000000000000100"
    private val encodedAssetName2 =
        "000000000000000000000000000000000000000000000000000000000000000163000000000000000000000000000000000000000000" +
            "00000000000000000000"
    private val encodedAssetTicker2 =
        "000000000000000000000000000000000000000000000000000000000000000164000000000000000000000000000000000000000000" +
            "00000000000000000000"

    @BeforeEach
    fun beforeEach() {
        reset(web3j)
    }

    @Test
    fun `must correctly return asset descriptors`() {
        suppose("Web3j client will return some asset descriptors") {
            val response = web3jMockResponse(
                "0x$encodedOffset$encodedArraySize$encodedFirstElementOffset$encodedSecondElementOffset" +
                    "$encodedAssetHolderAddress1$encodedTokenizedAssetAddress1$encodedAssetId1$encodedAssetTypeId1" +
                    "$encodedAssetNameOffset1$encodedAssetTickerOffset1$encodedAssetName1$encodedAssetTicker1" +
                    "$encodedAssetHolderAddress2$encodedTokenizedAssetAddress2$encodedAssetId2$encodedAssetTypeId2" +
                    "$encodedAssetNameOffset2$encodedAssetTickerOffset2$encodedAssetName2$encodedAssetTicker2"
            )
            web3j.mocks()
                .willReturn(response)
        }

        verify("correct asset category ID is returned") {
            val result = service.getAssets()
            assertThat(result)
                .isRightContaining(
                    listOf(
                        AssetDescriptor(
                            assetHolder = assetHolderAddress1,
                            tokenizedAsset = tokenizedAssetAddress1,
                            id = assetId1,
                            typeId = assetTypeId1,
                            name = assetName1,
                            ticker = assetTicker1
                        ),
                        AssetDescriptor(
                            assetHolder = assetHolderAddress2,
                            tokenizedAsset = tokenizedAssetAddress2,
                            id = assetId2,
                            typeId = assetTypeId2,
                            name = assetName2,
                            ticker = assetTicker2
                        )
                    )
                )
        }
    }

    @Test
    fun `must correctly return asset descriptor by ID`() {
        suppose("Web3j client will return some asset descriptor by ID") {
            val response = web3jMockResponse(
                "0x$encodedOffset$encodedAssetHolderAddress1$encodedTokenizedAssetAddress1$encodedAssetId1" +
                    "$encodedAssetTypeId1$encodedAssetNameOffset1$encodedAssetTickerOffset1$encodedAssetName1" +
                    encodedAssetTicker1
            )
            web3j.mocks()
                .willReturn(response)
        }

        verify("correct asset category ID is returned") {
            val result = service.getAssetById(assetId1)
            assertThat(result)
                .isRightContaining(
                    AssetDescriptor(
                        assetHolder = assetHolderAddress1,
                        tokenizedAsset = tokenizedAssetAddress1,
                        id = assetId1,
                        typeId = assetTypeId1,
                        name = assetName1,
                        ticker = assetTicker1
                    )
                )
        }
    }
}
