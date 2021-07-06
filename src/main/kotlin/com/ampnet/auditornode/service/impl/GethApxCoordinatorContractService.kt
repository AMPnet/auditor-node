package com.ampnet.auditornode.service.impl

import com.ampnet.auditornode.configuration.properties.AuditorProperties
import com.ampnet.auditornode.contract.coordinator.ApxCoordinatorContractTransactionGenerator
import com.ampnet.auditornode.model.contract.AssetId
import com.ampnet.auditornode.model.contract.UnsignedTransaction
import com.ampnet.auditornode.persistence.model.IpfsHash
import com.ampnet.auditornode.script.api.model.AbortedAudit
import com.ampnet.auditornode.script.api.model.AuditResult
import com.ampnet.auditornode.script.api.model.FailedAudit
import com.ampnet.auditornode.script.api.model.SuccessfulAudit
import com.ampnet.auditornode.service.ApxCoordinatorContractService
import mu.KotlinLogging
import org.kethereum.model.Address
import org.komputing.khex.extensions.toHexString
import javax.inject.Inject
import javax.inject.Singleton

private val logger = KotlinLogging.logger {}

@Singleton
class GethApxCoordinatorContractService @Inject constructor(
    auditorProperties: AuditorProperties
) : ApxCoordinatorContractService {

    private val contractAddress = Address(auditorProperties.apxCoordinatorContractAddress)
    private val contractTransactionGenerator = ApxCoordinatorContractTransactionGenerator(contractAddress)

    override fun generateTxForPerformAudit(
        assetId: AssetId,
        auditResult: AuditResult,
        directoryIpfsHash: IpfsHash
    ): UnsignedTransaction? {
        logger.info {
            "Generating transaction for asset with ID: $assetId, audit result: $auditResult, " +
                "directory IPFS hash: $directoryIpfsHash"
        }

        val validAudit = when (auditResult) {
            is SuccessfulAudit -> true
            is FailedAudit -> false
            is AbortedAudit -> null
        }

        val kethabiTransaction = validAudit?.let {
            contractTransactionGenerator.performAudit(
                assetId = assetId.value,
                assetValid = validAudit,
                additionalInfo = directoryIpfsHash.value
            )
        }

        return kethabiTransaction?.let {
            UnsignedTransaction(
                to = contractAddress.hex,
                data = it.input.toHexString()
            )
        }
    }
}
