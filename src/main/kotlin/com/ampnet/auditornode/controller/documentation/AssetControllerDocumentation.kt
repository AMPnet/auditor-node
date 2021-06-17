package com.ampnet.auditornode.controller.documentation

import com.ampnet.auditornode.model.response.AssetListResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Get
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag

@Tag(name = "Assets")
interface AssetControllerDocumentation {

    @Get(value = "/list", produces = [MediaType.APPLICATION_JSON])
    @Operation(
        summary = "List auditable assets",
        description = "Fetches a list of auditable assets for the provided wallet address"
    )
    @ApiResponse(responseCode = "200", description = "Returns a list of auditable assets")
    fun listAssets(): AssetListResponse
}
