package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.AttendanceDao
import com.example.data.dao.StudentDao
import com.example.data.model.AttendanceRecord
import com.example.data.model.SchoolStandards
import com.example.data.model.Student
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [Student::class, AttendanceRecord::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun studentDao(): StudentDao
    abstract fun attendanceDao(): AttendanceDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "attendance_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialStudents(database.studentDao())
                    }
                }
            }

            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        // Ensure all 9 standards exist if older database was seeded
                        val count = database.studentDao().getDirectStudentCount()
                        if (count < 10) {
                            populateInitialStudents(database.studentDao())
                        }
                    }
                }
            }
        }

        fun getSampleStudentsList(): listOfStudents {
            return listOf(
                // બાલવાટિકા
                Student(grNo = "BV-101", rollNo = 1, name = "પટેલ આરવ ધર્મેશભાઈ", gender = "KUMAR", standard = SchoolStandards.BALVATIKA, division = "અ", parentPhone = "9825123401"),
                Student(grNo = "BV-102", rollNo = 2, name = "પરમાર પ્રિયાંશી વિજયભાઈ", gender = "KANYA", standard = SchoolStandards.BALVATIKA, division = "અ", parentPhone = "9825123402"),
                Student(grNo = "BV-103", rollNo = 3, name = "સોલંકી વિવાન કલ્પેશભાઈ", gender = "KUMAR", standard = SchoolStandards.BALVATIKA, division = "અ", parentPhone = "9825123403"),
                Student(grNo = "BV-104", rollNo = 4, name = "શાહ દર્શના હસમુખભાઈ", gender = "KANYA", standard = SchoolStandards.BALVATIKA, division = "અ", parentPhone = "9825123404"),

                // ધોરણ ૧
                Student(grNo = "101", rollNo = 1, name = "મકવાણા દક્ષ જીતેન્દ્રભાઈ", gender = "KUMAR", standard = SchoolStandards.STD_1, division = "અ", parentPhone = "9879100101"),
                Student(grNo = "102", rollNo = 2, name = "ગોહિલ ધ્રુવી પંકજભાઈ", gender = "KANYA", standard = SchoolStandards.STD_1, division = "અ", parentPhone = "9879100102"),
                Student(grNo = "103", rollNo = 3, name = "ઝાલા પાર્થ ભરતસિંહ", gender = "KUMAR", standard = SchoolStandards.STD_1, division = "અ", parentPhone = "9879100103"),
                Student(grNo = "104", rollNo = 4, name = "દરજી તન્વી પ્રકાશભાઈ", gender = "KANYA", standard = SchoolStandards.STD_1, division = "અ", parentPhone = "9879100104"),

                // ધોરણ ૨
                Student(grNo = "201", rollNo = 1, name = "ચૌહાણ ક્રીશ અજયભાઈ", gender = "KUMAR", standard = SchoolStandards.STD_2, division = "અ", parentPhone = "9879200201"),
                Student(grNo = "202", rollNo = 2, name = "જોશી કાવ્યા સંજયભાઈ", gender = "KANYA", standard = SchoolStandards.STD_2, division = "અ", parentPhone = "9879200202"),
                Student(grNo = "203", rollNo = 3, name = "વાઘેલા રુદ્ર મનોજભાઈ", gender = "KUMAR", standard = SchoolStandards.STD_2, division = "અ", parentPhone = "9879200203"),
                Student(grNo = "204", rollNo = 4, name = "પંચાલ રીદ્ધિ હરેશભાઈ", gender = "KANYA", standard = SchoolStandards.STD_2, division = "અ", parentPhone = "9879200204"),

                // ધોરણ ૩
                Student(grNo = "301", rollNo = 1, name = "રાઠોડ ઓમ મહેન્દ્રભાઈ", gender = "KUMAR", standard = SchoolStandards.STD_3, division = "અ", parentPhone = "9879300301"),
                Student(grNo = "302", rollNo = 2, name = "ઠાકોર દિયા દિલીપભાઈ", gender = "KANYA", standard = SchoolStandards.STD_3, division = "અ", parentPhone = "9879300302"),
                Student(grNo = "303", rollNo = 3, name = "બારિયા કેવલ જગદીશભાઈ", gender = "KUMAR", standard = SchoolStandards.STD_3, division = "અ", parentPhone = "9879300303"),
                Student(grNo = "304", rollNo = 4, name = "સોની હેતવી શૈલેષભાઈ", gender = "KANYA", standard = SchoolStandards.STD_3, division = "અ", parentPhone = "9879300304"),

                // ધોરણ ૪
                Student(grNo = "401", rollNo = 1, name = "પટેલ શ્લોક વિપુલભાઈ", gender = "KUMAR", standard = SchoolStandards.STD_4, division = "અ", parentPhone = "9879400401"),
                Student(grNo = "402", rollNo = 2, name = "ચાવડા માનસી નરેશભાઈ", gender = "KANYA", standard = SchoolStandards.STD_4, division = "અ", parentPhone = "9879400402"),
                Student(grNo = "403", rollNo = 3, name = "પ્રજાપતિ યશ કિરીટભાઈ", gender = "KUMAR", standard = SchoolStandards.STD_4, division = "અ", parentPhone = "9879400403"),
                Student(grNo = "404", rollNo = 4, name = "વ્યાસ ખુશ્બુ પ્રફુલભાઈ", gender = "KANYA", standard = SchoolStandards.STD_4, division = "અ", parentPhone = "9879400404"),

                // ધોરણ ૫
                Student(grNo = "1001", rollNo = 1, name = "પટેલ આયુષ વિનોદભાઈ", gender = "KUMAR", standard = SchoolStandards.STD_5, division = "અ", parentPhone = "9876543210"),
                Student(grNo = "1002", rollNo = 2, name = "પરમાર દિયા હિતેશભાઈ", gender = "KANYA", standard = SchoolStandards.STD_5, division = "અ", parentPhone = "9876543211"),
                Student(grNo = "1003", rollNo = 3, name = "સોલંકી હર્ષ સુરેશભાઈ", gender = "KUMAR", standard = SchoolStandards.STD_5, division = "અ", parentPhone = "9876543212"),
                Student(grNo = "1004", rollNo = 4, name = "શાહ ખુશી મુકેશભાઈ", gender = "KANYA", standard = SchoolStandards.STD_5, division = "અ", parentPhone = "9876543213"),
                Student(grNo = "1005", rollNo = 5, name = "પંચાલ મનન દિનેશભાઈ", gender = "KUMAR", standard = SchoolStandards.STD_5, division = "અ", parentPhone = "9876543214"),
                Student(grNo = "1006", rollNo = 6, name = "ઝાલા પ્રિયા કનુભાઈ", gender = "KANYA", standard = SchoolStandards.STD_5, division = "અ", parentPhone = "9876543215"),

                // ધોરણ - ૬
                Student(grNo = "1007", rollNo = 1, name = "મકવાણા રોહન રાજેશભાઈ", gender = "KUMAR", standard = SchoolStandards.STD_6, division = "અ", parentPhone = "9876543216"),
                Student(grNo = "1008", rollNo = 2, name = "વાઘેલા સાક્ષી વિપુલભાઈ", gender = "KANYA", standard = SchoolStandards.STD_6, division = "અ", parentPhone = "9876543217"),
                Student(grNo = "1009", rollNo = 3, name = "ઠાકોર તન્મય મહેન્દ્રભાઈ", gender = "KUMAR", standard = SchoolStandards.STD_6, division = "અ", parentPhone = "9876543218"),
                Student(grNo = "1010", rollNo = 4, name = "ગોહિલ ઊર્મિ અશોકભાઈ", gender = "KANYA", standard = SchoolStandards.STD_6, division = "અ", parentPhone = "9876543219"),
                Student(grNo = "1011", rollNo = 5, name = "બારિયા વિવેક શૈલેષભાઈ", gender = "KUMAR", standard = SchoolStandards.STD_6, division = "અ", parentPhone = "9876543220"),
                Student(grNo = "1012", rollNo = 6, name = "સોની વિધિ કમલેશભાઈ", gender = "KANYA", standard = SchoolStandards.STD_6, division = "અ", parentPhone = "9876543221"),

                // ધોરણ ૭
                Student(grNo = "1013", rollNo = 1, name = "જોશી દેવ જગદીશભાઈ", gender = "KUMAR", standard = SchoolStandards.STD_7, division = "અ", parentPhone = "9876543222"),
                Student(grNo = "1014", rollNo = 2, name = "ચૌહાણ નિધિ પ્રવીણભાઈ", gender = "KANYA", standard = SchoolStandards.STD_7, division = "અ", parentPhone = "9876543223"),
                Student(grNo = "1015", rollNo = 3, name = "દવે પાર્થ સંજયભાઈ", gender = "KUMAR", standard = SchoolStandards.STD_7, division = "અ", parentPhone = "9876543224"),
                Student(grNo = "1016", rollNo = 4, name = "રાઠોડ પૂજા નરેશભાઈ", gender = "KANYA", standard = SchoolStandards.STD_7, division = "અ", parentPhone = "9876543225"),

                // ધોરણ ૮
                Student(grNo = "1017", rollNo = 1, name = "વ્યાસ ધ્રુવ અશ્વિનભાઈ", gender = "KUMAR", standard = SchoolStandards.STD_8, division = "અ", parentPhone = "9876543226"),
                Student(grNo = "1018", rollNo = 2, name = "પ્રજાપતિ અંજલિ ભાવેશભાઈ", gender = "KANYA", standard = SchoolStandards.STD_8, division = "અ", parentPhone = "9876543227"),
                Student(grNo = "1019", rollNo = 3, name = "પટેલ કૃષ્ણ જિજ્ઞેશભાઈ", gender = "KUMAR", standard = SchoolStandards.STD_8, division = "અ", parentPhone = "9876543228"),
                Student(grNo = "1020", rollNo = 4, name = "ચાવડા દૃષ્ટિ ભરતભાઈ", gender = "KANYA", standard = SchoolStandards.STD_8, division = "અ", parentPhone = "9876543229")
            )
        }

        suspend fun populateInitialStudents(studentDao: StudentDao) {
            val sampleStudents = getSampleStudentsList()
            studentDao.insertStudents(sampleStudents)
        }
    }
}

typealias listOfStudents = List<Student>
