package com.example.property_listing_system.data.local.database

import androidx.room.TypeConverter
import java.util.Date

/**
 * TypeConverters allow Room to persist custom non-primitive types by converting
 * them to and from SQLite-compatible primitive types (e.g. Long or String).
 */
class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun fromStringList(list: List<String>?): String? {
        return list?.joinToString(separator = ",")
    }

    @TypeConverter
    fun toStringList(data: String?): List<String>? {
        return if (data.isNullOrBlank()) {
            emptyList()
        } else {
            data.split(",").map { it.trim() }
        }
    }
}
