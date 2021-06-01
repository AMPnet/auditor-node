package com.ampnet.auditornode.persistence.model

import com.ampnet.auditornode.util.NativeReflection
import java.util.UUID

@NativeReflection
inline class ScriptSource(val content: String) // TODO use `value class` instead on later Kotlin version

@NativeReflection
inline class ScriptId(val value: UUID) // TODO use `value class` instead on later Kotlin version
