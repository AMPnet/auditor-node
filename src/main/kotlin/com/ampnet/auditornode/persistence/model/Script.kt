package com.ampnet.auditornode.persistence.model

import com.ampnet.auditornode.util.NativeReflection
import java.util.UUID

@JvmInline
@NativeReflection
value class ScriptSource(val content: String)

@JvmInline
@NativeReflection
value class ScriptId(val value: UUID)
