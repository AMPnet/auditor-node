package com.ampnet.auditornode.persistence.repository.impl

import com.ampnet.auditornode.persistence.model.ScriptId
import com.ampnet.auditornode.persistence.model.ScriptSource
import com.ampnet.auditornode.persistence.repository.ScriptRepository
import com.ampnet.auditornode.util.UuidProvider
import mu.KotlinLogging
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

private val logger = KotlinLogging.logger {}

@Singleton
class InMemoryScriptRepository @Inject constructor(private val uuidProvider: UuidProvider) : ScriptRepository {

    private val storage = ConcurrentHashMap<ScriptId, ScriptSource>()

    override fun store(source: ScriptSource): ScriptId {
        val id = ScriptId(uuidProvider.getUuid())
        logger.info { "Storing script, id: $id, source: $source" }
        storage[id] = source
        return id
    }

    override fun load(id: ScriptId): ScriptSource? {
        logger.info { "Loading script with id: $id" }
        val script = storage[id]

        if (script == null) {
            logger.warn { "Script not found for id: $id" }
        } else {
            logger.info { "Script found for id: $id" }
        }

        return script
    }
}
