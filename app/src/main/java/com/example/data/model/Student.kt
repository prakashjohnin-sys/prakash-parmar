package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

object SchoolStandards {
    val BALVATIKA = "બાલવાટિકા"
    val STD_1 = "ધોરણ ૧"
    val STD_2 = "ધોરણ ૨"
    val STD_3 = "ધોરણ ૩"
    val STD_4 = "ધોરણ ૪"
    val STD_5 = "ધોરણ ૫"
    val STD_6 = "ધોરણ ૬"
    val STD_7 = "ધોરણ ૭"
    val STD_8 = "ધોરણ ૮"

    val ALL = listOf(
        BALVATIKA,
        STD_1,
        STD_2,
        STD_3,
        STD_4,
        STD_5,
        STD_6,
        STD_7,
        STD_8
    )

    fun normalize(standard: String): String {
        return when (standard.trim()) {
            "ધોરણ ૬", "ધોરણ - ૬" -> STD_6
            else -> standard.trim()
        }
    }
}

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
    val standard: String, // "બાલવાટિકા", "ધોરણ ૧", ..., "ધોરણ ૮"
    val division: String = "અ",
    val parentPhone: String = "",
    val qrCode: String = grNo
) {
    val isKumar: Boolean
        get() = gender.equals("KUMAR", ignoreCase = true) || gender == "કુમાર"

    val genderLabel: String
        get() = if (isKumar) "કુમાર" else "કન્યા"
}
