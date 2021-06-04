package com.ampnet.auditornode.script.api.model

import com.ampnet.auditornode.util.NativeReflection
import org.graalvm.polyglot.HostAccess.Export

@NativeReflection
data class HttpCookie(
    @Export
    @JvmField
    val name: String,
    @Export
    @JvmField
    val value: String,
    @Export
    @JvmField
    val domain: String?,
    @Export
    @JvmField
    val path: String?,
    @Export
    @JvmField
    val httpOnly: Boolean,
    @Export
    @JvmField
    val secure: Boolean,
    @Export
    @JvmField
    val maxAge: Long,
    @Export
    @JvmField
    val sameSite: String
)

@NativeReflection
data class HttpResponse(
    @Export
    @JvmField
    val body: String?,
    @Export
    @JvmField
    val statusCode: Int,
    @Export
    @JvmField
    val headers: MapApi<String, ListApi<String>>,
    @Export
    @JvmField
    val cookies: ListApi<HttpCookie>
)
