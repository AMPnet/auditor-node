package com.ampnet.auditornode.controller

import arrow.core.Either
import arrow.core.computations.either
import com.ampnet.auditornode.model.error.ApplicationError
import com.ampnet.auditornode.persistence.repository.IpfsRepository
import com.ampnet.auditornode.service.AuditingService
import com.ampnet.auditornode.service.ContractService
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import org.slf4j.LoggerFactory
import javax.inject.Inject

@Controller("/audit") // TODO for example only, remove later
class AuditController @Inject constructor(
    private val contractService: ContractService,
    private val ipfsClientService: IpfsRepository,
    private val auditingService: AuditingService
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @Get(produces = [MediaType.TEXT_PLAIN])
    suspend fun index(): String {
        val result = either<ApplicationError, String> {
            val currentBlockNumber = contractService.currentBlockNumber().bind()
            log.info("Current block number: {}", currentBlockNumber)
            val ipfsFileHash = contractService.getIpfsFileHash().bind()
            log.info("Input IPFS hash: {}", ipfsFileHash)
            val ipfsFile = ipfsClientService.fetchTextFile(ipfsFileHash).bind()
            log.info("Got file from IPFS: {}", ipfsFile)
            val evaluationResult = auditingService.evaluate(ipfsFile.content).bind()
            log.info("Evaluation result: {}", evaluationResult)
            val transaction = contractService.storeIpfsFileHash(ipfsFileHash)
            transaction.toJson()
        }

        if (result is Either.Left) {
            log.error(result.value.message, result.value.cause)
        }

        return result.toString()
    }
}
