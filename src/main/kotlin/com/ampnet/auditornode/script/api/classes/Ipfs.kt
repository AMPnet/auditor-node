package com.ampnet.auditornode.script.api.classes

import com.amptnet.auditornode.documentation.annotation.ScriptApi
import com.amptnet.auditornode.documentation.annotation.ScriptApiCategory
import com.amptnet.auditornode.documentation.annotation.ScriptFunction

@ScriptApi(
    description = "Provides support for fetching files located in the same IPFS directory which contains the " +
        "auditing script. If the loaded script was provided locally instead of via IPFS and no IPFS directory was " +
        "specified, then `null` will always be returned. For specifying the IPFS directory for locally provided " +
        "scripts, see web socket documentation.",
    category = ScriptApiCategory.API,
    hasStaticApi = true
)
interface Ipfs {

    @ScriptFunction(
        description = "Reads content of the IPFS file with provided name. The file must be located in the IPFS " +
            "directory associated with the script. If the file cannot be found, `null` is returned.",
        exampleCall = "`{apiObjectName}.getFile(\"example.html\");`",
        nullable = true
    )
    fun getFile(fileName: String): String?

    @ScriptFunction(
        description = "Creates a link relative to the application which can be used to fetch the specified file from " +
            "the IPFS directory associated with the script. If the IPFS directory is not set, `null` is returned.",
        exampleCall = "`{apiObjectName}.linkToFile(\"example.html\");`",
        nullable = true
    )
    fun linkToFile(fileName: String): String?
}
