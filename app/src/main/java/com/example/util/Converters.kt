package com.example.util

import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID

class Converters {
    @TypeConverter
    fun fromUUID(uuid: UUID?): String? {
        return uuid?.toString()
    }

    @TypeConverter
    fun toUUID(uuidString: String?): UUID? {
        return uuidString?.let { UUID.fromString(it) }
    }

    @TypeConverter
    fun fromStringList(list: List<String>?): String {
        return list?.let { Json.encodeToString(it) } ?: "[]"
    }

    @TypeConverter
    fun toStringList(data: String?): List<String> {
        return data?.let { Json.decodeFromString(it) } ?: emptyList()
    }
}
