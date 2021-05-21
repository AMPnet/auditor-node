package com.ampnet.auditornode

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.SingletonSupport
import io.micronaut.jackson.serialize.JacksonObjectSerializer
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.fail
import java.util.UUID

object TestUtils {

    @Language("RegExp")
    const val UUID_REGEX = "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}"

    val objectMapper = ObjectMapper().apply {
        registerModule(KotlinModule(singletonSupport = SingletonSupport.CANONICALIZE))
    }
    val objectSerializer = JacksonObjectSerializer(objectMapper)

    fun String.parseScriptId(): UUID? {
        val responseRegex = """^\{"id":"($UUID_REGEX)"}$""".toRegex()
        val matchResult = responseRegex.find(this)
            ?: fail("Response does not match regular expression: $responseRegex")

        return matchResult.groups[1]?.value?.let(UUID::fromString)
    }
}
