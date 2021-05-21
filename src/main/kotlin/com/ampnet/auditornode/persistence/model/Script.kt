package com.ampnet.auditornode.persistence.model

import java.util.UUID

inline class ScriptSource(val content: String) // TODO use `value class` instead on later Kotlin version

inline class ScriptId(val value: UUID) // TODO use `value class` instead on later Kotlin version
