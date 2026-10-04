package com.example.data.repository

import com.example.data.AppDatabase
import com.example.data.dao.AttendanceDao
import com.example.data.dao.StudentDao
import com.example.data.model.AttendanceRecord
import com.example.data.model.Student
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AttendanceRepository(
    private val studentDao: StudentDao,
    private val attendanceDao: AttendanceDao
) {
    val allStudents: Flow<List<Student>> = studentDao.getAllStudents()
    val allStandards: Flow<List<String>> = kotlinx.coroutines.flow.flow {
        studentDao.getAllStandards().collect { dbStandards ->
            val combined = (com.example.data.model.SchoolStandards.ALL + dbStandards).distinct()
            emit(combined)
        }
    }
    val totalStudentsCount: Flow<Int> = studentDao.getStudentCount()

    fun getAttendanceForDate(date: String): Flow<List<AttendanceRecord>> =
        attendanceDao.getAttendanceForDate(date)

    fun getAttendanceForDateAndStandard(date: String, standard: String): Flow<List<AttendanceRecord>> =
        attendanceDao.getAttendanceForDateAndStandard(date, standard)

    suspend fun findStudentByQrOrGr(code: String): Student? {
        val cleanCode = code.trim()
        // If QR code is in format "STUDENT:1001" or JSON, extract the GR number
        val grNumber = if (cleanCode.startsWith("STUDENT:", ignoreCase = true)) {
            cleanCode.substringAfter("STUDENT:")
        } else {
            cleanCode
        }
        return studentDao.getStudentByGrOrQr(grNumber) ?: studentDao.getStudentByGrOrQr(cleanCode)
    }

    suspend fun markPresent(student: Student, date: String): AttendanceRecord {
        val existing = attendanceDao.getRecord(date, student.grNo)
        val record = AttendanceRecord(
            id = existing?.id ?: 0,
            date = date,
            studentGrNo = student.grNo,
            studentName = student.name,
            studentGender = student.gender,
            standard = student.standard,
            division = student.division,
            isPresent = true,
            scannedAt = System.currentTimeMillis()
        )
        attendanceDao.insertOrUpdate(record)
        return record
    }

    suspend fun toggleStudentAttendance(record: AttendanceRecord) {
        val updated = record.copy(isPresent = !record.isPresent)
        attendanceDao.insertOrUpdate(updated)
    }

    suspend fun markStudentStatus(student: Student, date: String, isPresent: Boolean) {
        val existing = attendanceDao.getRecord(date, student.grNo)
        val record = AttendanceRecord(
            id = existing?.id ?: 0,
            date = date,
            studentGrNo = student.grNo,
            studentName = student.name,
            studentGender = student.gender,
            standard = student.standard,
            division = student.division,
            isPresent = isPresent,
            scannedAt = System.currentTimeMillis()
        )
        attendanceDao.insertOrUpdate(record)
    }

    suspend fun finalizeAttendanceForStandard(standard: String, date: String) {
        val students = if (standard == "તમામ ધોરણ (All)" || standard.isBlank()) {
            studentDao.getAllStudents().first()
        } else {
            studentDao.getStudentsByStandard(standard).first()
        }

        val existingRecords = attendanceDao.getAttendanceForDate(date).first()
            .associateBy { it.studentGrNo }

        val recordsToSave = mutableListOf<AttendanceRecord>()
        for (student in students) {
            val existing = existingRecords[student.grNo]
            if (existing == null) {
                // Not scanned -> automatically marked as Absent (ગેરહાજર)
                recordsToSave.add(
                    AttendanceRecord(
                        date = date,
                        studentGrNo = student.grNo,
                        studentName = student.name,
                        studentGender = student.gender,
                        standard = student.standard,
                        division = student.division,
                        isPresent = false,
                        scannedAt = System.currentTimeMillis()
                    )
                )
            }
        }
        if (recordsToSave.isNotEmpty()) {
            attendanceDao.insertOrUpdateAll(recordsToSave)
        }
    }

    suspend fun addStudent(student: Student): Long {
        return studentDao.insertStudent(student)
    }

    suspend fun updateStudent(student: Student) {
        studentDao.updateStudent(student)
    }

    suspend fun deleteStudent(student: Student) {
        studentDao.deleteStudent(student)
    }

    suspend fun importStudents(students: List<Student>, replaceExisting: Boolean) {
        if (replaceExisting) {
            studentDao.clearAllStudents()
        }
        studentDao.insertStudents(students)
    }

    suspend fun resetSampleData() {
        studentDao.clearAllStudents()
        AppDatabase.populateInitialStudents(studentDao)
    }

    companion object {
        fun getTodayDateString(): String {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            return sdf.format(Date())
        }

        fun getDisplayDateString(dateStr: String): String {
            return try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val parsed = inputFormat.parse(dateStr)
                if (parsed != null) outputFormat.format(parsed) else dateStr
            } catch (_: Exception) {
                dateStr
            }
        }
    }
}
