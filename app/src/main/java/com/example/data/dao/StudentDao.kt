package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Student
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentDao {
    @Query("SELECT * FROM students ORDER BY standard ASC, rollNo ASC")
    fun getAllStudents(): Flow<List<Student>>

    @Query("SELECT * FROM students WHERE standard = :standard ORDER BY rollNo ASC")
    fun getStudentsByStandard(standard: String): Flow<List<Student>>

    @Query("SELECT * FROM students WHERE grNo = :grNo OR qrCode = :grNo LIMIT 1")
    suspend fun getStudentByGrOrQr(grNo: String): Student?

    @Query("SELECT COUNT(*) FROM students")
    fun getStudentCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: Student): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudents(students: List<Student>)

    @Update
    suspend fun updateStudent(student: Student)

    @Delete
    suspend fun deleteStudent(student: Student)

    @Query("SELECT DISTINCT standard FROM students ORDER BY standard ASC")
    fun getAllStandards(): Flow<List<String>>
}
