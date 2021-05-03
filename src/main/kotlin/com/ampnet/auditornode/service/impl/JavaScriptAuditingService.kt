package com.ampnet.auditornode.service.impl

import arrow.core.Either
import arrow.core.flatten
import arrow.core.left
import arrow.core.right
import com.ampnet.auditornode.model.error.EvaluationError.InvalidReturnValueError
import com.ampnet.auditornode.model.error.EvaluationError.ScriptExecutionError
import com.ampnet.auditornode.model.error.Try
import com.ampnet.auditornode.model.script.AuditResult
import com.ampnet.auditornode.model.script.AuditResultApi
import com.ampnet.auditornode.model.script.Http
import com.ampnet.auditornode.model.script.JavaScriptApi
import com.ampnet.auditornode.service.AuditingService
import org.graalvm.polyglot.Context
import org.graalvm.polyglot.HostAccess
import org.graalvm.polyglot.Source
import org.graalvm.polyglot.Value
import javax.inject.Singleton

@Singleton
class JavaScriptAuditingService : AuditingService {

    companion object {
        private const val TARGET_LANGUAGE = "js"
        private const val SCRIPT_FUNCTION_CALL = "audit();"
    }

    private val apiPackagePrefix = JavaScriptApi::class.java.`package`.name
    private val jsContextBuilder =
        Context.newBuilder(TARGET_LANGUAGE)
            .allowHostAccess(HostAccess.EXPLICIT)
            .allowHostClassLookup { fullClassName -> fullClassName.startsWith(apiPackagePrefix) }
    private val apiObjects = listOf(Http, AuditResultApi)
        .joinToString(separator = "\n") {
            it.createJavaScriptApiObject()
        }

    override fun evaluate(auditingScript: String): Try<AuditResult> {
        val scriptSource = "$apiObjects\n$auditingScript;\n$SCRIPT_FUNCTION_CALL"

        return Either.catch {
            jsContextBuilder.build()
                .use {
                    val source = Source.create(TARGET_LANGUAGE, scriptSource)
                    val result = it.eval(source)
                    asAuditResult(result)
                }
        }
            .mapLeft { ScriptExecutionError(scriptSource, it) }
            .flatten()
    }

    private fun asAuditResult(result: Value): Try<AuditResult> {
        val hostObject = when (result.isHostObject) {
            true -> result.asHostObject<Any>()
            false -> Unit
        }

        return when (hostObject) {
            is AuditResult -> hostObject.right()
            else -> InvalidReturnValueError(AuditResult::class).left()
        }
    }
}
