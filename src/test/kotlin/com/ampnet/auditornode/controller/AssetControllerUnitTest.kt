package com.ampnet.auditornode.controller

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.ampnet.auditornode.TestBase
import com.ampnet.auditornode.configuration.properties.AuditorProperties
import com.ampnet.auditornode.model.response.AssetListResponse
import com.ampnet.auditornode.model.response.AssetResponse
import org.junit.jupiter.api.Test
import org.mockito.kotlin.given
import org.mockito.kotlin.mock

class AssetControllerUnitTest : TestBase() {

    private val properties = mock<AuditorProperties>()
    private val controller = AssetController(properties)

    @Test // TODO re-implement after actual implementation of AssetController
    fun `must return correct dummy asset list`() {
        val contractAddress = "test"

        suppose("asset contract address property is set") {
            given(properties.assetContractAddress)
                .willReturn(contractAddress)
        }

        verify("correct dummy assets are returned") {
            assertThat(controller.listAssets())
                .isEqualTo(
                    AssetListResponse(
                        listOf(
                            AssetResponse(
                                name = "Example Asset",
                                contractAddress = contractAddress
                            )
                        )
                    )
                )
        }
    }
}
