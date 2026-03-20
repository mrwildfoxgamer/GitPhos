package com.example.gitphos.data.local.db

import androidx.room.TypeConverter

class Converters {

    @TypeConverter
    fun fromLongNullable(value: Long?): String = value?.toString() ?: ""

    @TypeConverter
    fun toLongNullable(value: String): Long? = value.toLongOrNull()
}