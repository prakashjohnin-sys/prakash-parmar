package com.example.ui.screens

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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewAttendanceScreen(
    viewModel: AttendanceViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.navigateTo(AppScreen.HOME)
    }

    val allStudents by viewModel.allStudents.collectAsState()
    val allStandards by viewModel.allStandards.collectAsState()
    val selectedStandard by viewModel.selectedStandard.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val attendanceRecords by viewModel.attendanceForSelectedDate.collectAsState()

    var statusFilter by remember { mutableStateOf(0) } // 0 = All, 1 = Present, 2 = Absent

    val recordMap = remember(attendanceRecords) {
        attendanceRecords.associateBy { it.studentGrNo }
    }

    val filteredStudents = remember(allStudents, selectedStandard, statusFilter, recordMap) {
        allStudents.filter { student ->
            val matchStandard = (selectedStandard == "તમામ ધોરણ" || selectedStandard.isBlank() || student.standard == selectedStandard)
            val isPresent = recordMap[student.grNo]?.isPresent == true
            val matchStatus = when (statusFilter) {
                1 -> isPresent
                2 -> !isPresent
                else -> true
            }
            matchStandard && matchStatus
        }
    }

    // Stats for the current view
    val totalInView = filteredStudents.size
    val presentInView = remember(filteredStudents, recordMap) {
        filteredStudents.count { recordMap[it.grNo]?.isPresent == true }
    }
    val absentInView = totalInView - presentInView

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "View Attendance (હાજરી જુઓ)",
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
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Date & Class Filters
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Date Display Pill
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(8.dp),
                    shadowElevation = 1.dp,
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = SchoolPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = AttendanceRepository.getDisplayDateString(selectedDate),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimaryLight
                        )
                    }
                }

                // Standard Selector
                Box(modifier = Modifier.weight(1f)) {
                    StandardSelectorDropdown(
                        standards = listOf("તમામ ધોરણ") + allStandards,
                        selected = selectedStandard,
                        onSelected = { viewModel.setSelectedStandard(it) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Status Filter Chips: All, Present, Absent
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = statusFilter == 0,
                    onClick = { statusFilter = 0 },
                    label = { Text("બધા (${filteredStudents.size})") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SchoolPrimary,
                        selectedLabelColor = Color.White
                    )
                )
                FilterChip(
                    selected = statusFilter == 1,
                    onClick = { statusFilter = 1 },
                    label = { Text("હાજર") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PresentGreen,
                        selectedLabelColor = Color.White
                    )
                )
                FilterChip(
                    selected = statusFilter == 2,
                    onClick = { statusFilter = 2 },
                    label = { Text("ગેરહાજર") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AbsentRed,
                        selectedLabelColor = Color.White
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Summary Card
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
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "કુલ વિદ્યાર્થી: $totalInView",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimaryLight
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "હાજર: $presentInView",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = PresentGreen
                        )
                        Text(
                            text = "ગેરહાજર: $absentInView",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = AbsentRed
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Student List
            if (filteredStudents.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 40.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Text(
                        text = "કોઈ વિદ્યાર્થી મળ્યા નથી.",
                        fontSize = 15.sp,
                        color = TextSecondaryLight
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredStudents, key = { it.grNo }) { student ->
                        val record = recordMap[student.grNo]
                        val isPresent = record?.isPresent == true

                        StudentAttendanceItem(
                            student = student,
                            isPresent = isPresent,
                            onToggle = {
                                if (record != null) {
                                    viewModel.toggleRecordAttendance(record)
                                } else {
                                    if (isPresent) {
                                        viewModel.markStudentAbsentDirectly(student)
                                    } else {
                                        viewModel.markStudentPresentDirectly(student)
                                    }
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
fun StudentAttendanceItem(
    student: Student,
    isPresent: Boolean,
    onToggle: () -> Unit
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
            // Roll No Badge
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
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "GR: ${student.grNo}",
                        fontSize = 12.sp,
                        color = TextSecondaryLight
                    )
                    Text(
                        text = "•",
                        fontSize = 12.sp,
                        color = TextSecondaryLight
                    )
                    Text(
                        text = student.standard,
                        fontSize = 12.sp,
                        color = TextSecondaryLight
                    )
                    Text(
                        text = "•",
                        fontSize = 12.sp,
                        color = TextSecondaryLight
                    )
                    Text(
                        text = if (student.isKumar) "કુમાર" else "કન્યા",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (student.isKumar) BoyBlue else GirlPink
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Present / Absent Badge + Toggle Switch
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
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

                Switch(
                    checked = isPresent,
                    onCheckedChange = { onToggle() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = PresentGreen,
                        checkedTrackColor = PresentGreenBg,
                        uncheckedThumbColor = AbsentRed,
                        uncheckedTrackColor = AbsentRedBg
                    )
                )
            }
        }
    }
}
