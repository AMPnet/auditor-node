package com.ampnet.auditornode.controller.documentation

import com.ampnet.auditornode.model.response.ExecuteScriptResponse
import com.ampnet.auditornode.model.response.StoreScriptResponse
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.Post
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.parameters.RequestBody
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import java.util.UUID

@Tag(name = "Scripts")
interface ScriptControllerDocumentation {

    @Operation(
        summary = "Execute script",
        description = "Executes the provided script source and returns the execution result"
    )
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "Returns script execution result"
        )
    )
    @Post(value = "/execute", produces = [MediaType.APPLICATION_JSON], consumes = [MediaType.TEXT_PLAIN])
    fun executeScript(
        @Body
        @RequestBody(
            description = "Script source code to execute",
            required = true
        ) scriptSource: String
    ): ExecuteScriptResponse

    @Post(value = "/store", produces = [MediaType.APPLICATION_JSON], consumes = [MediaType.TEXT_PLAIN])
    @Operation(
        summary = "Store script source",
        description = "Stores provided script source and returns an UUID used to identify the stored script"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Returns UUID of the stored script"
    )
    fun storeScript(
        @Body
        @RequestBody(
            description = "Script source code to store",
            required = true
        ) scriptSource: String
    ): StoreScriptResponse

    @Get(value = "/load/{scriptId}", produces = [MediaType.TEXT_PLAIN])
    @Operation(
        summary = "Fetch script source",
        description = "Fetches the source of a script with provided UUID"
    )
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "Returns stored script source code"
        ),
        ApiResponse(
            responseCode = "404",
            description = "Returned when script with specified UUID cannot be found"
        )
    )
    fun loadScript(
        @PathVariable("scriptId")
        @Parameter(description = "Script UUID") scriptId: UUID
    ): HttpResponse<String>
}
