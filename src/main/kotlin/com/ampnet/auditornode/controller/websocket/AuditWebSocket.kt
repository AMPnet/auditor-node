package com.ampnet.auditornode.controller.websocket

import arrow.core.Either
import arrow.core.computations.either
import arrow.core.left
import arrow.core.right
import com.ampnet.auditornode.configuration.properties.AuditorProperties
import com.ampnet.auditornode.controller.websocket.WebSocketSessionHelper.scriptInput
import com.ampnet.auditornode.controller.websocket.WebSocketSessionHelper.scriptState
import com.ampnet.auditornode.controller.websocket.WebSocketSessionHelper.scriptTask
import com.ampnet.auditornode.model.contract.AssetId
import com.ampnet.auditornode.model.contract.ContractAddress
import com.ampnet.auditornode.model.error.ApplicationError
import com.ampnet.auditornode.model.error.IpfsError
import com.ampnet.auditornode.model.error.ParseError
import com.ampnet.auditornode.model.error.ParseError.JsonParseError
import com.ampnet.auditornode.model.error.RpcError
import com.ampnet.auditornode.model.error.Try
import com.ampnet.auditornode.model.websocket.AuditResultResponse
import com.ampnet.auditornode.model.websocket.ConnectedInfoMessage
import com.ampnet.auditornode.model.websocket.ErrorResponse
import com.ampnet.auditornode.model.websocket.ExecutingInfoMessage
import com.ampnet.auditornode.model.websocket.ExecutingState
import com.ampnet.auditornode.model.websocket.FinishedState
import com.ampnet.auditornode.model.websocket.InitState
import com.ampnet.auditornode.model.websocket.InvalidInputJsonErrorMessage
import com.ampnet.auditornode.model.websocket.IpfsReadErrorMessage
import com.ampnet.auditornode.model.websocket.ReadyState
import com.ampnet.auditornode.model.websocket.RpcErrorMessage
import com.ampnet.auditornode.model.websocket.SpecifyIpfsDirectoryHashCommand
import com.ampnet.auditornode.model.websocket.WaitingForIpfsHash
import com.ampnet.auditornode.model.websocket.WebSocketApi
import com.ampnet.auditornode.persistence.model.IpfsHash
import com.ampnet.auditornode.persistence.model.IpfsTextFile
import com.ampnet.auditornode.persistence.model.ScriptSource
import com.ampnet.auditornode.persistence.repository.IpfsRepository
import com.ampnet.auditornode.script.api.ExecutionContext
import com.ampnet.auditornode.script.api.classes.DirectoryBasedIpfs
import com.ampnet.auditornode.script.api.classes.WebSocketInput
import com.ampnet.auditornode.script.api.classes.WebSocketOutput
import com.ampnet.auditornode.script.api.model.AuditStatus
import com.ampnet.auditornode.service.ApxCoordinatorContractService
import com.ampnet.auditornode.service.AssetHolderContractService
import com.ampnet.auditornode.service.AuditingService
import com.fasterxml.jackson.core.JacksonException
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import io.micronaut.http.annotation.PathVariable
import io.micronaut.scheduling.TaskExecutors
import io.micronaut.websocket.CloseReason
import io.micronaut.websocket.WebSocketSession
import io.micronaut.websocket.annotation.OnClose
import io.micronaut.websocket.annotation.OnMessage
import io.micronaut.websocket.annotation.OnOpen
import io.micronaut.websocket.annotation.ServerWebSocket
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import mu.KotlinLogging
import java.util.concurrent.ExecutorService
import javax.inject.Inject
import javax.inject.Named

private val logger = KotlinLogging.logger {}

@ServerWebSocket("/audit/{assetContractAddress}")
class AuditWebSocket @Inject constructor(
    private val apxCoordinatorContractService: ApxCoordinatorContractService,
    private val assetHolderContractService: AssetHolderContractService,
    private val auditingService: AuditingService,
    private val ipfsRepository: IpfsRepository,
    private val objectMapper: ObjectMapper,
    private val auditorProperties: AuditorProperties,
    @Named(TaskExecutors.IO) executorService: ExecutorService
) {

    companion object {
        private const val AUDITING_SCRIPT_FILE_NAME = "audit.js"
    }

    private val scheduler: Scheduler = Schedulers.from(executorService)

    @OnOpen
    suspend fun onOpen(
        @PathVariable("assetContractAddress") assetContractAddress: String,
        session: WebSocketSession
    ) {
        logger.info {
            "WebSocket connection opened, fetching auditing info from blockchain for asset with contract address: " +
                assetContractAddress
        }
        val webSocketApi = WebSocketApi(session, objectMapper)

        webSocketApi.sendInfoMessage(ConnectedInfoMessage)

        val result = either<ApplicationError, Triple<ScriptSource, ExecutionContext, AssetId>> {
            val assetHolderContractAddress = ContractAddress(assetContractAddress)
            val assetId = assetHolderContractService.getAssetId(assetHolderContractAddress).bind()
            logger.info { "Asset ID: $assetId" }

            val assetInfoIpfsHash = assetHolderContractService.getAssetInfoIpfsHash(assetHolderContractAddress).bind()
            logger.info { "Asset info IPFS hash: $assetInfoIpfsHash" }

            val assetInfoJson = ipfsRepository.fetchTextFile(assetInfoIpfsHash).bind()
            logger.info { "Successfully fetched asset info file from IPFS" }

            val auditDataJson = parseAuditInfo(assetInfoJson).bind()
            logger.info { "Successfully parsed asset info JSON" }

            // TODO for now we will use hard-coded auditing procedure directory IPFS hash since only qualified people
            //  will perform the auditing procedure
            val auditingProcedureDirectoryIpfsHash = IpfsHash(auditorProperties.auditingProcedureDirectoryIpfsHash)
            logger.info { "Auditing procedure directory IPFS hash: $auditingProcedureDirectoryIpfsHash" }

            val auditingScript = ipfsRepository
                .fetchTextFileFromDirectory(auditingProcedureDirectoryIpfsHash, AUDITING_SCRIPT_FILE_NAME)
                .bind()
                .let { ScriptSource(it.content) }
            logger.info { "Successfully fetched auditing script from IPFS" }

            val input = WebSocketInput(webSocketApi)
            session.scriptInput = input

            val output = WebSocketOutput(webSocketApi)
            val ipfs = DirectoryBasedIpfs(auditingProcedureDirectoryIpfsHash, ipfsRepository)
            val executionContext = ExecutionContext(input, output, ipfs, auditDataJson)

            session.scriptState = ReadyState

            Triple(auditingScript, executionContext, assetId)
        }

        when (result) {
            is Either.Left -> handleOnOpenError(result.value, session, webSocketApi)
            is Either.Right -> {
                val (scriptSource, executionContext, assetId) = result.value
                handleOnOpenSuccess(scriptSource, executionContext, assetId, session, webSocketApi)
            }
        }
    }

    private fun parseAuditInfo(assetInfo: IpfsTextFile): Try<JsonNode> =
        try {
            objectMapper.readTree(assetInfo.content).right()
        } catch (e: JacksonException) {
            JsonParseError(assetInfo.content, e).left()
        }

    private fun handleOnOpenError(error: ApplicationError, session: WebSocketSession, webSocketApi: WebSocketApi) {
        when (error) {
            is IpfsError -> webSocketApi.sendErrorMessage(IpfsReadErrorMessage(error.message))
            is ParseError -> webSocketApi.sendErrorMessage(InvalidInputJsonErrorMessage(error.message))
            is RpcError -> webSocketApi.sendErrorMessage(RpcErrorMessage(error.message))
        }

        session.close(CloseReason.NORMAL)
    }

    private fun handleOnOpenSuccess(
        scriptSource: ScriptSource,
        executionContext: ExecutionContext,
        assetId: AssetId,
        session: WebSocketSession,
        webSocketApi: WebSocketApi
    ) {
        session.scriptState = ExecutingState

        logger.info { "Starting auditing script" }
        webSocketApi.sendInfoMessage(ExecutingInfoMessage)

        val scriptTask = scheduler.scheduleDirect {
            auditingService.evaluate(scriptSource.content, executionContext).fold(
                ifLeft = {
                    logger.warn { "Script execution finished with error: ${it.message}" }
                    session.scriptState = FinishedState
                    webSocketApi.sendResponse(ErrorResponse(it.message ?: ""))
                    session.close(CloseReason.NORMAL)
                },
                ifRight = {
                    logger.info { "Script execution finished successfully, result: $it" }

                    when (it.status) {
                        AuditStatus.ABORTED -> {
                            session.scriptState = FinishedState
                            webSocketApi.sendResponse(AuditResultResponse(it, null))
                            session.close(CloseReason.NORMAL)
                        }
                        else -> {
                            session.scriptState = WaitingForIpfsHash(it, assetId)
                            webSocketApi.sendCommand(SpecifyIpfsDirectoryHashCommand(it))
                        }
                    }
                }
            )
        }

        session.scriptTask = scriptTask
    }

    @OnMessage
    fun onMessage(message: String, session: WebSocketSession) {
        logger.info { "WebSocket message: $message" }

        when (val scriptState = session.scriptState) {
            is InitState, is ReadyState, is FinishedState -> {
                logger.debug { "Web socket message discarded: $message" }
            }

            is ExecutingState -> {
                logger.debug { "Script input pushed: $message" }
                session.scriptInput?.push(message)
            }

            is WaitingForIpfsHash -> {
                logger.debug { "Audit result directory IPFS hash: $message" }
                session.scriptState = FinishedState

                // TODO check if IPFS directory exists?
                val transaction = apxCoordinatorContractService.generateTxForPerformAudit(
                    assetId = scriptState.assetId,
                    auditResult = scriptState.auditResult,
                    directoryIpfsHash = IpfsHash(message)
                )
                val webSocketApi = WebSocketApi(session, objectMapper)

                webSocketApi.sendResponse(AuditResultResponse(scriptState.auditResult, transaction))
                session.close(CloseReason.NORMAL)
            }
        }
    }

    @OnClose
    fun onClose(session: WebSocketSession) {
        logger.info { "WebSocket connection closed" }
        session.scriptTask?.dispose()
    }
}
