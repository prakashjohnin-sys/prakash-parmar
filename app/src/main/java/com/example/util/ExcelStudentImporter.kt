package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.model.SchoolStandards
import com.example.data.model.Student
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

object ExcelStudentImporter {

    private const val UTF8_BOM = "\uFEFF"

    /**
     * શાળાઓ માટે એક્સેલ (Excel) / CSV નમૂનો (Template)
     * UTF-8 BOM સાથે જેથી MS Excel માં ગુજરાતી ફોન્ટ સ્પષ્ટ વંચાય.
     */
    fun generateTemplateCsv(): String {
        val header = "રોલ નં,GR નં,વિધ્યાર્થીનું પૂરું નામ,ધોરણ,જાતિ,વાલીનો મોબાઈલ નંબર"
        val sampleRows = listOf(
            "1,101,પટેલ આરવ ધર્મેશભાઈ,બાલવાટિકા,કુમાર,9825123401",
            "2,102,પરમાર પ્રિયાંશી વિજયભાઈ,બાલવાટિકા,કન્યા,9825123402",
            "1,201,મકવાણા દક્ષ જીતેન્દ્રભાઈ,ધોરણ ૧,કુમાર,9879100101",
            "2,202,ગોહિલ ધ્રુવી પંકજભાઈ,ધોરણ ૧,કન્યા,9879100102",
            "1,301,ચૌહાણ ક્રીશ અજયભાઈ,ધોરણ ૨,કુમાર,9879200201",
            "2,302,જોશી કાવ્યા સંજયભાઈ,ધોરણ ૨,કન્યા,9879200202",
            "1,401,રાઠોડ ઓમ મહેન્દ્રભાઈ,ધોરણ ૩,કુમાર,9879300301",
            "1,501,પટેલ શ્લોક વિપુલભાઈ,ધોરણ ૪,કુમાર,9879400401",
            "1,601,પટેલ આયુષ વિનોદભાઈ,ધોરણ ૫,કુમાર,9876543210",
            "2,602,પરમાર દિયા હિતેશભાઈ,ધોરણ ૫,કન્યા,9876543211",
            "1,701,મકવાણા રોહન રાજેશભાઈ,ધોરણ ૬,કુમાર,9876543216",
            "2,702,વાઘેલા સાક્ષી વિપુલભાઈ,ધોરણ ૬,કન્યા,9876543217",
            "1,801,જોશી દેવ જગદીશભાઈ,ધોરણ ૭,કુમાર,9876543222",
            "1,901,વ્યાસ ધ્રુવ અશ્વિનભાઈ,ધોરણ ૮,કુમાર,9876543226"
        )
        return UTF8_BOM + header + "\n" + sampleRows.joinToString("\n")
    }

    /**
     * વિદ્યાર્થીઓની યાદીને એક્સેલ / CSV ફોર્મેટમાં કન્વર્ટ કરે છે
     */
    fun exportStudentsToCsv(students: List<Student>): String {
        val header = "રોલ નં,GR નં,વિધ્યાર્થીનું પૂરું નામ,ધોરણ,જાતિ,વાલીનો મોબાઈલ નંબર"
        val rows = students.map { student ->
            val cleanName = escapeCsv(student.name)
            "${student.rollNo},${student.grNo},$cleanName,${student.standard},${student.genderLabel},${student.parentPhone}"
        }
        return UTF8_BOM + header + "\n" + rows.joinToString("\n")
    }

    /**
     * એક્સેલ / CSV ફાઇલ સેવ કરીને શેર કરે છે
     */
    fun shareCsvFile(context: Context, csvContent: String, fileName: String): Boolean {
        return try {
            val file = File(context.cacheDir, fileName)
            file.writeText(csvContent, Charsets.UTF_8)

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "વિદ્યાર્થી ડેટા - નગર પ્રાથમિક શાળા નં ૨૯")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(intent, "Excel / CSV ફાઇલ શેર કરો"))
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * અપલોડ કરેલી Excel / CSV ફાઇલ (URI) માંથી વિદ્યાર્થીઓ વાંચે છે
     */
    fun parseStudentsFromUri(context: Context, uri: Uri): Result<List<Student>> {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return Result.failure(Exception("ફાઇલ ખોલી શકાઈ નથી"))

            val reader = BufferedReader(InputStreamReader(inputStream, StandardCharsets.UTF_8))
            val lines = mutableListOf<String>()
            var line = reader.readLine()
            while (line != null) {
                if (line.isNotBlank()) {
                    lines.add(line)
                }
                line = reader.readLine()
            }
            reader.close()

            parseStudentsFromLines(lines)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * સીધા ટેક્સ્ટ / પેસ્ટ કરેલા CSV માંથી વિદ્યાર્થીઓ વાંચે છે
     */
    fun parseStudentsFromText(text: String): Result<List<Student>> {
        val lines = text.lines().filter { it.isNotBlank() }
        return parseStudentsFromLines(lines)
    }

    private fun parseStudentsFromLines(lines: List<String>): Result<List<Student>> {
        if (lines.isEmpty()) {
            return Result.failure(Exception("ફાઇલમાં કોઈ માહિતી મળી નથી"))
        }

        val firstLine = lines.first().removePrefix(UTF8_BOM).trim()
        val delimiter = detectDelimiter(firstLine)

        val headerTokens = parseCsvLine(firstLine, delimiter).map { it.trim().lowercase() }
        val hasHeader = isHeaderRow(headerTokens)

        val dataLines = if (hasHeader) lines.drop(1) else lines

        // Detect column positions
        var colRoll = -1
        var colGr = -1
        var colName = -1
        var colStd = -1
        var colGender = -1
        var colPhone = -1

        if (hasHeader) {
            headerTokens.forEachIndexed { index, rawHeader ->
                val h = rawHeader.lowercase()
                when {
                    colRoll == -1 && (h.contains("રોલ") || h.contains("roll")) -> colRoll = index
                    colGr == -1 && (h.contains("gr") || h.contains("જીઆર") || h.contains("જી.આર")) -> colGr = index
                    colName == -1 && (h.contains("નામ") || h.contains("વિદ્યાર્થી") || h.contains("વિધ્યાર્થી") || h.contains("name")) -> colName = index
                    colStd == -1 && (h.contains("ધોરણ") || h.contains("std") || h.contains("class")) -> colStd = index
                    colGender == -1 && (h.contains("જાતિ") || h.contains("લિંગ") || h.contains("gender") || h.contains("sex")) -> colGender = index
                    colPhone == -1 && (h.contains("મોબાઈલ") || h.contains("મોબાઇલ") || h.contains("ફોન") || h.contains("mobile") || h.contains("phone")) -> colPhone = index
                }
            }
        }

        // Fallbacks if columns could not be mapped by header names
        if (colRoll == -1) colRoll = 0
        if (colGr == -1) colGr = 1
        if (colName == -1) colName = 2
        if (colStd == -1) colStd = 3
        if (colGender == -1) colGender = 4
        if (colPhone == -1) colPhone = 5

        val students = mutableListOf<Student>()
        var autoRoll = 1

        for ((index, rawLine) in dataLines.withIndex()) {
            val clean = rawLine.removePrefix(UTF8_BOM).trim()
            if (clean.isBlank()) continue

            val tokens = parseCsvLine(clean, delimiter)
            if (tokens.isEmpty()) continue

            // Extract Name
            val rawName = tokens.getOrNull(colName)?.trim().orEmpty()
            if (rawName.isBlank()) continue

            // Extract Roll No
            val parsedRoll = tokens.getOrNull(colRoll)?.trim()?.toIntOrNull()
            val rollNo = parsedRoll ?: autoRoll++

            // Extract GR No
            val rawGr = tokens.getOrNull(colGr)?.trim().orEmpty()
            val grNo = if (rawGr.isNotBlank()) rawGr else "GR-${1000 + index + 1}"

            // Extract Standard
            val rawStd = tokens.getOrNull(colStd)?.trim().orEmpty()
            val standard = normalizeStandard(rawStd)

            // Extract Gender
            val rawGender = tokens.getOrNull(colGender)?.trim().orEmpty()
            val gender = normalizeGender(rawGender)

            // Extract Phone
            val rawPhone = tokens.getOrNull(colPhone)?.trim().orEmpty()
            val parentPhone = sanitizePhone(rawPhone)

            students.add(
                Student(
                    grNo = grNo,
                    rollNo = rollNo,
                    name = rawName,
                    gender = gender,
                    standard = standard,
                    division = "અ",
                    parentPhone = parentPhone,
                    qrCode = grNo
                )
            )
        }

        return if (students.isNotEmpty()) {
            Result.success(students)
        } else {
            Result.failure(Exception("યોગ્ય વિદ્યાર્થી રેકોર્ડ મળી શક્યા નથી. કૃપા કરીને નમૂનો (Template) ચકાસો."))
        }
    }

    private fun detectDelimiter(line: String): Char {
        val commaCount = line.count { it == ',' }
        val tabCount = line.count { it == '\t' }
        val semiCount = line.count { it == ';' }
        return when {
            tabCount > commaCount && tabCount > semiCount -> '\t'
            semiCount > commaCount && semiCount > tabCount -> ';'
            else -> ','
        }
    }

    private fun isHeaderRow(tokens: List<String>): Boolean {
        val joined = tokens.joinToString(" ")
        return joined.contains("નામ") || joined.contains("ધોરણ") || joined.contains("રોલ") ||
                joined.contains("name") || joined.contains("standard") || joined.contains("roll")
    }

    private fun parseCsvLine(line: String, delimiter: Char): List<String> {
        val result = mutableListOf<String>()
        val sb = java.lang.StringBuilder()
        var inQuotes = false

        for (ch in line) {
            when {
                ch == '\"' -> inQuotes = !inQuotes
                ch == delimiter && !inQuotes -> {
                    result.add(sb.toString().trim())
                    sb.setLength(0)
                }
                else -> sb.append(ch)
            }
        }
        result.add(sb.toString().trim())
        return result
    }

    private fun escapeCsv(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"" + value.replace("\"", "\"\"") + "\""
        } else {
            value
        }
    }

    private fun normalizeStandard(input: String): String {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return SchoolStandards.STD_1

        if (trimmed.contains("બાલ") || trimmed.contains("bal", ignoreCase = true)) {
            return SchoolStandards.BALVATIKA
        }

        // Match Gujarati or English standard numbers
        return when {
            trimmed.contains("1") || trimmed.contains("૧") -> SchoolStandards.STD_1
            trimmed.contains("2") || trimmed.contains("૨") -> SchoolStandards.STD_2
            trimmed.contains("3") || trimmed.contains("૩") -> SchoolStandards.STD_3
            trimmed.contains("4") || trimmed.contains("૪") -> SchoolStandards.STD_4
            trimmed.contains("5") || trimmed.contains("૫") -> SchoolStandards.STD_5
            trimmed.contains("6") || trimmed.contains("૬") -> SchoolStandards.STD_6
            trimmed.contains("7") || trimmed.contains("૭") -> SchoolStandards.STD_7
            trimmed.contains("8") || trimmed.contains("૮") -> SchoolStandards.STD_8
            else -> {
                // If it already matches one of the standards, return it
                SchoolStandards.ALL.firstOrNull { it.equals(trimmed, ignoreCase = true) } ?: SchoolStandards.STD_1
            }
        }
    }

    private fun normalizeGender(input: String): String {
        val lower = input.trim().lowercase()
        return when {
            lower.contains("કન્યા") || lower.contains("girl") || lower.contains("f") || lower.contains("female") -> "KANYA"
            else -> "KUMAR"
        }
    }

    private fun sanitizePhone(input: String): String {
        return input.filter { it.isDigit() }.takeLast(10)
    }
}
