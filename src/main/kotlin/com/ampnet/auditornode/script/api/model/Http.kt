package com.ampnet.auditornode.script.api.model

import org.graalvm.polyglot.HostAccess.Export

class HttpCookie(
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
    val sameSite: String?
)

class HttpResponse(
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