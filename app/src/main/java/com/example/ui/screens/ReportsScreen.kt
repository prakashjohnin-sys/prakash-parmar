package com.example.ui.screens

import android.app.DatePickerDialog
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HighlightOff
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SchoolStandards
import com.example.data.model.Student
import com.example.data.repository.AttendanceRepository
import com.example.ui.AppScreen
import com.example.ui.AttendanceViewModel
import com.example.ui.components.SchoolLogoBadge
import com.example.ui.theme.AbsentRed
import com.example.ui.theme.AbsentRedBg
import com.example.ui.theme.BackgroundLight
import com.example.ui.theme.BoyBlue
import com.example.ui.theme.GirlPink
import com.example.ui.theme.PresentGreen
import com.example.ui.theme.PresentGreenBg
import com.example.ui.theme.SchoolPrimary
import com.example.ui.theme.TextPrimaryLight
import com.example.ui.theme.TextSecondaryLight
import com.example.util.CommunicationUtil
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    viewModel: AttendanceViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.navigateTo(AppScreen.HOME)
    }

    val context = LocalContext.current

    val allStudents by viewModel.allStudents.collectAsState()
    val selectedStandard by viewModel.selectedStandard.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val attendanceRecords by viewModel.attendanceForSelectedDate.collectAsState()
    val reportTab by viewModel.reportTab.collectAsState()

    var studentToMessage by remember { mutableStateOf<Student?>(null) }
    var customMessageText by remember { mutableStateOf("") }

    val recordMap = remember(attendanceRecords) {
        attendanceRecords.associateBy { it.studentGrNo }
    }

    // Filter students by selected standard
    val filteredStudents = remember(allStudents, selectedStandard) {
        if (selectedStandard == "તમામ ધોરણ" || selectedStandard.isBlank()) {
            allStudents
        } else {
            allStudents.filter { it.standard == selectedStandard }
        }
    }

    val presentStudents = remember(filteredStudents, recordMap) {
        filteredStudents.filter { recordMap[it.grNo]?.isPresent == true }
    }

    val absentStudents = remember(filteredStudents, recordMap) {
        filteredStudents.filter { recordMap[it.grNo]?.isPresent != true }
    }

    // Stats calculations
    val presentKumar = presentStudents.count { it.isKumar }
    val presentKanya = presentStudents.count { !it.isKumar }
    val absentKumar = absentStudents.count { it.isKumar }
    val absentKanya = absentStudents.count { !it.isKumar }
    val totalCount = filteredStudents.size

    val standardsList = remember { listOf("તમામ ધોરણ") + SchoolStandards.ALL }

    // Date Picker Dialog setup
    val calendar = Calendar.getInstance()
    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val formatted = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
                viewModel.setSelectedDate(formatted)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    fun shareAbsentReport() {
        val dateDisplay = AttendanceRepository.getDisplayDateString(selectedDate)
        val sb = StringBuilder()
        sb.append("📋 *ગેરહાજર વિદ્યાર્થી રિપોર્ટ*\n")
        sb.append("🏫 નગર પ્રાથમિક શાળા નં ૨૯ પુરુષોત્તમનગર, બાકરોલ\n")
        sb.append("📅 તારીખ: $dateDisplay\n")
        sb.append("📚 ધોરણ: $selectedStandard\n")
        sb.append("----------------------------\n")
        sb.append("❌ ગેરહાજર કુલ: ${absentStudents.size} (કુમાર: $absentKumar, કન્યા: $absentKanya)\n")
        sb.append("----------------------------\n")
        absentStudents.forEachIndexed { index, st ->
            sb.append("${index + 1}. [રોલ નં: ${st.rollNo}] ${st.name} (${st.standard})")
            if (st.parentPhone.isNotBlank()) {
                sb.append(" - મો: ${st.parentPhone}")
            }
            sb.append("\n")
        }
        sb.append("\n- મુખ્ય શિક્ષકશ્રી, નગર પ્રાથમિક શાળા નં ૨૯")
        CommunicationUtil.shareAbsentReport(context, sb.toString())
    }

    fun sharePresentReport() {
        val dateDisplay = AttendanceRepository.getDisplayDateString(selectedDate)
        val sb = StringBuilder()
        sb.append("📋 *હાજર વિદ્યાર્થી રિપોર્ટ*\n")
        sb.append("🏫 નગર પ્રાથમિક શાળા નં ૨૯ પુરુષોત્તમનગર, બાકરોલ\n")
        sb.append("📅 તારીખ: $dateDisplay\n")
        sb.append("📚 ધોરણ: $selectedStandard\n")
        sb.append("----------------------------\n")
        sb.append("✅ હાજર કુલ: ${presentStudents.size} (કુમાર: $presentKumar, કન્યા: $presentKanya)\n")
        sb.append("કુલ સંખ્યા: $totalCount\n")
        sb.append("----------------------------\n")
        presentStudents.forEachIndexed { index, st ->
            sb.append("${index + 1}. [રોલ નં: ${st.rollNo}] ${st.name} (${st.standard})\n")
        }
        sb.append("\n- મુખ્ય શિક્ષકશ્રી, નગર પ્રાથમિક શાળા નં ૨૯")
        CommunicationUtil.shareAbsentReport(context, sb.toString())
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Attendance Reports (રિપોર્ટ્સ)",
                        color = Color.White,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Medium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateTo(AppScreen.HOME) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "પાછળ જાઓ",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        if (reportTab == 1) sharePresentReport() else shareAbsentReport()
                    }) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "રિપોર્ટ શેર કરો",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SchoolPrimary
                )
            )
        },
        containerColor = BackgroundLight,
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            // Header with school official logo
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SchoolLogoBadge(
                    size = 56.dp,
                    showDetails = false
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "નગર પ્રાથમિક શાળા નં ૨૯ પુરુષોત્તમનગર",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFBA1B1D)
                    )
                    Text(
                        text = "બાકરોલ, તા.જી. આણંદ | ડાયસ કોડ: ૨૪૧૫૦૧૦૦૧૦૭",
                        fontSize = 11.sp,
                        color = TextSecondaryLight
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Date & Standard Selection Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Date picker button
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color.White,
                    shadowElevation = 1.dp,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { datePickerDialog.show() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "Select Date",
                            tint = SchoolPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "તારીખ",
                                fontSize = 10.sp,
                                color = TextSecondaryLight
                            )
                            Text(
                                text = AttendanceRepository.getDisplayDateString(selectedDate),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimaryLight
                            )
                        }
                    }
                }

                // Standard Selector Dropdown
                Box(modifier = Modifier.weight(1.3f)) {
                    ReportStandardDropdown(
                        standards = standardsList,
                        selected = selectedStandard,
                        onSelected = { viewModel.setSelectedStandard(it) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Sub-tabs: 3/1 Present Students Report | 3/2 Absent Students Report
            TabRow(
                selectedTabIndex = if (reportTab == 1) 0 else 1,
                containerColor = Color.White,
                contentColor = SchoolPrimary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[if (reportTab == 1) 0 else 1]),
                        color = if (reportTab == 1) PresentGreen else AbsentRed,
                        height = 3.dp
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .shadow(1.dp, RoundedCornerShape(8.dp))
            ) {
                Tab(
                    selected = reportTab == 1,
                    onClick = { viewModel.setReportTab(1) },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = if (reportTab == 1) PresentGreen else Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "3/1 Present (${presentStudents.size})",
                                fontWeight = if (reportTab == 1) FontWeight.Bold else FontWeight.Normal,
                                color = if (reportTab == 1) PresentGreen else TextSecondaryLight,
                                fontSize = 13.sp
                            )
                        }
                    }
                )

                Tab(
                    selected = reportTab == 2,
                    onClick = { viewModel.setReportTab(2) },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.HighlightOff,
                                contentDescription = null,
                                tint = if (reportTab == 2) AbsentRed else Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "3/2 Absent (${absentStudents.size})",
                                fontWeight = if (reportTab == 2) FontWeight.Bold else FontWeight.Normal,
                                color = if (reportTab == 2) AbsentRed else TextSecondaryLight,
                                fontSize = 13.sp
                            )
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Summary Card (Kumar / Kanya / Total)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(1.dp, RoundedCornerShape(10.dp)),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Present column
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "હાજર",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = PresentGreen
                        )
                        Text(
                            text = "કુમાર: $presentKumar  કન્યા: $presentKanya",
                            fontSize = 12.sp,
                            color = TextSecondaryLight
                        )
                    }

                    // Absent column
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "ગેરહાજર",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = AbsentRed
                        )
                        Text(
                            text = "કુમાર: $absentKumar  કન્યા: $absentKanya",
                            fontSize = 12.sp,
                            color = TextSecondaryLight
                        )
                    }

                    // Total
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "કુલ સંખ્યા",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryLight
                        )
                        Text(
                            text = "$totalCount",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = SchoolPrimary
                        )
                    }
                }
            }

            // Absent Parent Messaging Action Bar (Specific to 3/2 Absent Report)
            if (reportTab == 2 && absentStudents.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Message,
                                contentDescription = null,
                                tint = Color(0xFFE65100),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "વાલીઓને મેસેજ સુવિધા",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE65100)
                            )
                        }

                        Button(
                            onClick = { shareAbsentReport() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100)),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("બધા વાલીઓને જાણ કરો", fontSize = 11.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Student List Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (reportTab == 1) "હાજર વિદ્યાર્થીઓની યાદી (Present)" else "ગેરહાજર વિદ્યાર્થીઓની યાદી (Absent)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (reportTab == 1) PresentGreen else AbsentRed
                )

                Text(
                    text = "${if (reportTab == 1) presentStudents.size else absentStudents.size} વિદ્યાર્થીઓ",
                    fontSize = 12.sp,
                    color = TextSecondaryLight
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            val currentList = if (reportTab == 1) presentStudents else absentStudents

            if (currentList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 40.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = if (reportTab == 1) Icons.Default.HighlightOff else Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (reportTab == 1) Color.Gray else PresentGreen,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (reportTab == 1) "કોઈ વિદ્યાર્થી હાજર નોંધાયેલ નથી." else "અભિનંદન! તમામ વિદ્યાર્થીઓ હાજર છે.",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondaryLight
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(currentList, key = { it.grNo }) { student ->
                        val dateDisplay = AttendanceRepository.getDisplayDateString(selectedDate)
                        ReportStudentItem(
                            student = student,
                            isPresent = reportTab == 1,
                            onCall = {
                                CommunicationUtil.makeCall(context, student.parentPhone)
                            },
                            onSms = {
                                val message = CommunicationUtil.createAbsentMessage(
                                    studentName = student.name,
                                    standard = student.standard,
                                    rollNo = student.rollNo,
                                    date = dateDisplay
                                )
                                CommunicationUtil.sendSms(context, student.parentPhone, message)
                            },
                            onWhatsApp = {
                                val message = CommunicationUtil.createAbsentMessage(
                                    studentName = student.name,
                                    standard = student.standard,
                                    rollNo = student.rollNo,
                                    date = dateDisplay
                                )
                                CommunicationUtil.sendWhatsApp(context, student.parentPhone, message)
                            },
                            onCustomizeMessage = {
                                customMessageText = CommunicationUtil.createAbsentMessage(
                                    studentName = student.name,
                                    standard = student.standard,
                                    rollNo = student.rollNo,
                                    date = dateDisplay
                                )
                                studentToMessage = student
                            }
                        )
                    }
                }
            }
        }
    }

    // Dialog for viewing & custom editing message before sending
    studentToMessage?.let { student ->
        AlertDialog(
            onDismissRequest = { studentToMessage = null },
            title = {
                Text(
                    text = "વાલીને મેસેજ મોકલો",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = SchoolPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "વિદ્યાર્થી: ${student.name} (${student.standard})",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "વાલીનો મોબાઈલ નં: ${if (student.parentPhone.isNotBlank()) student.parentPhone else "નોંધાયેલ નથી"}",
                        fontSize = 12.sp,
                        color = TextSecondaryLight
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    OutlinedTextField(
                        value = customMessageText,
                        onValueChange = { customMessageText = it },
                        label = { Text("મેસેજ લખાણ") },
                        minLines = 4,
                        maxLines = 6,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // WhatsApp Button
                    Button(
                        onClick = {
                            CommunicationUtil.sendWhatsApp(context, student.parentPhone, customMessageText)
                            studentToMessage = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
                    ) {
                        Text("WhatsApp", color = Color.White, fontSize = 12.sp)
                    }

                    // SMS Button
                    Button(
                        onClick = {
                            CommunicationUtil.sendSms(context, student.parentPhone, customMessageText)
                            studentToMessage = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SchoolPrimary)
                    ) {
                        Text("SMS", color = Color.White, fontSize = 12.sp)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { studentToMessage = null }) {
                    Text("રદ કરો")
                }
            }
        )
    }
}

@Composable
fun ReportStudentItem(
    student: Student,
    isPresent: Boolean,
    onCall: () -> Unit,
    onSms: () -> Unit,
    onWhatsApp: () -> Unit,
    onCustomizeMessage: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(1.dp, RoundedCornerShape(10.dp)),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Roll Number Circle Badge
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (student.isKumar) Color(0xFFE3F2FD) else Color(0xFFFCE4EC)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${student.rollNo}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (student.isKumar) BoyBlue else GirlPink
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = student.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimaryLight
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${student.standard} | GR: ${student.grNo} | ${student.genderLabel}",
                            fontSize = 11.sp,
                            color = TextSecondaryLight
                        )
                    }
                    if (student.parentPhone.isNotBlank()) {
                        Text(
                            text = "વાલી મો: ${student.parentPhone}",
                            fontSize = 11.sp,
                            color = Color(0xFF00796B),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Status Badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isPresent) PresentGreenBg else AbsentRedBg
                ) {
                    Text(
                        text = if (isPresent) "હાજર" else "ગેરહાજર",
                        color = if (isPresent) PresentGreen else AbsentRed,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // If student is absent, display dedicated parent communication buttons
            if (!isPresent) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF8FAFC), RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "વાલીને જાણ કરો:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextSecondaryLight
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        // WhatsApp Button
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFE8F5E9),
                            modifier = Modifier.clickable { onWhatsApp() }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "WhatsApp",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2E7D32)
                                )
                            }
                        }

                        // SMS Button
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFE3F2FD),
                            modifier = Modifier.clickable { onSms() }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Message,
                                    contentDescription = null,
                                    tint = SchoolPrimary,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "SMS",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SchoolPrimary
                                )
                            }
                        }

                        // Call Button
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFEDE7F6),
                            modifier = Modifier.clickable { onCall() }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Phone,
                                    contentDescription = null,
                                    tint = Color(0xFF512DA8),
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "કૉલ",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF512DA8)
                                )
                            }
                        }

                        // Edit / Preview Dialog Button
                        IconButton(
                            onClick = onCustomizeMessage,
                            modifier = Modifier.size(26.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "કસ્ટમ મેસેજ",
                                tint = TextSecondaryLight,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportStandardDropdown(
    standards: List<String>,
    selected: String,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text("ધોરણ પસંદ કરો") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            standards.forEach { standardItem ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = standardItem,
                            fontWeight = if (standardItem == selected) FontWeight.Bold else FontWeight.Normal,
                            color = if (standardItem == selected) SchoolPrimary else TextPrimaryLight
                        )
                    },
                    onClick = {
                        onSelected(standardItem)
                        expanded = false
                    }
                )
            }
        }
    }
}
