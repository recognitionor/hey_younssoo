package com.bium.youngssoo.game.hanja.data

import com.russhwolf.settings.Settings
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

class HanjaPreferencesRepository(
    private val settings: Settings
) {
    companion object {
        private const val KEY_SELECTED_GRADES = "hanja_selected_grades"
    }

    private val json = Json

    fun saveSelectedGrades(grades: Set<String>) {
        val gradesJson = json.encodeToString(grades.toList())
        settings.putString(KEY_SELECTED_GRADES, gradesJson)
    }

    fun getSelectedGrades(): Set<String> {
        return try {
            val gradesJson = settings.getStringOrNull(KEY_SELECTED_GRADES) ?: return emptySet()
            val gradesList = json.decodeFromString<List<String>>(gradesJson)
            gradesList.toSet()
        } catch (e: Exception) {
            // JSON 파싱 실패 시 빈 Set 반환
            emptySet()
        }
    }

    fun clearSelectedGrades() {
        settings.remove(KEY_SELECTED_GRADES)
    }
}
