package com.ampnet.auditornode.script.api.model

import com.ampnet.auditornode.util.NativeReflection
import com.amptnet.auditornode.documentation.annotation.ScriptApi
import com.amptnet.auditornode.documentation.annotation.ScriptApiCategory
import com.amptnet.auditornode.documentation.annotation.ScriptField
import org.graalvm.polyglot.HostAccess.Export

@ScriptApi(
    description = "Model of HTTP cookie objects.",
    category = ScriptApiCategory.MODEL,
    hasStaticApi = false
)
@NativeReflection
data class HttpCookie(
    @Export
    @JvmField
    @ScriptField(description = "Name of the cookie.", nullable = false)
    val name: String,
    @Export
    @JvmField
    @ScriptField(description = "Value of the cookie.", nullable = false)
    val value: String,
    @Export
    @JvmField
    @ScriptField(description = "Cookie domain, if specified.", nullable = true)
    val domain: String?,
    @Export
    @JvmField
    @ScriptField(description = "Cookie path, if specified.", nullable = true)
    val path: String?,
    @Export
    @JvmField
    @ScriptField(description = "Specifies whether the cookie is HTTP-only.", nullable = false)
    val httpOnly: Boolean,
    @Export
    @JvmField
    @ScriptField(description = "Specifies whether the cookie is secure.", nullable = false)
    val secure: Boolean,
    @Export
    @JvmField
    @ScriptField(description = "Maximum age of the cookie in seconds.", nullable = false)
    val maxAge: Long,
    @Export
    @JvmField
    @ScriptField(description = "`SameSite` attribute value of the cookie.", nullable = false)
    val sameSite: String
)

@ScriptApi(
    description = "Model of HTTP response objects.",
    category = ScriptApiCategory.MODEL,
    hasStaticApi = false
)
@NativeReflection
data class HttpResponse(
    @Export
    @JvmField
    @ScriptField(description = "Response body.", nullable = true)
    val body: String?,
    @Export
    @JvmField
    @ScriptField(description = "Response status code.", nullable = false)
    val statusCode: Int,
    @Export
    @JvmField
    @ScriptField(
        description = "Response headers.",
        signature = "`headers: Map<String, List<String>>`",
        nullable = false
    )
    val headers: MapApi<String, ListApi<String>>,
    @Export
    @JvmField
    @ScriptField(description = "Response cookies.", signature = "`cookies: List<HttpCookie>`", nullable = false)
    val cookies: ListApi<HttpCookie>
)
