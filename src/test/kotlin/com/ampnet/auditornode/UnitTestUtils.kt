package com.ampnet.auditornode

import com.ampnet.auditornode.model.response.IpfsDirectoryUploadResponse
import com.ampnet.auditornode.model.response.IpfsFileUploadResponse
import com.ampnet.auditornode.model.websocket.WebSocketMessage
import com.ampnet.auditornode.persistence.model.IpfsHash
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.SingletonSupport
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.fail
import java.util.UUID

object UnitTestUtils {

    @Language("RegExp")
    const val UUID_REGEX = "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}"

    val objectMapper = ObjectMapper().apply {
        registerModule(KotlinModule(singletonSupport = SingletonSupport.CANONICALIZE))
    }

    fun WebSocketMessage.toJson(): String = objectMapper.writeValueAsString(this)

    fun String.parseScriptId(): UUID? {
        val responseRegex = """^\{"id":"($UUID_REGEX)"}$""".toRegex()
        val matchResult = responseRegex.find(this)
            ?: fail("Response does not match regular expression: $responseRegex")

        return matchResult.groups[1]?.value?.let(UUID::fromString)
    }

    fun String.parseIpfsDirectoryUploadResponse(): IpfsDirectoryUploadResponse {
        val rootNode = objectMapper.readTree(this)
        val files = rootNode["files"].elements().asSequence().map {
            IpfsFileUploadResponse(
                fileName = it["fileName"].asText(),
                ipfsHash = IpfsHash(it["ipfsHash"].asText())
            )
        }.toList()

        return IpfsDirectoryUploadResponse(
            files = files,
            directoryIpfsHash = IpfsHash(rootNode["directoryIpfsHash"].asText())
        )
    }
}
