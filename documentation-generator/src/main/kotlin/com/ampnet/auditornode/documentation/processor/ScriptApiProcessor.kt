package com.ampnet.auditornode.documentation.processor

import com.ampnet.auditornode.documentation.processor.model.FieldModel
import com.ampnet.auditornode.documentation.processor.model.FunctionModel
import com.ampnet.auditornode.documentation.processor.model.ScriptApiModel
import com.amptnet.auditornode.documentation.annotation.ScriptApi
import com.amptnet.auditornode.documentation.annotation.ScriptField
import com.amptnet.auditornode.documentation.annotation.ScriptFunction
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable
import java.nio.file.Path
import java.nio.file.Paths
import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement
import javax.lang.model.type.TypeKind
import javax.lang.model.type.TypeMirror

object ScriptApiProcessor {

    private val TYPE_SUBSTITUTIONS = mapOf(
        "int" to "Int",
        "boolean" to "Boolean",
        "long" to "Long",
        "Value" to "Object",
        "Double" to "Number",
        "double" to "Number",
        "void" to "Void"
    )

    private const val NULLABLE_SIGNATURE = " &#124; null"

    fun processElement(element: TypeElement, resourcesPath: Path): ScriptApiModel {
        val scriptApiAnnotation = element.getAnnotation(ScriptApi::class.java)
        val apiObjectName = scriptApiAnnotation.apiObjectName.trim().ifEmpty {
            element.simpleName
        }

        val annotatedFunctionModels = element.enclosedElements.mapNotNull(::extractScriptFunctionElement)
        val additionalFunctionModels = scriptApiAnnotation.additionalFunctions.map {
            FunctionModel(
                description = it.description,
                exampleCall = it.exampleCall,
                signature = it.signature
            )
        }

        val annotatedFieldModels = element.enclosedElements.mapNotNull(::extractScriptFieldElement)
        val additionalFieldModels = scriptApiAnnotation.additionalFields.map {
            FieldModel(
                description = it.description,
                signature = it.signature
            )
        }

        val packagePath = element.qualifiedName.toString().substringBeforeLast('.').replace('.', '/')
        val additionalFunctionsDocumentationPaths = scriptApiAnnotation.additionalFunctionDocumentationFiles.map {
            resourcesPath.resolve(Paths.get("$packagePath/$it"))
        }
        val additionalFieldsDocumentationPaths = scriptApiAnnotation.additionalFieldDocumentationFiles.map {
            resourcesPath.resolve(Paths.get("$packagePath/$it"))
        }

        return ScriptApiModel(
            description = scriptApiAnnotation.description,
            category = scriptApiAnnotation.category,
            hasStaticApi = scriptApiAnnotation.hasStaticApi,
            apiObjectName = apiObjectName.toString(),
            functionModels = annotatedFunctionModels + additionalFunctionModels,
            fieldModels = annotatedFieldModels + additionalFieldModels,
            functionsDocumentationHeader = scriptApiAnnotation.functionsDocumentationHeader,
            fieldsDocumentationHeader = scriptApiAnnotation.fieldsDocumentationHeader,
            additionalFunctionDocumentationPaths = additionalFunctionsDocumentationPaths,
            additionalFieldDocumentationPaths = additionalFieldsDocumentationPaths
        )
    }

    private val TypeMirror.simpleName: String
        get() {
            val name = this.toString().substringAfterLast('.')
            return TYPE_SUBSTITUTIONS[name] ?: name
        }

    private fun extractScriptFunctionElement(element: Element): FunctionModel? =
        element.getAnnotation(ScriptFunction::class.java)?.let {
            if (element is ExecutableElement) {
                val signature = it.signature.trim().ifEmpty { constructFunctionSignature(element) }

                FunctionModel(
                    description = it.description,
                    exampleCall = it.exampleCall,
                    signature = signature
                )
            } else {
                null
            }
        }

    private fun constructFunctionSignature(functionElement: ExecutableElement): String {
        val functionName = functionElement.simpleName
        val parameters = functionElement.parameters.joinToString(separator = ", ") {
            "${it.simpleName}: ${it.asType().simpleName}"
        }

        val returnType = functionElement.returnType.simpleName +
            if (functionElement.isNullable) NULLABLE_SIGNATURE else ""

        return "<code>$functionName($parameters): $returnType</code>"
    }

    private fun extractScriptFieldElement(element: Element): FieldModel? =
        element.getAnnotation(ScriptField::class.java)?.let {
            if (element is VariableElement) {
                val signature = it.signature.trim().ifEmpty { constructFieldSignature(element) }

                FieldModel(
                    description = it.description,
                    signature = signature
                )
            } else {
                null
            }
        }

    private fun constructFieldSignature(fieldElement: VariableElement): String {
        val fieldName = fieldElement.simpleName
        val fieldType = fieldElement.asType().simpleName + if (fieldElement.isNullable) NULLABLE_SIGNATURE else ""

        return "<code>$fieldName: $fieldType</code>"
    }

    private val Element.isNullable: Boolean
        get() {
            if (this.getAnnotation(Nullable::class.java) != null) {
                return true
            }

            if (this.getAnnotation(NotNull::class.java) != null) {
                return false
            }

            val isPrimitiveOrVoid = when (this) {
                is ExecutableElement -> this.returnType.isPrimitiveOrVoid
                is VariableElement -> this.asType().isPrimitiveOrVoid
                else -> false
            }

            if (isPrimitiveOrVoid) {
                return false
            }

            throw IllegalStateException("Cannot determine nullability for element: $this")
        }

    private val TypeMirror.isPrimitiveOrVoid: Boolean
        get() = kind.isPrimitive || kind == TypeKind.VOID
}
