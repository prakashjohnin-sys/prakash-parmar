package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HighlightOff
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Student
import com.example.data.repository.AttendanceRepository
import com.example.ui.AppScreen
import com.example.ui.AttendanceViewModel
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    val allStandards by viewModel.allStandards.collectAsState()
    val selectedStandard by viewModel.selectedStandard.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val attendanceRecords by viewModel.attendanceForSelectedDate.collectAsState()
    val reportTab by viewModel.reportTab.collectAsState()

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
    val pKumar = presentStudents.count { it.isKumar }
    val pKanya = presentStudents.count { !it.isKumar }
    val aKumar = absentStudents.count { it.isKumar }
    val aKanya = absentStudents.count { !it.isKumar }
    val totalCount = filteredStudents.size

    fun shareReport() {
        val dateDisplay = AttendanceRepository.getDisplayDateString(selectedDate)
        val sb = StringBuilder()
        sb.append("🏫 *નગર પ્રાથમિક શાળા નં ૨૯ પુરુષોતમનગર બાકરોલ*\n")
        sb.append("📅 *તારીખ:* $dateDisplay\n")
        sb.append("📚 *ધોરણ:* $selectedStandard\n\n")

        sb.append("📊 *હાજરી સારાંશ:*\n")
        sb.append("• હાજર: કુલ ${presentStudents.size} (કુમાર: $pKumar, કન્યા: $pKanya)\n")
        sb.append("• ગેરહાજર: કુલ ${absentStudents.size} (કુમાર: $aKumar, કન્યા: $aKanya)\n")
        sb.append("• કુલ સંખ્યા: $totalCount\n\n")

        if (reportTab == 1) {
            sb.append("✅ *3/1 હાજર વિદ્યાર્થીઓની યાદી (Present Students):*\n")
            presentStudents.forEachIndexed { i, s ->
                sb.append("${i + 1}. [રોલ: ${s.rollNo}] ${s.name} (${if (s.isKumar) "કુમાર" else "કન્યા"})\n")
            }
        } else {
            sb.append("❌ *3/2 ગેરહાજર વિદ્યાર્થીઓની યાદી (Absent Students):*\n")
            absentStudents.forEachIndexed { i, s ->
                val phoneText = if (s.parentPhone.isNotBlank()) " - મો: ${s.parentPhone}" else ""
                sb.append("${i + 1}. [રોલ: ${s.rollNo}] ${s.name} (${if (s.isKumar) "કુમાર" else "કન્યા"})$phoneText\n")
            }
        }

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, sb.toString())
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "હાજરી રિપોર્ટ શેર કરો")
        context.startActivity(shareIntent)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Attendance Reports",
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
                    IconButton(onClick = { shareReport() }) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share Report",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SchoolPrimary)
            )
        },
        containerColor = BackgroundLight,
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Tabs 3/1 Present and 3/2 Absent
            TabRow(
                selectedTabIndex = if (reportTab == 1) 0 else 1,
                containerColor = Color.White,
                contentColor = SchoolPrimary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[if (reportTab == 1) 0 else 1]),
                        color = if (reportTab == 1) PresentGreen else AbsentRed
                    )
                }
            ) {
                Tab(
                    selected = reportTab == 1,
                    onClick = { viewModel.setReportTab(1) },
                    text = {
                        Text(
                            text = "3/1 Present (${presentStudents.size})",
                            fontWeight = if (reportTab == 1) FontWeight.Bold else FontWeight.Normal,
                            color = if (reportTab == 1) PresentGreen else TextSecondaryLight
                        )
                    }
                )
                Tab(
                    selected = reportTab == 2,
                    onClick = { viewModel.setReportTab(2) },
                    text = {
                        Text(
                            text = "3/2 Absent (${absentStudents.size})",
                            fontWeight = if (reportTab == 2) FontWeight.Bold else FontWeight.Normal,
                            color = if (reportTab == 2) AbsentRed else TextSecondaryLight
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Standard Filter
            StandardSelectorDropdown(
                standards = listOf("તમામ ધોરણ") + allStandards,
                selected = selectedStandard,
                onSelected = { viewModel.setSelectedStandard(it) }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Today's Breakdown Summary Card matching user's prompt
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(1.dp, RoundedCornerShape(8.dp)),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "વિદ્યાર્થી સંખ્યા સારાંશ (Attendance Summary)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimaryLight
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Present Section
                        Column {
                            Text(
                                text = "હાજર: ${presentStudents.size}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = PresentGreen
                            )
                            Text(
                                text = "કુમાર: $pKumar | કન્યા: $pKanya",
                                fontSize = 12.sp,
                                color = TextSecondaryLight
                            )
                        }

                        // Absent Section
                        Column {
                            Text(
                                text = "ગેરહાજર: ${absentStudents.size}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = AbsentRed
                            )
                            Text(
                                text = "કુમાર: $aKumar | કન્યા: $aKanya",
                                fontSize = 12.sp,
                                color = TextSecondaryLight
                            )
                        }

                        // Total Section
                        Column {
                            Text(
                                text = "કુલ સંખ્યા",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimaryLight
                            )
                            Text(
                                text = "$totalCount",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimaryLight
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Report Student List Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (reportTab == 1) "હાજર વિદ્યાર્થીઓની યાદી" else "ગેરહાજર વિદ્યાર્થીઓની યાદી",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (reportTab == 1) PresentGreen else AbsentRed
                )

                TextButton(onClick = { shareReport() }) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = SchoolPrimary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("શેર કરો", color = SchoolPrimary, fontSize = 13.sp)
                }
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
                    Text(
                        text = if (reportTab == 1) "કોઈ વિદ્યાર્થી હાજર નથી." else "તમામ વિદ્યાર્થીઓ હાજર છે!",
                        fontSize = 15.sp,
                        color = TextSecondaryLight
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(currentList, key = { it.grNo }) { student ->
                        ReportStudentItem(
                            student = student,
                            isPresent = reportTab == 1,
                            onCall = {
                                if (student.parentPhone.isNotBlank()) {
                                    val intent = Intent(Intent.ACTION_DIAL).apply {
                                        data = Uri.parse("tel:${student.parentPhone}")
                                    }
                                    context.startActivity(intent)
                                }
                            },
                            onSms = {
                                if (student.parentPhone.isNotBlank()) {
                                    val intent = Intent(Intent.ACTION_VIEW).apply {
                                        data = Uri.parse("sms:${student.parentPhone}")
                                        putExtra(
                                            "sms_body",
                                            "આદરણીય વાલીશ્રી, આપનો પાલ્ય ${student.name} આજે શાળામાં ગેરહાજર છે. - નગર પ્રાથમિક શાળા નં ૨૯"
                                        )
                                    }
                                    context.startActivity(intent)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ReportStudentItem(
    student: Student,
    isPresent: Boolean,
    onCall: () -> Unit,
    onSms: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(1.dp, RoundedCornerShape(8.dp)),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(if (student.isKumar) Color(0xFFE3F2FD) else Color(0xFFFCE4EC)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${student.rollNo}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (student.isKumar) BoyBlue else GirlPink
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = student.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimaryLight
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = student.standard,
                        fontSize = 12.sp,
                        color = TextSecondaryLight
                    )
                    Text(text = "•", fontSize = 12.sp, color = TextSecondaryLight)
                    Text(
                        text = if (student.isKumar) "કુમાર" else "કન્યા",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (student.isKumar) BoyBlue else GirlPink
                    )
                    if (student.parentPhone.isNotBlank()) {
                        Text(text = "•", fontSize = 12.sp, color = TextSecondaryLight)
                        Text(
                            text = "મો: ${student.parentPhone}",
                            fontSize = 11.sp,
                            color = TextSecondaryLight
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            if (!isPresent && student.parentPhone.isNotBlank()) {
                // Call and SMS actions for absent students
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = onCall,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "Call Parent",
                            tint = Color(0xFF1976D2),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(
                        onClick = onSms,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Message,
                            contentDescription = "SMS Parent",
                            tint = Color(0xFF388E3C),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isPresent) PresentGreenBg else AbsentRedBg
                ) {
                    Text(
                        text = if (isPresent) "હાજર" else "ગેરહાજર",
                        color = if (isPresent) PresentGreen else AbsentRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}
