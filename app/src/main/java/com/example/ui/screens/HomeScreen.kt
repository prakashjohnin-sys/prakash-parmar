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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.AppScreen
import com.example.ui.AttendanceViewModel
import com.example.ui.theme.BackgroundLight
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
                    // Quick shortcut to Student Database / QR ID cards
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
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // School Emblem / Logo
            Image(
                painter = painterResource(id = R.drawable.ic_school_logo),
                contentDescription = "School Logo",
                modifier = Modifier
                    .size(96.dp)
                    .padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // School Welcome Message
            Text(
                text = "Welcome to નગર પ્રાથમિક શાળા નં ૨૯\nપુરુષોતમનગર બાકરોલ તા.જી.આણંદ",
                fontSize = 17.sp,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp,
                color = TextPrimaryLight,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

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

            Spacer(modifier = Modifier.height(16.dp))

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

            Spacer(modifier = Modifier.height(32.dp))

            // Today Section Header
            Text(
                text = "Today",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimaryLight
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 3 Metric Cards side by side
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Card 1: હાજર (કુમાર / કન્યા)
                MetricCategoryCard(
                    title = "હાજર",
                    col1Label = "કુમાર",
                    col1Value = stats.presentKumar,
                    col2Label = "કન્યા",
                    col2Value = stats.presentKanya,
                    modifier = Modifier.weight(1f)
                )

                // Card 2: ગેરહાજર (કુમાર / કન્યા)
                MetricCategoryCard(
                    title = "ગેરહાજર",
                    col1Label = "કુમાર",
                    col1Value = stats.absentKumar,
                    col2Label = "કન્યા",
                    col2Value = stats.absentKanya,
                    modifier = Modifier.weight(1f)
                )

                // Card 3: કુલ સંખ્યા
                MetricSingleCard(
                    title = "કુલ",
                    subtitle = "સંખ્યા",
                    value = stats.totalCount,
                    modifier = Modifier.weight(0.9f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun MenuCard(
    title: String,
    icon: ImageVector,
    testTag: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(140.dp)
            .shadow(2.dp, RoundedCornerShape(4.dp))
            .clickable { onClick() }
            .testTag(testTag),
        shape = RoundedCornerShape(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Color(0xFF5A738E),
                modifier = Modifier.size(36.dp)
            )

            Text(
                text = title,
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 22.sp,
                color = TextPrimaryLight
            )
        }
    }
}

@Composable
fun MetricCategoryCard(
    title: String,
    col1Label: String,
    col1Value: Int,
    col2Label: String,
    col2Value: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .shadow(2.dp, RoundedCornerShape(2.dp)),
        shape = RoundedCornerShape(2.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimaryLight
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = col1Label,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimaryLight
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = col1Value.toString(),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Normal,
                        color = TextPrimaryLight
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = col2Label,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimaryLight
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = col2Value.toString(),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Normal,
                        color = TextPrimaryLight
                    )
                }
            }
        }
    }
}

@Composable
fun MetricSingleCard(
    title: String,
    subtitle: String,
    value: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .shadow(2.dp, RoundedCornerShape(2.dp)),
        shape = RoundedCornerShape(2.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimaryLight
            )
            Text(
                text = subtitle,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimaryLight
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value.toString(),
                fontSize = 18.sp,
                fontWeight = FontWeight.Normal,
                color = TextPrimaryLight
            )
        }
    }
}
