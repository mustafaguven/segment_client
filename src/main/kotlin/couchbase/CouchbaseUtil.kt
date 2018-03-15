package couchbase

import com.couchbase.lite.*
import com.fasterxml.jackson.databind.ObjectMapper
import domain.SegmentData
import domain.SegmentMap

class CouchbaseUtil {
    companion object {
        const val DB_NAME = "db"
    }

    val manager: Manager? by lazy { Manager(JavaContext(), Manager.DEFAULT_OPTIONS) }
    val database: Database? by lazy { manager?.getDatabase(DB_NAME) }
    private val objectMapper: ObjectMapper  by lazy { ObjectMapper() }

    @Synchronized
    private fun upsertDocument(properties: MutableMap<String, Any>, documentId: String) {
        try {
            var document = database?.getDocument(documentId)
            if (document?.properties == null) {
                document = Document(database, documentId)
                document.putProperties(properties)

            } else {
                val finalDocument = document
                document.update { newRevision: UnsavedRevision ->
                    val oldProperties = HashMap(finalDocument.properties)

                    val cleanPropertiesMap = java.util.HashMap<String, Any>()
                    for ((key, value) in oldProperties) {
                        if (!key.startsWith("_") && value != null) {
                            if (cleanPropertiesMap.put(key, value) != null) {
                                throw IllegalStateException("Duplicate key")
                            }
                        }
                    }

                    if (properties == cleanPropertiesMap) {
                        return@update false
                    }
                    newRevision.userProperties = properties
                    true
                }
            }
        } catch (e: CouchbaseLiteException) {
            //e.printStackTrace()
        }
    }

    fun upsertSegmentDocument(payload: SegmentData) {
        val props = objectMapper.convertValue(payload, Map::class.java)
        upsertDocument(props as MutableMap<String, Any>, payload.segmentId.toString())
    }

    fun upsertSegmentMapDocument(payload: SegmentMap) {
        val props = objectMapper.convertValue(payload, Map::class.java)
        upsertDocument(props as MutableMap<String, Any>, "1")
    }

}