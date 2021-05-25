package com.ampnet.auditornode.controller

import arrow.core.Either
import arrow.core.computations.either
import com.ampnet.auditornode.model.error.ApplicationError
import com.ampnet.auditornode.service.AssetContractService
import com.ampnet.auditornode.service.RegistryContractService
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import mu.KotlinLogging
import org.intellij.lang.annotations.Language
import javax.inject.Inject

private val logger = KotlinLogging.logger {}

@Controller("/audit") // TODO for example only, remove later
class AuditController @Inject constructor(
    private val assetContractService: AssetContractService,
    private val registryContractService: RegistryContractService
) {

    @Get(produces = [MediaType.APPLICATION_JSON])
    suspend fun index(): String {
        val result = either<ApplicationError, String> {
            val assetInfoIpfsHash = assetContractService.getAssetInfoIpfsHash().bind()
            logger.info { "Asset info IPFS hash: $assetInfoIpfsHash" }
            val assetCategoryId = assetContractService.getAssetCategoryId().bind()
            logger.info { "Asset category ID: $assetCategoryId" }
            val auditingProcedureDirectoryIpfsHash = registryContractService
                .getAuditingProcedureDirectoryIpfsHash(assetCategoryId)
                .bind()
            @Language("json") val result = """
                {
                    "assetInfoIpfsHash": "${assetInfoIpfsHash.value}",
                    "assetCategoryId": ${assetCategoryId.value},
                    "auditingProcedureDirectoryIpfsHash": "${auditingProcedureDirectoryIpfsHash.value}"
                }
            """.trimIndent()
            result
        }

        return when (result) {
            is Either.Left -> {
                logger.error(result.value.message, result.value.cause)
                return "{\"errorMessage\":${result.value.message}}"
            }
            is Either.Right -> result.value
        }
    }
}
