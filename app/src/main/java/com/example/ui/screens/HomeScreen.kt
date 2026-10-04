package com.example.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.SchoolStandards
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: AttendanceViewModel,
    modifier: Modifier = Modifier
) {
    val stats by viewModel.todayStats.collectAsState()
    val allStandards by viewModel.allStandards.collectAsState()
    val selectedStandard by viewModel.selectedStandard.collectAsState()
    val allStudents by viewModel.allStudents.collectAsState()
    val attendanceRecords by viewModel.attendanceForSelectedDate.collectAsState()

    // Filtered stats by selected standard
    val filteredStudents = remember(allStudents, selectedStandard) {
        if (selectedStandard == "તમામ ધોરણ" || selectedStandard.isBlank()) {
            allStudents
        } else {
            allStudents.filter { it.standard == selectedStandard }
        }
    }

    val recordMap = remember(attendanceRecords) {
        attendanceRecords.associateBy { it.studentGrNo }
    }

    val presentKumar = remember(filteredStudents, recordMap) {
        filteredStudents.count { it.isKumar && recordMap[it.grNo]?.isPresent == true }
    }
    val presentKanya = remember(filteredStudents, recordMap) {
        filteredStudents.count { !it.isKumar && recordMap[it.grNo]?.isPresent == true }
    }
    val absentKumar = remember(filteredStudents, recordMap) {
        filteredStudents.count { it.isKumar && recordMap[it.grNo]?.isPresent != true }
    }
    val absentKanya = remember(filteredStudents, recordMap) {
        filteredStudents.count { !it.isKumar && recordMap[it.grNo]?.isPresent != true }
    }
    val totalCount = filteredStudents.size

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Attendance Scanner",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium
                    )
                },
                actions = {
                    // Quick shortcut to Excel import & Student Database
                    IconButton(
                        onClick = { viewModel.navigateTo(AppScreen.STUDENT_DATABASE) },
                        modifier = Modifier.testTag("excel_import_shortcut")
                    ) {
                        Icon(
                            imageVector = Icons.Default.TableChart,
                            contentDescription = "Excel ફાઇલ / વિદ્યાર્થીઓ",
                            tint = Color.White
                        )
                    }
                    IconButton(
                        onClick = { viewModel.navigateTo(AppScreen.STUDENT_DATABASE) },
                        modifier = Modifier.testTag("manage_students_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Badge,
                            contentDescription = "વિદ્યાર્થી ડેટાબેઝ",
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Official School Seal / Logo Badge with red outer ring and Saraswati emblem
            SchoolLogoBadge(
                size = 112.dp,
                showDetails = true,
                modifier = Modifier.padding(top = 2.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // School Welcome Message
            Text(
                text = "Welcome to નગર પ્રાથમિક શાળા નં ૨૯\nપુરુષોતમનગર બાકરોલ તા.જી.આણંદ",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                lineHeight = 23.sp,
                color = TextPrimaryLight,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(18.dp))

            // 4 Grid Action Cards (2 rows of 2)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                MenuCard(
                    title = "Take Attendance",
                    icon = Icons.Default.DateRange,
                    testTag = "btn_take_attendance",
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.navigateTo(AppScreen.TAKE_ATTENDANCE) }
                )
                MenuCard(
                    title = "View Attendance",
                    icon = Icons.Default.Groups,
                    testTag = "btn_view_attendance",
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.navigateTo(AppScreen.VIEW_ATTENDANCE) }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                MenuCard(
                    title = "Reports",
                    icon = Icons.Default.Description,
                    testTag = "btn_reports",
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.navigateTo(AppScreen.REPORTS) }
                )
                MenuCard(
                    title = "Logout",
                    icon = Icons.Default.PowerSettingsNew,
                    testTag = "btn_logout",
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.logout() }
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Standard Filter Dropdown for Today's Stats
            HomeStandardDropdown(
                standards = listOf("તમામ ધોરણ") + SchoolStandards.ALL,
                selected = selectedStandard,
                onSelected = { viewModel.setSelectedStandard(it) }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Today Section Heading
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Today",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryLight
                )
                if (selectedStandard != "તમામ ધોરણ") {
                    Text(
                        text = " ($selectedStandard)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = SchoolPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 3 Today Attendance Summary Cards: હાજર, ગેરહાજર, કુલ સંખ્યા
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // હાજર કાર્ડ (Present)
                StatCard(
                    title = "હાજર",
                    kumarCount = presentKumar,
                    kanyaCount = presentKanya,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("stat_card_present")
                )

                // ગેરહાજર કાર્ડ (Absent)
                StatCard(
                    title = "ગેરહાજર",
                    kumarCount = absentKumar,
                    kanyaCount = absentKanya,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("stat_card_absent")
                )

                // કુલ સંખ્યા કાર્ડ (Total Count)
                TotalCountCard(
                    title = "કુલ\nસંખ્યા",
                    total = totalCount,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("stat_card_total")
                )
            }

            // Quick Parent Messaging shortcut if there are absent students
            if (absentKumar + absentKanya > 0) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            viewModel.setReportTab(2) // 3/2 Absent tab
                            viewModel.navigateTo(AppScreen.REPORTS)
                        },
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = AbsentRedBg)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Message,
                                contentDescription = null,
                                tint = AbsentRed,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "ગેરહાજર વાલીઓને મેસેજ કરો (${absentKumar + absentKanya})",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AbsentRed
                                )
                                Text(
                                    text = "SMS અથવા WhatsApp દ્વારા જાણ કરો",
                                    fontSize = 11.sp,
                                    color = TextSecondaryLight
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "મેસેજ મોકલો",
                            tint = AbsentRed,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeStandardDropdown(
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
            label = { Text("ધોરણ પસંદ કરો (Select Standard)") },
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

@Composable
private fun MenuCard(
    title: String,
    icon: ImageVector,
    testTag: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(130.dp)
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(4.dp),
                ambientColor = Color.Black.copy(alpha = 0.08f)
            )
            .clickable(onClick = onClick)
            .testTag(testTag),
        shape = RoundedCornerShape(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Color(0xFF5F6368),
                modifier = Modifier.size(36.dp)
            )

            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 20.sp,
                color = Color(0xFF202124)
            )
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    kumarCount: Int,
    kanyaCount: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(120.dp)
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(4.dp),
                ambientColor = Color.Black.copy(alpha = 0.08f)
            ),
        shape = RoundedCornerShape(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 6.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF202124)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "કુમાર",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF202124)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$kumarCount",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color(0xFF202124)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "કન્યા",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF202124)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$kanyaCount",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color(0xFF202124)
                    )
                }
            }
        }
    }
}

@Composable
private fun TotalCountCard(
    title: String,
    total: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(120.dp)
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(4.dp),
                ambientColor = Color.Black.copy(alpha = 0.08f)
            ),
        shape = RoundedCornerShape(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 6.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp,
                color = Color(0xFF202124)
            )

            Text(
                text = "$total",
                fontSize = 20.sp,
                fontWeight = FontWeight.Normal,
                color = Color(0xFF202124)
            )
        }
    }
}
