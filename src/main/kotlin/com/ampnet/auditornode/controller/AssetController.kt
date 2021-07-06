package com.ampnet.auditornode.controller

import com.ampnet.auditornode.controller.documentation.AssetControllerDocumentation
import com.ampnet.auditornode.model.response.AssetListResponse
import com.ampnet.auditornode.model.response.AssetResponse
import io.micronaut.http.annotation.Controller
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

@Controller("/assets") // TODO write tests after implementation is done
class AssetController : AssetControllerDocumentation {

    override fun listAssets(): AssetListResponse { // TODO write actual implementation
        logger.info { "Listing assets" }
        return AssetListResponse(
            listOf(
                AssetResponse(
                    name = "Example Asset",
                    contractAddress = "0x19837CF4ed595794eB26A0F60F76c2efd13b097B" // TODO hard-coded address
                )
            )
        )
    }
}
