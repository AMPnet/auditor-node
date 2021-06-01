package com.ampnet.auditornode.script.api.model

import com.ampnet.auditornode.util.NativeReflection
import org.graalvm.polyglot.HostAccess.Export

@NativeReflection
class ListApi<out E>(private val underlying: List<E>) {

    @Export
    operator fun get(index: Int): E? = underlying.getOrNull(index)

    @Export
    @JvmField
    val length: Int = underlying.size
}

@NativeReflection
class MapApi<K, out V>(private val underlying: Map<K, V>) {

    @Export
    operator fun get(key: K): V? = underlying[key]

    @Export
    @JvmField
    val size: Int = underlying.size

    @Export
    fun keys(): ListApi<K> = ListApi(underlying.keys.toList())
}
