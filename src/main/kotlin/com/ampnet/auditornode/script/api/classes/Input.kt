package com.ampnet.auditornode.script.api.classes

import com.ampnet.auditornode.script.api.model.MapApi
import org.graalvm.polyglot.Value

interface Input {
    fun readBoolean(message: String): Boolean?
    fun readNumber(message: String): Double?
    fun readString(message: String): String?
    fun readFields(fields: Value, message: String): MapApi<String, Any>?
    fun button(message: String)
}
