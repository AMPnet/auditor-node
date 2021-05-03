package com.ampnet.auditornode.script.evaluation

import arrow.core.Either
import arrow.core.flatten
import arrow.core.left
import arrow.core.right
import com.ampnet.auditornode.error.EvaluationError.InvalidReturnValueError
import com.ampnet.auditornode.error.EvaluationError.ScriptExecutionError
import com.ampnet.auditornode.error.Try
import com.ampnet.auditornode.script.api.AuditResult
import com.ampnet.auditornode.script.api.AuditResultApi
import com.ampnet.auditornode.script.api.Http
import com.ampnet.auditornode.script.api.JavaScriptApi
import org.graalvm.polyglot.Context
import org.graalvm.polyglot.HostAccess
import org.graalvm.polyglot.Source
import org.graalvm.polyglot.Value

object JavaScriptEvaluator {

    private const val TARGET_LANGUAGE = "js"
    private const val SCRIPT_FUNCTION_CALL = "audit();"

    private val apiPackagePrefix = JavaScriptApi::class.java.`package`.name
    private val jsContextBuilder =
        Context.newBuilder(TARGET_LANGUAGE)
            .allowHostAccess(HostAccess.EXPLICIT)
            .allowHostClassLookup { fullClassName -> fullClassName.startsWith(apiPackagePrefix) }
    private val apiObjects = listOf(Http, AuditResultApi)
        .joinToString(separator = "\n") {
            it.createJavaScriptApiObject()
        }

    fun evaluate(input: String): Try<AuditResult> {
        val scriptSource = "$apiObjects\n$input;\n$SCRIPT_FUNCTION_CALL"

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
