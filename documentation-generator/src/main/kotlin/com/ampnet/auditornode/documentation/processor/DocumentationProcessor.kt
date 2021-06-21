package com.ampnet.auditornode.documentation.processor

import com.amptnet.auditornode.documentation.annotation.ScriptApi
import com.google.auto.service.AutoService
import java.nio.file.Paths
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.annotation.processing.SupportedOptions
import javax.annotation.processing.SupportedSourceVersion
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText

@Suppress("unused")
@AutoService(Processor::class)
@SupportedOptions(DocumentationProcessor.DOCUMENTATION_OUTPUT_OPTION_NAME)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
class DocumentationProcessor : AbstractProcessor() {

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
        val outputPath = Paths.get(documentationOutputPath)

        roundEnv?.getElementsAnnotatedWith(ScriptApi::class.java)
            ?.map { ScriptApiProcessor.processElement(it) }
            ?.map { MarkdownFileGenerator.generateMarkdownFile(it) }
            ?.forEach {
                val filePath = outputPath.resolve(it.outputFile)
                filePath.parent.createDirectories()
                filePath.writeText(it.markdown)
            }

        return true
    }
}
