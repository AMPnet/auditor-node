package com.ampnet.auditornode

import com.ampnet.auditornode.script.api.model.AbortedAudit
import com.ampnet.auditornode.script.api.model.AuditResult
import com.ampnet.auditornode.script.api.model.AuditStatus
import com.ampnet.auditornode.script.api.model.FailedAudit
import com.ampnet.auditornode.script.api.model.SuccessfulAudit
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyName
import com.fasterxml.jackson.databind.exc.InvalidFormatException
import com.fasterxml.jackson.databind.exc.InvalidNullException
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.SingletonSupport
import io.micronaut.jackson.serialize.JacksonObjectSerializer
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.fail
import java.util.UUID

object TestUtils {

    @Language("RegExp")
    const val UUID_REGEX = "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}"

    val objectSerializer = JacksonObjectSerializer(
        ObjectMapper().apply {
            registerModule(
                SimpleModule().apply {
                    addDeserializer(AuditResult::class.java, AuditResultDeserializer)
                }
            )
            registerModule(KotlinModule(singletonSupport = SingletonSupport.CANONICALIZE))
        }
    )

    fun String.parseScriptId(): UUID? {
        val responseRegex = """^\{"id":"($UUID_REGEX)"}$""".toRegex()
        val matchResult = responseRegex.find(this)
            ?: fail("Response does not match regular expression: $responseRegex")

        return matchResult.groups[1]?.value?.let(UUID::fromString)
    }

    private object AuditResultDeserializer : JsonDeserializer<AuditResult>() {

        override fun deserialize(parser: JsonParser, context: DeserializationContext): AuditResult {
            val tree = parser.codec.readTree<ObjectNode>(parser)
            val auditStatus = tree["status"]?.asText()?.let(AuditStatus::valueOf)

            if (auditStatus == AuditStatus.SUCCESS) {
                return SuccessfulAudit
            }

            val message = tree["message"]?.asText() ?: throw InvalidNullException.from(
                context,
                PropertyName("message"),
                context.contextualType
            )

            return when (auditStatus) {
                AuditStatus.FAILURE -> FailedAudit(message)
                AuditStatus.ABORTED -> AbortedAudit(message)
                else -> throw InvalidFormatException(
                    parser,
                    "Unable to deserialize AuditResult: 'status' field is missing or null",
                    tree,
                    AuditResult::class.java
                )
            }
        }
    }
}
