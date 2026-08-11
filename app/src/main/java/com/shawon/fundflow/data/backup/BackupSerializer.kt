package com.shawon.fundflow.data.backup

import com.shawon.fundflow.data.backup.model.BackupData
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupSerializer @Inject constructor() {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    fun serialize(data: BackupData): String {
        return json.encodeToString(data)
    }

    fun deserialize(jsonString: String): BackupData {
        return json.decodeFromString(jsonString)
    }
}
