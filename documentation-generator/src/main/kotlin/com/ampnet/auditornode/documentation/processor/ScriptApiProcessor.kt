package com.ampnet.auditornode.documentation.processor

import com.ampnet.auditornode.documentation.processor.model.FieldModel
import com.ampnet.auditornode.documentation.processor.model.FunctionModel
import com.ampnet.auditornode.documentation.processor.model.ScriptApiModel
import com.amptnet.auditornode.documentation.annotation.ScriptApi
import com.amptnet.auditornode.documentation.annotation.ScriptField
import com.amptnet.auditornode.documentation.annotation.ScriptFunction
import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.VariableElement
import javax.lang.model.type.TypeMirror

object ScriptApiProcessor {

    private const val NULLABLE_SIGNATURE = " &#124; null"

    fun processElement(element: Element): ScriptApiModel {
        val scriptApiAnnotation = element.getAnnotation(ScriptApi::class.java)
        val staticObjectName = scriptApiAnnotation.staticObjectName.trim().ifEmpty {
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

        return ScriptApiModel(
            description = scriptApiAnnotation.description,
            category = scriptApiAnnotation.category,
            hasStaticObject = scriptApiAnnotation.hasStaticObject,
            staticObjectName = staticObjectName.toString(),
            functionModels = annotatedFunctionModels + additionalFunctionModels,
            fieldModels = annotatedFieldModels + additionalFieldModels
        )
    }

    private val TypeMirror.simpleName: String
        get() = this.toString().substringAfterLast('.')

    private fun extractScriptFunctionElement(element: Element): FunctionModel? =
        element.getAnnotation(ScriptFunction::class.java)?.let {
            if (element is ExecutableElement) {
                val signature = it.signature.trim().ifEmpty { constructFunctionSignature(element, it.nullable) }

                FunctionModel(
                    description = it.description,
                    exampleCall = it.exampleCall,
                    signature = signature
                )
            } else {
                null
            }
        }

    private fun constructFunctionSignature(functionElement: ExecutableElement, nullable: Boolean): String {
        val functionName = functionElement.simpleName
        val parameters = functionElement.parameters.map {
            "${it.simpleName}: ${it.asType().simpleName}"
        }.joinToString(separator = ", ")
        val returnType = functionElement.returnType.simpleName + if (nullable) NULLABLE_SIGNATURE else ""

        return "<code>$functionName($parameters): $returnType</code>"
    }

    private fun extractScriptFieldElement(element: Element): FieldModel? =
        element.getAnnotation(ScriptField::class.java)?.let {
            if (element is VariableElement) {
                val signature = it.signature.trim().ifEmpty { constructFieldSignature(element, it.nullable) }

                FieldModel(
                    description = it.description,
                    signature = signature
                )
            } else {
                null
            }
        }


    private fun constructFieldSignature(fieldElement: VariableElement, nullable: Boolean): String {
        val fieldName = fieldElement.simpleName
        val fieldType = fieldElement.asType().simpleName + if (nullable) NULLABLE_SIGNATURE else ""

        return "<code>$fieldName: $fieldType</code>"
    }
}
