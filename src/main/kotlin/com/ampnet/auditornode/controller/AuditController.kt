package com.ampnet.auditornode.controller

import arrow.core.Either
import arrow.core.computations.either
import com.ampnet.auditornode.model.error.ApplicationError
import com.ampnet.auditornode.persistence.repository.IpfsRepository
import com.ampnet.auditornode.script.api.ExecutionContext
import com.ampnet.auditornode.script.api.classes.DirectoryBasedIpfs
import com.ampnet.auditornode.service.AuditingService
import com.ampnet.auditornode.service.ContractService
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import mu.KotlinLogging
import javax.inject.Inject

private val logger = KotlinLogging.logger {}

@Controller("/audit") // TODO for example only, remove later
class AuditController @Inject constructor(
    private val contractService: ContractService,
    private val ipfsRepository: IpfsRepository,
    private val auditingService: AuditingService
) {

    @Get(produces = [MediaType.TEXT_PLAIN])
    suspend fun index(): String {
        val result = either<ApplicationError, String> {
            val currentBlockNumber = contractService.currentBlockNumber().bind()
            logger.info { "Current block number: $currentBlockNumber" }
            val ipfsDirectoryHash = contractService.getIpfsDirectoryHash().bind()
            logger.info { "Input IPFS hash: $ipfsDirectoryHash" }
            val ipfsFile = ipfsRepository.fetchTextFileFromDirectory(ipfsDirectoryHash, "audit.js").bind()
            logger.info { "Got file from IPFS: $ipfsFile" }
            val executionContext = ExecutionContext.noOp.copy(
                ipfs = DirectoryBasedIpfs(ipfsDirectoryHash, ipfsRepository)
            )
            val evaluationResult = auditingService.evaluate(ipfsFile.content, executionContext).bind()
            logger.info { "Evaluation result: $evaluationResult" }
            val transaction = contractService.storeIpfsDirectoryHash(ipfsDirectoryHash)
            transaction.toJson()
        }

        if (result is Either.Left) {
            logger.error(result.value.message, result.value.cause)
        }

        return result.toString()
    }
}
