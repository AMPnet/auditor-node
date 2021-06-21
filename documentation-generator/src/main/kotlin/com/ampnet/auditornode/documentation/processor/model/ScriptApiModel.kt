package com.ampnet.auditornode.documentation.processor.model

import com.amptnet.auditornode.documentation.annotation.ScriptApiCategory

data class ScriptApiModel(
    val description: String,
    val category: ScriptApiCategory,
    val hasStaticObject: Boolean,
    val staticObjectName: String,
    val functionModels: List<FunctionModel>,
    val fieldModels: List<FieldModel>
)
