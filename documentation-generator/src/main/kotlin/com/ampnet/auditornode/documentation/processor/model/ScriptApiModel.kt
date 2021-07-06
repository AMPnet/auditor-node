package com.ampnet.auditornode.documentation.processor.model

import com.ampnet.auditornode.documentation.annotation.ScriptApiCategory
import java.nio.file.Path

data class ScriptApiModel(
    val description: String,
    val category: ScriptApiCategory,
    val hasStaticApi: Boolean,
    val apiObjectName: String,
    val functionModels: List<FunctionModel>,
    val fieldModels: List<FieldModel>,
    val functionsDocumentationHeader: String,
    val fieldsDocumentationHeader: String,
    val additionalFunctionDocumentationPaths: List<Path>,
    val additionalFieldDocumentationPaths: List<Path>
)
