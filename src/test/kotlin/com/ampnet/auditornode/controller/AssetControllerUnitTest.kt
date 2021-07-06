package com.ampnet.auditornode.controller

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.ampnet.auditornode.TestBase
import com.ampnet.auditornode.model.response.AssetListResponse
import com.ampnet.auditornode.model.response.AssetResponse
import org.junit.jupiter.api.Test

class AssetControllerUnitTest : TestBase() {

    private val controller = AssetController()

    @Test // TODO re-implement after actual implementation of AssetController
    fun `must return correct dummy asset list`() {
        verify("correct dummy assets are returned") {
            assertThat(controller.listAssets())
                .isEqualTo(
                    AssetListResponse(
                        listOf(
                            AssetResponse(
                                name = "Example Asset",
                                contractAddress = "0x19837CF4ed595794eB26A0F60F76c2efd13b097B"
                            )
                        )
                    )
                )
        }
    }
}
