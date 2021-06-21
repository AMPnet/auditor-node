package com.ampnet.auditornode.documentation.processor

import com.ampnet.auditornode.documentation.processor.model.MarkdownModel
import com.ampnet.auditornode.documentation.processor.model.ScriptApiModel

object MarkdownFileGenerator {

    private const val STATIC_OBJECT_NAME_PLACEHOLDER = "{staticObjectName}"
    private const val STATIC_OBJECT_NAME = "Static object name: `${STATIC_OBJECT_NAME_PLACEHOLDER}`"
    private const val NO_STATIC_OBJECT = "There is no static object available in the scripts."
    private const val NO_FIELDS = "There are no readable fields."
    private const val FIELDS_TABLE_HEADER = "| Field | Description |\n|| ----- | ----------- |\n"
    private const val NO_FUNCTIONS = "There are no functions available."
    private const val FUNCTIONS_TABLE_HEADER = "| Signature | Description | Example call |\n" +
        "|| --------- | ----------- | ------------ |\n"

    fun generateMarkdownFile(scriptApiModel: ScriptApiModel): MarkdownModel {
        val staticObjectLine = if (scriptApiModel.hasStaticObject) STATIC_OBJECT_NAME else NO_STATIC_OBJECT

        val fieldTableLines = scriptApiModel.fieldModels.createTable(
            header = FIELDS_TABLE_HEADER,
            empty = NO_FIELDS
        ) { "|| ${it.signature} | ${it.description} |" }

        val functionTableLines = scriptApiModel.functionModels.createTable(
            header = FUNCTIONS_TABLE_HEADER,
            empty = NO_FUNCTIONS
        ) { "|| ${it.signature} | ${it.description} | ${it.exampleCall} |" }

        val markdown = """
            |<details>
            |<summary><b>${scriptApiModel.staticObjectName}</b></summary>
            |
            |${scriptApiModel.description}  
            |$staticObjectLine
            |
            |###### Fields
            |
            |$fieldTableLines
            |
            |###### Functions
            |
            |$functionTableLines
            |
            |</details>
        """.trimMargin()

        val fileName = "${scriptApiModel.category.name.lowercase()}/${scriptApiModel.staticObjectName}.md"

        return MarkdownModel(
            markdown = markdown.replace(STATIC_OBJECT_NAME_PLACEHOLDER, scriptApiModel.staticObjectName),
            outputFile = fileName
        )
    }

    private fun <T> List<T>.createTable(header: String, empty: String, columnMapper: (T) -> String): String =
        if (isEmpty()) empty else joinToString(separator = "\n", prefix = header) { columnMapper(it) }
}
