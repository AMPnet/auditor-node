package com.ampnet.auditornode.documentation.processor

import com.amptnet.auditornode.documentation.annotation.ScriptApi
import com.google.auto.service.AutoService
import java.nio.file.Files
import java.nio.file.Paths
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.annotation.processing.SupportedOptions
import javax.annotation.processing.SupportedSourceVersion
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement

@Suppress("unused")
@AutoService(Processor::class)
@SupportedOptions(DocumentationGenerator.DOCUMENTATION_OUTPUT_OPTION_NAME)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
class DocumentationGenerator : AbstractProcessor() {

    companion object {
        const val DOCUMENTATION_OUTPUT_OPTION_NAME = "com.amptnet.auditornode.documentation.output"
    }

    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        return mutableSetOf(ScriptApi::class.java.name)
    }

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.RELEASE_8
    }

    override fun process(annotations: MutableSet<out TypeElement>?, roundEnv: RoundEnvironment?): Boolean {
        val documentationOutputPath = processingEnv.options[DOCUMENTATION_OUTPUT_OPTION_NAME]
            ?: throw IllegalArgumentException(
                """Value for $DOCUMENTATION_OUTPUT_OPTION_NAME option is not set. You can set it by adding the following
                   snippet to your build.gradle:

                   kapt {
                       arguments {
                           arg("$DOCUMENTATION_OUTPUT_OPTION_NAME", "...")
                       }
                   }
                """.trimIndent()
            )
        val annotatedElements = roundEnv?.getElementsAnnotatedWith(ScriptApi::class.java)
            ?.map { it?.simpleName?.toString() }
            ?.joinToString(separator = "\n")
            ?.toByteArray() ?: ByteArray(0)
        println("Annotated elements:\n${String(annotatedElements)}")
        Files.write(
            Paths.get("/Users/domagoj/Projects/AMPnet/auditor-node/doc-output-path.txt"),
            documentationOutputPath.toByteArray()
        )
        return true
    }
}
