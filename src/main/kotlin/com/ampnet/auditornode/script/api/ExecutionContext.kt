package com.ampnet.auditornode.script.api

import com.ampnet.auditornode.script.api.classes.Input
import com.ampnet.auditornode.script.api.classes.Ipfs
import com.ampnet.auditornode.script.api.classes.NoOpInput
import com.ampnet.auditornode.script.api.classes.NoOpIpfs
import com.ampnet.auditornode.script.api.classes.NoOpOutput
import com.ampnet.auditornode.script.api.classes.Output

data class ExecutionContext(val input: Input, val output: Output, val ipfs: Ipfs) {

    companion object {
        val noOp = ExecutionContext(NoOpInput, NoOpOutput, NoOpIpfs)
    }

    fun apiClasses(): Map<String, Any> {
        return mapOf(
            "Input" to input,
            "Output" to output,
            "Ipfs" to ipfs
        )
    }
}
