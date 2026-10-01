package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "students",
    indices = [Index(value = ["grNo"], unique = true)]
)
data class Student(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val grNo: String,
    val rollNo: Int,
    val name: String,
    val gender: String, // "KUMAR" or "KANYA"
    val standard: String, // e.g., "ધોરણ ૫", "ધોરણ ૬", "ધોરણ ૭", "ધોરણ ૮"
    val division: String = "અ",
    val parentPhone: String = "",
    val qrCode: String = grNo
) {
    val isKumar: Boolean
        get() = gender.equals("KUMAR", ignoreCase = true) || gender == "કુમાર"

    val genderLabel: String
        get() = if (isKumar) "કુમાર (Boy)" else "કન્યા (Girl)"
}
