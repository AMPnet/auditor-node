package com.amptnet.auditornode.documentation.annotation

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class ScriptApi(
    val description: String,
    val category: ScriptApiCategory,
    val hasStaticObject: Boolean,
    val staticObjectName: String = "",
    val additionalFunctions: Array<ScriptFunction> = [],
    val additionalFields: Array<ScriptField> = []
)
