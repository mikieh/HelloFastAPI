package com.callfinder.app.data.local.db

import androidx.room.TypeConverter
import com.callfinder.app.domain.model.CallType
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {

    private val gson = Gson()

    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, type) ?: emptyList()
    }

    @TypeConverter
    fun fromCallType(value: CallType): String {
        return value.name
    }

    @TypeConverter
    fun toCallType(value: String): CallType {
        return try {
            CallType.valueOf(value)
        } catch (e: IllegalArgumentException) {
            CallType.UNKNOWN
        }
    }
}
