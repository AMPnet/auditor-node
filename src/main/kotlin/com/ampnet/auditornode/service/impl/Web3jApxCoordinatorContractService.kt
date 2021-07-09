package com.ampnet.auditornode.service.impl

import arrow.core.Either
import com.ampnet.auditornode.configuration.properties.AuditorProperties
import com.ampnet.auditornode.configuration.properties.RpcProperties
import com.ampnet.auditornode.contract.ApxCoordinator
import com.ampnet.auditornode.model.contract.AssetAuditGapDuration
import com.ampnet.auditornode.model.contract.AssetId
import com.ampnet.auditornode.model.contract.Auditor
import com.ampnet.auditornode.model.contract.AuditorPool
import com.ampnet.auditornode.model.contract.AuditorPoolId
import com.ampnet.auditornode.model.contract.ContractAddress
import com.ampnet.auditornode.model.contract.EthereumAddress
import com.ampnet.auditornode.model.contract.UnsignedTransaction
import com.ampnet.auditornode.model.contract.UsdcPerAudit
import com.ampnet.auditornode.model.contract.UsdcPerList
import com.ampnet.auditornode.model.error.Try
import com.ampnet.auditornode.persistence.model.IpfsHash
import com.ampnet.auditornode.script.api.model.AbortedAudit
import com.ampnet.auditornode.script.api.model.AuditResult
import com.ampnet.auditornode.script.api.model.FailedAudit
import com.ampnet.auditornode.script.api.model.SuccessfulAudit
import com.ampnet.auditornode.service.AbstractWeb3jContractService
import com.ampnet.auditornode.service.ApxCoordinatorContractService
import com.ampnet.auditornode.service.AssetListHolderContractService
import com.ampnet.auditornode.service.ContractProvider
import com.ampnet.auditornode.service.ERC20ContractService
import mu.KotlinLogging
import org.web3j.protocol.Web3j
import org.web3j.tx.ReadonlyTransactionManager
import org.web3j.tx.gas.DefaultGasProvider
import java.math.BigInteger
import javax.inject.Inject
import javax.inject.Singleton

private val logger = KotlinLogging.logger {}

@Singleton
@Suppress("UsePropertyAccessSyntax", "TooManyFunctions")
class Web3jApxCoordinatorContractService @Inject constructor(
    web3j: Web3j,
    contractProvider: ContractProvider,
    rpcProperties: RpcProperties,
    private val auditorProperties: AuditorProperties
) : AbstractWeb3jContractService(logger, rpcProperties), ApxCoordinatorContractService {

    private class Contract(override val contractAddress: ContractAddress, web3j: Web3j) : IContract, ApxCoordinator(
        contractAddress.value,
        web3j,
        ReadonlyTransactionManager(web3j, contractAddress.value),
        DefaultGasProvider()
    )

    override val contractName: String = "APX coordinator"

    private val contract by lazy {
        Contract(ContractAddress(auditorProperties.apxCoordinatorContractAddress), web3j)
    }

    private val cachedStableCoinContract: ERC20ContractService by lazy {
        cacheableContract(
            "stable coin contract address",
            { it.stablecoin().send() },
            contractProvider::getERC20Contract
        )
    }

    private val cachedAssetListHolderContract: AssetListHolderContractService by lazy {
        cacheableContract(
            "asset list holder contract address",
            { it.assetListHolder().send() },
            contractProvider::getAssetListHolderContract
        )
    }

    private fun <C> cacheableContract(
        valueName: String,
        addressGetter: (Contract) -> String,
        contractConstructor: (ContractAddress) -> C
    ): C {
        val contract = getValueFromContract(valueName, contract, addressGetter) {
            contractConstructor(ContractAddress(it))
        }

        return when (contract) {
            is Either.Right -> contract.value
            is Either.Left -> throw contract.value
        }
    }

    @Suppress("UNCHECKED_CAST") // we always throw ApplicationError here
    override fun getStableCoinContract(): Try<ERC20ContractService> =
        Either.catch { cachedStableCoinContract } as Try<ERC20ContractService>

    @Suppress("UNCHECKED_CAST") // we always throw ApplicationError here
    override fun getAssetListHolderContract(): Try<AssetListHolderContractService> =
        Either.catch { cachedAssetListHolderContract } as Try<AssetListHolderContractService>

    override fun getAssetAuditGapDuration(): Try<AssetAuditGapDuration> =
        getValueFromContract(
            "asset audit gap duration",
            contract,
            { it.auditGapDuration().send() },
            ::AssetAuditGapDuration
        )

    override fun getUsdcPerAudit(): Try<UsdcPerAudit> =
        getValueFromContract("USDC per audit", contract, { it.usdcPerAudit().send() }, ::UsdcPerAudit)

    override fun getUsdcPerList(): Try<UsdcPerList> =
        getValueFromContract("USDC per list", contract, { it.usdcPerList().send() }, ::UsdcPerList)

    @Suppress("UNCHECKED_CAST")
    override fun getAuditorPoolMemberships(auditorAddress: EthereumAddress): Try<List<AuditorPoolId>> =
        getValueFromContract(
            "auditor pool memberships",
            contract,
            { it.getPoolMemberships(auditorAddress.value).send() },
            { (it as List<BigInteger>).map(::AuditorPoolId) }
        )

    @Suppress("UNCHECKED_CAST")
    override fun getAuditorPools(): Try<List<AuditorPool>> =
        getValueFromContract("auditor pools", contract, { it.getPools().send() }) {
            (it as List<ApxCoordinator.AuditorPool>)
                .map { pool ->
                    AuditorPool(
                        id = AuditorPoolId(pool.id),
                        name = pool.name,
                        info = IpfsHash(pool.info),
                        active = pool.active,
                        activeMembers = pool.activeMembers
                    )
                }
        }

    override fun getAuditorPoolById(auditorPoolId: AuditorPoolId): Try<AuditorPool> =
        getValueFromContract("auditor pool by ID", contract, { it.getPoolById(auditorPoolId.value).send() }) {
            AuditorPool(
                id = AuditorPoolId(it.id),
                name = it.name,
                info = IpfsHash(it.info),
                active = it.active,
                activeMembers = it.activeMembers
            )
        }

    @Suppress("UNCHECKED_CAST")
    override fun getAuditorPoolMembers(auditorPoolId: AuditorPoolId): Try<List<Auditor>> =
        getValueFromContract("auditor pool members", contract, { it.getPoolMembers(auditorPoolId.value).send() }) {
            (it as List<ApxCoordinator.Auditor>)
                .map { auditor ->
                    Auditor(
                        address = EthereumAddress(auditor.auditor),
                        totalAuditsPerformed = auditor.totalAuditsPerformed,
                        totalListingsPerformed = auditor.totalListingsPerformed,
                        info = IpfsHash(auditor.info)
                    )
                }
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
