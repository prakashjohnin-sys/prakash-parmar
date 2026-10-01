package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "attendance_records",
    indices = [Index(value = ["date", "studentGrNo"], unique = true)]
)
data class AttendanceRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String, // Format: "yyyy-MM-dd"
    val studentGrNo: String,
    val studentName: String,
    val studentGender: String, // "KUMAR" or "KANYA"
    val standard: String,
    val division: String = "અ",
    val isPresent: Boolean, // true = હાજર, false = ગેરહાજર
    val scannedAt: Long = System.currentTimeMillis()
) {
    val isKumar: Boolean
        get() = studentGender.equals("KUMAR", ignoreCase = true) || studentGender == "કુમાર"
}
