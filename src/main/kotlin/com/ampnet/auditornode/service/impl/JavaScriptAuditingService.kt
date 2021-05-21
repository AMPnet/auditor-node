package com.ampnet.auditornode.service.impl

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.flatten
import arrow.core.left
import arrow.core.right
import com.ampnet.auditornode.model.error.EvaluationError.InvalidReturnValueError
import com.ampnet.auditornode.model.error.EvaluationError.ScriptExecutionError
import com.ampnet.auditornode.model.error.Try
import com.ampnet.auditornode.script.api.ExecutionContext
import com.ampnet.auditornode.script.api.classes.HttpClient
import com.ampnet.auditornode.script.api.model.AuditResult
import com.ampnet.auditornode.script.api.objects.AuditResultApi
import com.ampnet.auditornode.script.api.objects.Converters
import com.ampnet.auditornode.script.api.objects.JavaScriptApiObject
import com.ampnet.auditornode.script.api.objects.Properties
import com.ampnet.auditornode.service.AuditingService
import mu.KotlinLogging
import org.graalvm.polyglot.Context
import org.graalvm.polyglot.EnvironmentAccess
import org.graalvm.polyglot.HostAccess
import org.graalvm.polyglot.PolyglotAccess
import org.graalvm.polyglot.Source
import org.graalvm.polyglot.Value
import javax.inject.Inject
import javax.inject.Singleton

private val logger = KotlinLogging.logger {}

@Singleton
class JavaScriptAuditingService @Inject constructor(httpClient: HttpClient, properties: Properties) : AuditingService {

    companion object {
        private const val TARGET_LANGUAGE = "js"
        private const val SCRIPT_FUNCTION_CALL = "audit();"
        private val languageOptions = mapOf(
            "js.ecmascript-version" to "2020"
        )
    }

    private val apiObjectPackagePrefix = JavaScriptApiObject::class.java.`package`.name
    private val jsContextBuilder =
        Context.newBuilder(TARGET_LANGUAGE)
            .options(languageOptions)
            .allowIO(false)
            .allowCreateThread(false)
            .allowCreateProcess(false)
            .allowNativeAccess(false)
            .allowPolyglotAccess(PolyglotAccess.NONE)
            .allowEnvironmentAccess(EnvironmentAccess.NONE)
            .allowHostAccess(HostAccess.EXPLICIT)
            .allowHostClassLookup { fullClassName -> fullClassName.startsWith(apiObjectPackagePrefix) }

    private val apiObjects = listOf(AuditResultApi, Converters, properties)
        .joinToString(separator = "\n") { it.createJavaScriptApiObject() }
    private val apiClasses = mapOf<String, Any>(
        "HttpClient" to httpClient
    )

    override fun evaluate(auditingScript: String, executionContext: ExecutionContext): Try<AuditResult> {
        val scriptSource = "$apiObjects\n$auditingScript;\n$SCRIPT_FUNCTION_CALL"
        logger.info { "Evaluating auditing script:\n$auditingScript" }
        logger.debug { "Full script source:\n$scriptSource" }

        return Either.catch {
            jsContextBuilder.build()
                .use {
                    val apiBindings = it.getBindings(TARGET_LANGUAGE)

                    (apiClasses + executionContext.apiClasses())
                        .map { (identifier, value) -> apiBindings.putMember(identifier, value) }

                    val source = Source.create(TARGET_LANGUAGE, scriptSource)
                    logger.info { "Script evaluation starting" }
                    val result = it.eval(source)
                    asAuditResult(result)
                }
        }
            .mapLeft { ScriptExecutionError(it) }
            .flatten()
    }

    private fun asAuditResult(result: Value): Try<AuditResult> =
        Either.conditionally(
            test = result.isHostObject,
            ifTrue = { result.asHostObject<Any>() },
            ifFalse = { InvalidReturnValueError("<native value>") }
        )
            .flatMap {
                when (it) {
                    is AuditResult -> it.right()
                    else -> InvalidReturnValueError(it.javaClass).left()
                }
            }
}
