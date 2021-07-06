package com.ampnet.auditornode.service.impl

import com.ampnet.auditornode.configuration.properties.AuditorProperties
import com.ampnet.auditornode.contract.ApxCoordinator
import com.ampnet.auditornode.model.contract.AssetId
import com.ampnet.auditornode.model.contract.ContractAddress
import com.ampnet.auditornode.model.contract.UnsignedTransaction
import com.ampnet.auditornode.persistence.model.IpfsHash
import com.ampnet.auditornode.script.api.model.AbortedAudit
import com.ampnet.auditornode.script.api.model.AuditResult
import com.ampnet.auditornode.script.api.model.FailedAudit
import com.ampnet.auditornode.script.api.model.SuccessfulAudit
import com.ampnet.auditornode.service.ApxCoordinatorContractService
import mu.KotlinLogging
import org.web3j.protocol.Web3j
import org.web3j.tx.ReadonlyTransactionManager
import org.web3j.tx.gas.DefaultGasProvider
import javax.inject.Inject
import javax.inject.Singleton

private val logger = KotlinLogging.logger {}

@Singleton
class Web3jApxCoordinatorContractService @Inject constructor(
    web3j: Web3j,
    private val auditorProperties: AuditorProperties
) : ApxCoordinatorContractService {

    private class Contract(contractAddress: ContractAddress, web3j: Web3j) : ApxCoordinator(
        contractAddress.value,
        web3j,
        ReadonlyTransactionManager(web3j, contractAddress.value),
        DefaultGasProvider()
    )

    private val contract by lazy {
        Contract(ContractAddress(auditorProperties.apxCoordinatorContractAddress), web3j)
    }

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

        val functionCall = validAudit?.let { contract.performAudit(assetId.value, validAudit, directoryIpfsHash.value) }
            ?.encodeFunctionCall()

        return functionCall?.let {
            UnsignedTransaction(
                to = auditorProperties.apxCoordinatorContractAddress,
                data = it
            )
        }
    }
}
