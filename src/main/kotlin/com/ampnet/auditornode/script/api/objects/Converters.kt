package com.ampnet.auditornode.script.api.objects

import com.ampnet.auditornode.script.api.model.ListApi
import com.ampnet.auditornode.script.api.model.MapApi
import com.ampnet.auditornode.util.NativeReflection
import com.amptnet.auditornode.documentation.annotation.ScriptApi
import com.amptnet.auditornode.documentation.annotation.ScriptApiCategory
import com.amptnet.auditornode.documentation.annotation.ScriptFunction
import org.graalvm.polyglot.HostAccess.Export
import org.graalvm.polyglot.Value
import org.intellij.lang.annotations.Language

@ScriptApi(
    description = "Contains utility functions to convert values between JavaScript native objects and script models.",
    category = ScriptApiCategory.UTILITY,
    hasStaticApi = true,
    additionalFunctions = [
        ScriptFunction(
            description = "Converts List model into a JavaScript array. If the provided argument is not a List " +
                "model, this method will either return an empty array or throw an exception.",
            exampleCall = "`{apiObjectName}.listToArray(someList);`",
            signature = "`listToArray(list: List<?>): Array<?>`"
        ),
        ScriptFunction(
            description = "Converts Map model into a JavaScript object. If the provided argument is not a Map mode, " +
                "this method will either return an empty object or throw an exception.",
            exampleCall = "`{apiObjectName}.mapToObject(someMap);`",
            signature = "`mapToObject(map: Map<?, ?>): Object`"
        )
    ]
)
@NativeReflection
object Converters : JavaScriptApiObject {

    override fun createJavaScriptApiObject(): String {
        val apiObject = super.createJavaScriptApiObject()
        @Language("JavaScript") val objectCreator = """
            (function() {
                $apiObject
                const out = {
                    arrayToList: ${apiObjectName()}.arrayToList,
                    objectToMap: ${apiObjectName()}.objectToMap
                };
                out.listToArray = function(list) {
                    const out = [];
                    for (let i = 0; i < list.length; i++) {
                        out.push(list.get(i));
                    }
                    return out;
                };
                out.mapToObject = function(map) {
                    const out = {};
                    const keys = map.keys();
                    for (let i = 0; i < keys.length; i++) {
                        const key = keys.get(i);
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
    @ScriptFunction(
        description = "Converts JavaScript array into a List model. If the provided argument is not an array, " +
            "empty list will be returned.",
        exampleCall = "`{apiObjectName}.arrayToList([1, 2, 3]);`",
        signature = "`arrayToList(array: Array<?>): List<?>`"
    )
    fun arrayToList(array: Value): ListApi<Value> {
        return if (array.hasArrayElements()) {
            ListApi((0 until array.arraySize).map { array.getArrayElement(it) })
        } else {
            ListApi(emptyList())
        }
    }

    @Export
    @JvmStatic
    @ScriptFunction(
        description = "Converts JavaScript object into a Map model. If the provided argument is not an object, " +
            "empty map will be returned.",
        exampleCall = "`{apiObjectName}.objectToMap({ example: true });`",
        signature = "`objectToMap(obj: Object): Map<String, ?>`"
    )
    fun objectToMap(obj: Value): MapApi<String, Value> {
        return if (obj.hasMembers()) {
            return MapApi(obj.memberKeys.associateWith { obj.getMember(it) })
        } else {
            MapApi(emptyMap())
        }
    }
}
