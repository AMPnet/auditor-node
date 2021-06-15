package com.ampnet.auditornode.controller

import com.ampnet.auditornode.configuration.properties.AuditorProperties
import com.ampnet.auditornode.model.response.AssetListResponse
import com.ampnet.auditornode.model.response.AssetResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import javax.inject.Inject

@Controller("/assets") // TODO write tests after implementation is done
class AssetController @Inject constructor(
    private val auditorProperties: AuditorProperties
) {

    @Get(value = "/list", produces = [MediaType.APPLICATION_JSON])
    fun listAssets(): AssetListResponse { // TODO write actual implementation
        return AssetListResponse(
            listOf(
                AssetResponse(
                    name = "Example Asset",
                    contractAddress = auditorProperties.assetContractAddress
                )
            )
        )
    }
}
