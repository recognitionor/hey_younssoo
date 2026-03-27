package com.bium.youngssoo.core.data.local

import androidx.room.TypeConverter

class QuestionCategoryConverter {
    @TypeConverter
    fun fromCategory(category: QuestionCategory): String {
        return category.name
    }

    @TypeConverter
    fun toCategory(value: String): QuestionCategory {
        return QuestionCategory.valueOf(value)
    }
}
