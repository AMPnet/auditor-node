package com.ampnet.auditornode.service.impl

import com.ampnet.auditornode.configuration.properties.AuditorProperties
import com.ampnet.auditornode.contract.AuditRegistryContractTransactionGenerator
import com.ampnet.auditornode.persistence.model.AssetContractAddress
import com.ampnet.auditornode.persistence.model.UnsignedTransaction
import com.ampnet.auditornode.script.api.model.AbortedAudit
import com.ampnet.auditornode.script.api.model.AuditResult
import com.ampnet.auditornode.script.api.model.FailedAudit
import com.ampnet.auditornode.script.api.model.SuccessfulAudit
import com.ampnet.auditornode.service.AuditRegistryContractTransactionService
import mu.KotlinLogging
import org.kethereum.model.Address
import org.komputing.khex.extensions.toHexString
import javax.inject.Inject
import javax.inject.Singleton

private val logger = KotlinLogging.logger {}

@Singleton
class KethabiAuditRegistryContractTransactionService @Inject constructor(
    auditorProperties: AuditorProperties
) : AuditRegistryContractTransactionService {

    private val contractAddress = Address(auditorProperties.auditRegistryContractAddress)
    private val contractTransactionGenerator = AuditRegistryContractTransactionGenerator(contractAddress)

    override fun castAuditVoteForAsset(
        assetContractAddress: AssetContractAddress,
        auditResult: AuditResult
    ): UnsignedTransaction? {
        logger.info {
            "Generating transaction for asset with address: $assetContractAddress, audit result: $auditResult"
        }
        val validAudit = when (auditResult) {
            is SuccessfulAudit -> true
            is FailedAudit -> false
            is AbortedAudit -> null
        }

        val kethabiTransaction = validAudit?.let {
            contractTransactionGenerator.castVote(
                asset = Address(assetContractAddress.value),
                valid = it
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
