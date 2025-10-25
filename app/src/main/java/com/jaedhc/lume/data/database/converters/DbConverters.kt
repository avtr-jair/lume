package com.jaedhc.lume.data.database.converters

import androidx.room.TypeConverter
import java.util.Date

object DbConverters {
    @TypeConverter
    @JvmStatic fun dateToLong(d: Date?): Long? = d?.time
    @TypeConverter
    @JvmStatic fun longToDate(v: Long?): Date? = v?.let { Date(it) }
}