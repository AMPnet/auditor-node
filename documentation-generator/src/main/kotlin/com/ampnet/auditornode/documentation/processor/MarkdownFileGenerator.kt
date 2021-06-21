package com.ampnet.auditornode.documentation.processor

import com.ampnet.auditornode.documentation.processor.model.MarkdownModel
import com.ampnet.auditornode.documentation.processor.model.ScriptApiModel
import java.nio.file.Path
import kotlin.io.path.readLines

object MarkdownFileGenerator {

    private val emptyLineList = listOf("")
    private const val API_OBJECT_NAME_PLACEHOLDER = "{apiObjectName}"
    private const val STATIC_OBJECT_NAME = "Static object name: `$API_OBJECT_NAME_PLACEHOLDER`"
    private const val NO_STATIC_OBJECT = "There is no static object available in the scripts."
    private const val NO_FIELDS = "There are no readable fields."
    private const val FIELDS_TABLE_HEADER = "| Field | Description |\n|| ----- | ----------- |\n"
    private const val NO_FUNCTIONS = "There are no functions available."
    private const val FUNCTIONS_TABLE_HEADER = "| Signature | Description | Example call |\n" +
        "|| --------- | ----------- | ------------ |\n"

    fun generateMarkdownFile(scriptApiModel: ScriptApiModel): MarkdownModel {
        val staticObjectLine = if (scriptApiModel.hasStaticApi) STATIC_OBJECT_NAME else NO_STATIC_OBJECT

        val fieldSectionLines = scriptApiModel.fieldModels.createTable(
            header = FIELDS_TABLE_HEADER,
            empty = NO_FIELDS
        ) { "|| ${it.signature} | ${it.description} |" }

        val functionSectionLines = scriptApiModel.functionModels.createTable(
            header = FUNCTIONS_TABLE_HEADER,
            empty = NO_FUNCTIONS
        ) { "|| ${it.signature} | ${it.description} | ${it.exampleCall} |" }

        val markdown = """
            |<details>
            |<summary><b>$API_OBJECT_NAME_PLACEHOLDER</b></summary>
            |
            |${scriptApiModel.description}  
            |
            |$staticObjectLine
            |
            |###### Fields
            |
            |${scriptApiModel.fieldsDocumentationHeader}  
            |
            |$fieldSectionLines
            |
            |${loadResources(scriptApiModel.additionalFieldsDocumentationPaths)}
            |
            |###### Functions
            |
            |${scriptApiModel.functionsDocumentationHeader}  
            |
            |$functionSectionLines
            |
            |${loadResources(scriptApiModel.additionalFunctionsDocumentationPaths)}
            |
            |</details>
        """.trimMargin()

        val fileName = "${scriptApiModel.category.name.lowercase()}/${scriptApiModel.apiObjectName.fileName}.md"

        return MarkdownModel(
            markdown = markdown.replace(API_OBJECT_NAME_PLACEHOLDER, scriptApiModel.apiObjectName.htmlName),
            outputFile = fileName
        )
    }

    private fun <T> List<T>.createTable(header: String, empty: String, columnMapper: (T) -> String): String =
        if (isEmpty()) empty else joinToString(separator = "\n", prefix = header) { columnMapper(it) }

    private fun loadResources(resourcePaths: List<Path>): String =
        (emptyLineList + resourcePaths.flatMap { it.readLines() }.map { "|$it" })
            .joinToString(separator = "\n")

    private val String.htmlName: String
        get() = this.replace("<", "&lt;").replace(">", "&gt;")

    private val String.fileName: String
        get() = this.replace("[<>, ]".toRegex(), "")
}
