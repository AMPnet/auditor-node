package com.ampnet.auditornode.controller.websocket

import arrow.core.Either
import arrow.core.computations.either
import arrow.core.left
import arrow.core.right
import com.ampnet.auditornode.configuration.properties.AuditorProperties
import com.ampnet.auditornode.controller.websocket.WebSocketSessionHelper.scriptInput
import com.ampnet.auditornode.controller.websocket.WebSocketSessionHelper.scriptState
import com.ampnet.auditornode.controller.websocket.WebSocketSessionHelper.scriptTask
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
import com.ampnet.auditornode.model.websocket.InvalidInputJsonInfoMessage
import com.ampnet.auditornode.model.websocket.IpfsReadErrorInfoMessage
import com.ampnet.auditornode.model.websocket.ReadyState
import com.ampnet.auditornode.model.websocket.RpcErrorInfoMessage
import com.ampnet.auditornode.model.websocket.WebSocketApi
import com.ampnet.auditornode.persistence.model.AssetContractAddress
import com.ampnet.auditornode.persistence.model.IpfsTextFile
import com.ampnet.auditornode.persistence.model.ScriptSource
import com.ampnet.auditornode.persistence.repository.IpfsRepository
import com.ampnet.auditornode.script.api.ExecutionContext
import com.ampnet.auditornode.script.api.classes.DirectoryBasedIpfs
import com.ampnet.auditornode.script.api.classes.WebSocketInput
import com.ampnet.auditornode.script.api.classes.WebSocketOutput
import com.ampnet.auditornode.service.AssetContractService
import com.ampnet.auditornode.service.AuditRegistryContractTransactionService
import com.ampnet.auditornode.service.AuditingService
import com.ampnet.auditornode.service.RegistryContractService
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
    private val assetContractService: AssetContractService,
    private val registryContractService: RegistryContractService,
    private val auditRegistryContractTransactionService: AuditRegistryContractTransactionService,
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
        @PathVariable("assetContractAddress") assetContractAddress: String, // TODO currently hard-coded
        session: WebSocketSession
    ) {
        logger.info {
            "WebSocket connection opened, fetching auditing info from blockchain for asset with contract address: " +
                assetContractAddress
        }
        val webSocketApi = WebSocketApi(session, objectMapper)

        webSocketApi.sendInfoMessage(ConnectedInfoMessage)

        val result = either<ApplicationError, Pair<ScriptSource, ExecutionContext>> {
            val assetInfoIpfsHash = assetContractService.getAssetInfoIpfsHash().bind()
            logger.info { "Asset info IPFS hash: $assetInfoIpfsHash" }

            val assetInfoJson = ipfsRepository.fetchTextFile(assetInfoIpfsHash).bind()
            logger.info { "Successfully fetched asset info file from IPFS" }

            val auditDataJson = parseAuditInfo(assetInfoJson).bind()
            logger.info { "Successfully parsed asset info JSON" }

            val assetCategoryId = assetContractService.getAssetCategoryId().bind()
            logger.info { "Asset category ID: $assetCategoryId" }

            val auditingProcedureDirectoryIpfsHash = registryContractService
                .getAuditingProcedureDirectoryIpfsHash(assetCategoryId)
                .bind()
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

            Pair(auditingScript, executionContext)
        }

        when (result) {
            is Either.Left -> handleOnOpenError(result.value, session, webSocketApi)
            is Either.Right -> handleOnOpenSuccess(result.value.first, result.value.second, session, webSocketApi)
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
            is IpfsError -> webSocketApi.sendInfoMessage(IpfsReadErrorInfoMessage)
            is ParseError -> webSocketApi.sendInfoMessage(InvalidInputJsonInfoMessage)
            is RpcError -> webSocketApi.sendInfoMessage(RpcErrorInfoMessage)
        }

        session.close(CloseReason.NORMAL)
    }

    private fun handleOnOpenSuccess(
        scriptSource: ScriptSource,
        executionContext: ExecutionContext,
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
                    webSocketApi.sendResponse(ErrorResponse(it.message ?: ""))
                },
                ifRight = {
                    logger.info { "Script execution finished successfully, result: $it" }
                    // TODO asset address will not be hard-coded in the future
                    val transaction = auditRegistryContractTransactionService.generateTxForCastAuditVote(
                        assetContractAddress = AssetContractAddress(auditorProperties.assetContractAddress),
                        auditResult = it
                    )
                    webSocketApi.sendResponse(AuditResultResponse(it, transaction))
                }
            )
            session.scriptState = FinishedState
            session.close(CloseReason.NORMAL)
        }

        session.scriptTask = scriptTask
    }

    @OnMessage
    fun onMessage(message: String, session: WebSocketSession) {
        logger.info { "WebSocket message: $message" }

        when (session.scriptState) {
            is InitState, is ReadyState, is FinishedState -> Unit
            is ExecutingState -> session.scriptInput?.push(message)
        }
    }

    @OnClose
    fun onClose(session: WebSocketSession) {
        logger.info { "WebSocket connection closed" }
        session.scriptTask?.dispose()
    }
}
