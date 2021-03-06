package com.ampnet.auditornode.script.api.model

import com.ampnet.auditornode.documentation.annotation.ScriptApi
import com.ampnet.auditornode.documentation.annotation.ScriptApiCategory
import com.ampnet.auditornode.documentation.annotation.ScriptField
import com.ampnet.auditornode.documentation.annotation.ScriptFunction
import com.ampnet.auditornode.util.NativeReflection
import org.graalvm.polyglot.HostAccess.Export

@ScriptApi(
    description = "Model of the lists returned from the API objects. Contains elements of type `E`.",
    category = ScriptApiCategory.MODEL,
    hasStaticApi = false,
    apiObjectName = "List<E>"
)
@NativeReflection
data class ListApi<out E>(private val underlying: List<E>) {

    @Export
    @ScriptFunction(
        description = "Fetch an item at the specified index from the list.",
        exampleCall = "`someList.get(0);`"
    )
    operator fun get(index: Int): E? = underlying.getOrNull(index)

    @Export
    @JvmField
    @ScriptField(description = "Number of elements in the list.")
    val length: Int = underlying.size
}

@ScriptApi(
    description = "Model of the maps returned from the API objects. Contains elements of type `V` stored under keys " +
        "of type `K`.",
    category = ScriptApiCategory.MODEL,
    hasStaticApi = false,
    apiObjectName = "Map<K, V>"
)
@NativeReflection
data class MapApi<K, out V>(private val underlying: Map<K, V>) {

    @Export
    @ScriptFunction(
        description = "Fetch an item for the specified key from the map.",
        exampleCall = "`someMap.get(\"exampleKey\");`"
    )
    operator fun get(key: K): V? = underlying[key]

    @Export
    @JvmField
    @ScriptField(description = "Number of elements in the map.")
    val size: Int = underlying.size

    @Export
    @ScriptFunction(
        description = "Returns a list of all the keys contained in the map.",
        exampleCall = "`someMap.keys();`",
        signature = "`keys(): List<K>`"
    )
    fun keys(): ListApi<K> = ListApi(underlying.keys.toList())
}
