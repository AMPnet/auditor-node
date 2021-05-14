package com.ampnet.auditornode.script.api.objects

import com.ampnet.auditornode.script.api.model.ListApi
import com.ampnet.auditornode.script.api.model.MapApi
import org.graalvm.polyglot.HostAccess.Export
import org.graalvm.polyglot.Value
import org.intellij.lang.annotations.Language

object Converters : JavaScriptApiObject {

    override fun createJavaScriptApiObject(): String {
        val apiObject = super.createJavaScriptApiObject()
        @Language("JavaScript") val objectCreator = """
            (function() {
                $apiObject
                let out = {
                    arrayToList: ${apiObjectName()}.arrayToList,
                    objectToMap: ${apiObjectName()}.objectToMap
                };
                out.listToArray = function(list) {
                    let out = [];
                    for (var i = 0; i < list.length; i++) {
                        out.push(list.get(i));
                    }
                    return out;
                };
                out.mapToObject = function(map) {
                    let out = {};
                    let keys = map.keys();
                    for (var i = 0; i < keys.length; i++) {
                        let key = keys.get(i);
                        out[key] = map.get(key);
                    }
                    return out;
                };
                return out;
            })();
        """.trimIndent()
        return "const ${apiObjectName()} = $objectCreator"
    }

    @Export
    @JvmStatic
    fun arrayToList(array: Value): ListApi<Value> {
        return if (array.hasArrayElements()) {
            ListApi((0 until array.arraySize).map { array.getArrayElement(it) })
        } else {
            ListApi(emptyList())
        }
    }

    @Export
    @JvmStatic
    fun objectToMap(obj: Value): MapApi<String, Value> {
        return if (obj.hasMembers()) {
            return MapApi(obj.memberKeys.associateWith { obj.getMember(it) })
        } else {
            MapApi(emptyMap())
        }
    }
}
