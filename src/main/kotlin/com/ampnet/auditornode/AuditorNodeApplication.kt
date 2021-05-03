package com.ampnet.auditornode

import com.ampnet.auditornode.configuration.ProgramArgumentPropertyNames
import io.micronaut.runtime.Micronaut

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
