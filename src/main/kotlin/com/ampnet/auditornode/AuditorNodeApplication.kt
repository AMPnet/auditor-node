package com.ampnet.auditornode

import com.ampnet.auditornode.configuration.properties.ProgramArgumentPropertyNames
import io.micronaut.runtime.Micronaut
import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Contact
import io.swagger.v3.oas.annotations.info.Info

@OpenAPIDefinition(
    info = Info(
        title = "Auditor Node",
        version = "v0",
        description = "Auditor Node HTTP API",
        contact = Contact(
            name = "AMPNet Auditor Node",
            url = "https://github.com/AMPnet/auditor-node",
            email = "domagoj@ampnet.io"
        )
    )
)
object AuditorNodeApplication {

    @JvmStatic
    fun main(vararg args: String) {
        if (args.contains("--local-ipfs")) {
            System.setProperty(ProgramArgumentPropertyNames.USE_LOCAL_IPFS, "true")
        }

        Micronaut.build(*args)
            .mainClass(AuditorNodeApplication.javaClass)
            .banner(false)
            .start()
    }
}
