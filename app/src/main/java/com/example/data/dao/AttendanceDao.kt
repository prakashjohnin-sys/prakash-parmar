package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.AttendanceRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceDao {
    @Query("SELECT * FROM attendance_records WHERE date = :date ORDER BY standard ASC, studentName ASC")
    fun getAttendanceForDate(date: String): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records WHERE date = :date AND standard = :standard ORDER BY studentName ASC")
    fun getAttendanceForDateAndStandard(date: String, standard: String): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records WHERE date = :date AND studentGrNo = :studentGrNo LIMIT 1")
    suspend fun getRecord(date: String, studentGrNo: String): AttendanceRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(record: AttendanceRecord)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateAll(records: List<AttendanceRecord>)

    @Query("DELETE FROM attendance_records WHERE date = :date AND standard = :standard")
    suspend fun deleteForDateAndStandard(date: String, standard: String)

    @Query("DELETE FROM attendance_records WHERE date = :date")
    suspend fun deleteForDate(date: String)
}
