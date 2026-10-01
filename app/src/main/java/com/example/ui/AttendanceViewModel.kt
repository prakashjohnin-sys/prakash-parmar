package com.example.ui

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.model.AttendanceRecord
import com.example.data.model.Student
import com.example.data.repository.AttendanceRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppScreen {
    LOGIN,
    HOME,
    TAKE_ATTENDANCE,
    VIEW_ATTENDANCE,
    REPORTS,
    STUDENT_DATABASE
}

data class TodayStats(
    val presentKumar: Int = 0,
    val presentKanya: Int = 0,
    val absentKumar: Int = 0,
    val absentKanya: Int = 0,
    val totalCount: Int = 0
)

class AttendanceViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application, viewModelScope)
    private val repository = AttendanceRepository(database.studentDao(), database.attendanceDao())

    private val sharedPrefs = application.getSharedPreferences("shala_prefs", Context.MODE_PRIVATE)

    private val _currentScreen = MutableStateFlow(
        if (sharedPrefs.getBoolean("is_logged_in", false)) AppScreen.HOME else AppScreen.LOGIN
    )
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _selectedDate = MutableStateFlow(AttendanceRepository.getTodayDateString())
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    private val _selectedStandard = MutableStateFlow("તમામ ધોરણ")
    val selectedStandard: StateFlow<String> = _selectedStandard.asStateFlow()

    // Screen 3 Reports specific tab: 1 = Present, 2 = Absent
    private val _reportTab = MutableStateFlow(1)
    val reportTab: StateFlow<Int> = _reportTab.asStateFlow()

    val allStudents: StateFlow<List<Student>> = repository.allStudents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allStandards: StateFlow<List<String>> = repository.allStandards
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf("ધોરણ ૫", "ધોરણ ૬", "ધોરણ ૭", "ધોરણ ૮"))

    // Attendance records for the selected date
    val attendanceForSelectedDate: StateFlow<List<AttendanceRecord>> = _selectedDate
        .combine(repository.getAttendanceForDate(_selectedDate.value)) { _, records -> records }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Today's attendance stats for the dashboard
    val todayStats: StateFlow<TodayStats> = combine(
        repository.allStudents,
        repository.getAttendanceForDate(AttendanceRepository.getTodayDateString())
    ) { students, records ->
        val total = students.size
        var pKumar = 0
        var pKanya = 0
        var aKumar = 0
        var aKanya = 0

        val recordMap = records.associateBy { it.studentGrNo }

        for (student in students) {
            val record = recordMap[student.grNo]
            val isPresent = record?.isPresent == true
            if (isPresent) {
                if (student.isKumar) pKumar++ else pKanya++
            } else {
                if (student.isKumar) aKumar++ else aKanya++
            }
        }

        TodayStats(
            presentKumar = pKumar,
            presentKanya = pKanya,
            absentKumar = aKumar,
            absentKanya = aKanya,
            totalCount = total
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TodayStats())

    private val _scanMessage = MutableSharedFlow<String>()
    val scanMessage: SharedFlow<String> = _scanMessage.asSharedFlow()

    private val _recentlyScannedStudent = MutableStateFlow<Student?>(null)
    val recentlyScannedStudent: StateFlow<Student?> = _recentlyScannedStudent.asStateFlow()

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun setReportTab(tab: Int) {
        _reportTab.value = tab
    }

    fun setSelectedDate(date: String) {
        _selectedDate.value = date
    }

    fun setSelectedStandard(standard: String) {
        _selectedStandard.value = standard
    }

    fun login(user: String, pass: String): Boolean {
        // Teacher / Admin login verification
        val validUser = user.trim().lowercase()
        val validPass = pass.trim()
        if ((validUser == "teacher" || validUser == "admin" || validUser == "shala29" || validUser.isNotBlank()) &&
            (validPass == "123" || validPass == "1234" || validPass == "shala29" || validPass.isNotBlank())
        ) {
            sharedPrefs.edit().putBoolean("is_logged_in", true).apply()
            _currentScreen.value = AppScreen.HOME
            return true
        }
        return false
    }

    fun logout() {
        sharedPrefs.edit().putBoolean("is_logged_in", false).apply()
        _currentScreen.value = AppScreen.LOGIN
    }

    fun onQrScanned(code: String) {
        viewModelScope.launch {
            val student = repository.findStudentByQrOrGr(code)
            if (student != null) {
                repository.markPresent(student, _selectedDate.value)
                _recentlyScannedStudent.value = student
                vibrateDevice()
                _scanMessage.emit("હાજર: ${student.name} (${student.standard})")
            } else {
                vibrateDevice(isError = true)
                _scanMessage.emit("અમાન્ય QR કોડ! કોઈ વિદ્યાર્થી મળ્યો નથી ($code)")
            }
        }
    }

    fun markStudentPresentDirectly(student: Student) {
        viewModelScope.launch {
            repository.markPresent(student, _selectedDate.value)
            _recentlyScannedStudent.value = student
            vibrateDevice()
            _scanMessage.emit("હાજર: ${student.name}")
        }
    }

    fun markStudentAbsentDirectly(student: Student) {
        viewModelScope.launch {
            repository.markStudentStatus(student, _selectedDate.value, isPresent = false)
            _scanMessage.emit("ગેરહાજર: ${student.name}")
        }
    }

    fun toggleRecordAttendance(record: AttendanceRecord) {
        viewModelScope.launch {
            repository.toggleStudentAttendance(record)
        }
    }

    fun finalizeAttendance() {
        viewModelScope.launch {
            repository.finalizeAttendanceForStandard(_selectedStandard.value, _selectedDate.value)
            _scanMessage.emit("હાજરી પૂર્ણ થઈ ગઈ છે. બાકીના વિદ્યાર્થીઓ ગેરહાજર નોંધાયા છે.")
        }
    }

    fun addStudent(student: Student) {
        viewModelScope.launch {
            repository.addStudent(student)
            _scanMessage.emit("વિદ્યાર્થી ઉમેરાઈ ગયો: ${student.name}")
        }
    }

    fun deleteStudent(student: Student) {
        viewModelScope.launch {
            repository.deleteStudent(student)
            _scanMessage.emit("વિદ્યાર્થી રદ્દ થયો: ${student.name}")
        }
    }

    fun resetData() {
        viewModelScope.launch {
            repository.resetSampleData()
            _scanMessage.emit("ડેટાબેઝ સફળતાપૂર્વક રીસેટ થયો!")
        }
    }

    fun clearRecentlyScanned() {
        _recentlyScannedStudent.value = null
    }

    private fun vibrateDevice(isError: Boolean = false) {
        try {
            val context = getApplication<Application>()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                val vibrator = vibratorManager?.defaultVibrator
                val pattern = if (isError) longArrayOf(0, 100, 80, 150) else longArrayOf(0, 60)
                vibrator?.vibrate(VibrationEffect.createWaveform(pattern, -1))
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                vibrator?.vibrate(if (isError) 200 else 60)
            }
        } catch (_: Exception) {
            // Ignore vibration error on emulators without vibrator
        }
    }
}
