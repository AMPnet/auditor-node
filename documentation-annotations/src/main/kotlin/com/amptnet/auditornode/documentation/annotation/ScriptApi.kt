package com.amptnet.auditornode.documentation.annotation

@Suppress("LongParameterList")
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class ScriptApi(
    val description: String,
    val category: ScriptApiCategory,
    val hasStaticApi: Boolean,
    val apiObjectName: String = "",
    val additionalFunctions: Array<ScriptFunction> = [],
    val additionalFields: Array<ScriptField> = [],
    val functionsDocumentationHeader: String = "",
    val fieldsDocumentationHeader: String = "",
    val additionalFunctionsDocumentation: Array<String> = [],
    val additionalFieldsDocumentation: Array<String> = []
)
