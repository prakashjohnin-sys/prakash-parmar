package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.AttendanceDao
import com.example.data.dao.StudentDao
import com.example.data.model.AttendanceRecord
import com.example.data.model.Student
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [Student::class, AttendanceRecord::class],
    version = 1,
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
        }

        suspend fun populateInitialStudents(studentDao: StudentDao) {
            val sampleStudents = listOf(
                // ધોરણ ૫
                Student(grNo = "1001", rollNo = 1, name = "પટેલ આયુષ વિનોદભાઈ", gender = "KUMAR", standard = "ધોરણ ૫", division = "અ", parentPhone = "9876543210"),
                Student(grNo = "1002", rollNo = 2, name = "પરમાર દિયા હિતેશભાઈ", gender = "KANYA", standard = "ધોરણ ૫", division = "અ", parentPhone = "9876543211"),
                Student(grNo = "1003", rollNo = 3, name = "સોલંકી હર્ષ સુરેશભાઈ", gender = "KUMAR", standard = "ધોરણ ૫", division = "અ", parentPhone = "9876543212"),
                Student(grNo = "1004", rollNo = 4, name = "શાહ ખુશી મુકેશભાઈ", gender = "KANYA", standard = "ધોરણ ૫", division = "અ", parentPhone = "9876543213"),
                Student(grNo = "1005", rollNo = 5, name = "પંચાલ મનન દિનેશભાઈ", gender = "KUMAR", standard = "ધોરણ ૫", division = "અ", parentPhone = "9876543214"),
                Student(grNo = "1006", rollNo = 6, name = "ઝાલા પ્રિયા કનુભાઈ", gender = "KANYA", standard = "ધોરણ ૫", division = "અ", parentPhone = "9876543215"),
                
                // ધોરણ ૬
                Student(grNo = "1007", rollNo = 1, name = "મકવાણા રોહન રાજેશભાઈ", gender = "KUMAR", standard = "ધોરણ ૬", division = "અ", parentPhone = "9876543216"),
                Student(grNo = "1008", rollNo = 2, name = "વાઘેલા સાક્ષી વિપુલભાઈ", gender = "KANYA", standard = "ધોરણ ૬", division = "અ", parentPhone = "9876543217"),
                Student(grNo = "1009", rollNo = 3, name = "ઠાકોર તન્મય મહેન્દ્રભાઈ", gender = "KUMAR", standard = "ધોરણ ૬", division = "અ", parentPhone = "9876543218"),
                Student(grNo = "1010", rollNo = 4, name = "ગોહિલ ઊર્મિ અશોકભાઈ", gender = "KANYA", standard = "ધોરણ ૬", division = "અ", parentPhone = "9876543219"),
                Student(grNo = "1011", rollNo = 5, name = "બારિયા વિવેક શૈલેષભાઈ", gender = "KUMAR", standard = "ધોરણ ૬", division = "અ", parentPhone = "9876543220"),
                Student(grNo = "1012", rollNo = 6, name = "સોની વિધિ કમલેશભાઈ", gender = "KANYA", standard = "ધોરણ ૬", division = "અ", parentPhone = "9876543221"),

                // ધોરણ ૭
                Student(grNo = "1013", rollNo = 1, name = "જોશી દેવ જગદીશભાઈ", gender = "KUMAR", standard = "ધોરણ ૭", division = "અ", parentPhone = "9876543222"),
                Student(grNo = "1014", rollNo = 2, name = "ચૌહાણ નિધિ પ્રવીણભાઈ", gender = "KANYA", standard = "ધોરણ ૭", division = "અ", parentPhone = "9876543223"),
                Student(grNo = "1015", rollNo = 3, name = "દવે પાર્થ સંજયભાઈ", gender = "KUMAR", standard = "ધોરણ ૭", division = "અ", parentPhone = "9876543224"),
                Student(grNo = "1016", rollNo = 4, name = "રાઠોડ પૂજા નરેશભાઈ", gender = "KANYA", standard = "ધોરણ ૭", division = "અ", parentPhone = "9876543225"),

                // ધોરણ ૮
                Student(grNo = "1017", rollNo = 1, name = "વ્યાસ ધ્રુવ અશ્વિનભાઈ", gender = "KUMAR", standard = "ધોરણ ૮", division = "અ", parentPhone = "9876543226"),
                Student(grNo = "1018", rollNo = 2, name = "પ્રજાપતિ અંજલિ ભાવેશભાઈ", gender = "KANYA", standard = "ધોરણ ૮", division = "અ", parentPhone = "9876543227"),
                Student(grNo = "1019", rollNo = 3, name = "પટેલ કૃષ્ણ જિજ્ઞેશભાઈ", gender = "KUMAR", standard = "ધોરણ ૮", division = "અ", parentPhone = "9876543228"),
                Student(grNo = "1020", rollNo = 4, name = "ચાવડા દૃષ્ટિ ભરતભાઈ", gender = "KANYA", standard = "ધોરણ ૮", division = "અ", parentPhone = "9876543229")
            )
            studentDao.insertStudents(sampleStudents)
        }
    }
}
