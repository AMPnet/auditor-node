package com.ampnet.auditornode.persistence.repository

import com.ampnet.auditornode.persistence.model.ScriptId
import com.ampnet.auditornode.persistence.model.ScriptSource

interface ScriptRepository {
    fun store(source: ScriptSource): ScriptId
    fun load(id: ScriptId): ScriptSource?
}
