package com.ampnet.auditornode.script.api.classes

import com.ampnet.auditornode.script.api.model.MapApi
import org.graalvm.polyglot.HostAccess.Export
import org.graalvm.polyglot.Value

object NoOpInput : Input {

    @Export
    override fun readBoolean(message: String): Boolean? = null

    @Export
    override fun readNumber(message: String): Double? = null

    @Export
    override fun readString(message: String): String? = null

    @Export
    override fun readFields(fields: Value, message: String): MapApi<String, Any>? = null

    @Export
    override fun button(message: String) = Unit
}
