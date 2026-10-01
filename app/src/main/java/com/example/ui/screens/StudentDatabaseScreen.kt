package com.example.ui.screens

import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.data.model.Student
import com.example.ui.AppScreen
import com.example.ui.AttendanceViewModel
import com.example.ui.theme.BackgroundLight
import com.example.ui.theme.BoyBlue
import com.example.ui.theme.GirlPink
import com.example.ui.theme.SchoolPrimary
import com.example.ui.theme.TextPrimaryLight
import com.example.ui.theme.TextSecondaryLight
import com.example.util.QrCodeUtil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentDatabaseScreen(
    viewModel: AttendanceViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.navigateTo(AppScreen.HOME)
    }

    val allStudents by viewModel.allStudents.collectAsState()
    val allStandards by viewModel.allStandards.collectAsState()
    val selectedStandard by viewModel.selectedStandard.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var viewingStudentIdCard by remember { mutableStateOf<Student?>(null) }
    var studentToDelete by remember { mutableStateOf<Student?>(null) }

    val filteredStudents = remember(allStudents, selectedStandard) {
        if (selectedStandard == "તમામ ધોરણ" || selectedStandard.isBlank()) {
            allStudents
        } else {
            allStudents.filter { it.standard == selectedStandard }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Student Database (ઓળખ કાર્ડ)",
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
                    IconButton(onClick = { viewModel.resetData() }) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = "રીસેટ કરો",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SchoolPrimary)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = SchoolPrimary,
                contentColor = Color.White,
                modifier = Modifier.testTag("fab_add_student")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Student")
            }
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
            // Standard filter
            StandardSelectorDropdown(
                standards = listOf("તમામ ધોરણ") + allStandards,
                selected = selectedStandard,
                onSelected = { viewModel.setSelectedStandard(it) }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Hint card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE1F5FE))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCode,
                        contentDescription = null,
                        tint = SchoolPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "વિદ્યાર્થીના કાર્ડ પર ક્લિક કરીને તેનો QR કોડ જોઈ શકો છો અને સ્કેન ટેસ્ટ કરી શકો છો.",
                        fontSize = 13.sp,
                        color = TextPrimaryLight
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "કુલ વિદ્યાર્થીઓ: ${filteredStudents.size}",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondaryLight
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredStudents, key = { it.grNo }) { student ->
                    StudentCardRow(
                        student = student,
                        onViewCard = { viewingStudentIdCard = student },
                        onDelete = { studentToDelete = student }
                    )
                }
            }
        }

        // Add Student Dialog
        if (showAddDialog) {
            AddStudentDialog(
                standards = allStandards.ifEmpty { listOf("ધોરણ ૫", "ધોરણ ૬", "ધોરણ ૭", "ધોરણ ૮") },
                onDismiss = { showAddDialog = false },
                onAdd = { newStudent ->
                    viewModel.addStudent(newStudent)
                    showAddDialog = false
                }
            )
        }

        // Student ID Card Preview Dialog with Real Generated QR Code
        viewingStudentIdCard?.let { student ->
            StudentIdCardDialog(
                student = student,
                onDismiss = { viewingStudentIdCard = null },
                onTestScan = {
                    viewModel.onQrScanned(student.grNo)
                    viewingStudentIdCard = null
                }
            )
        }

        // Delete Confirmation Dialog
        studentToDelete?.let { student ->
            AlertDialog(
                onDismissRequest = { studentToDelete = null },
                title = { Text("વિદ્યાર્થી રદ્દ કરવો છે?") },
                text = { Text("શું તમે '${student.name}' ને ડેટાબેઝમાંથી કાઢી નાખવા માંગો છો?") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteStudent(student)
                            studentToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                    ) {
                        Text("કાઢી નાખો")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { studentToDelete = null }) {
                        Text("રદ્દ કરો")
                    }
                }
            )
        }
    }
}

@Composable
fun StudentCardRow(
    student: Student,
    onViewCard: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(1.dp, RoundedCornerShape(8.dp))
            .clickable { onViewCard() },
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
                    .size(38.dp)
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
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "GR: ${student.grNo}",
                        fontSize = 12.sp,
                        color = TextSecondaryLight
                    )
                    Text(text = "•", fontSize = 12.sp, color = TextSecondaryLight)
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
                }
            }

            IconButton(onClick = onViewCard) {
                Icon(
                    imageVector = Icons.Default.QrCode,
                    contentDescription = "View QR",
                    tint = SchoolPrimary
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color(0xFFE57373)
                )
            }
        }
    }
}

@Composable
fun StudentIdCardDialog(
    student: Student,
    onDismiss: () -> Unit,
    onTestScan: () -> Unit
) {
    val qrBitmap: Bitmap? = remember(student.grNo) {
        QrCodeUtil.generateQrBitmap(student.grNo, size = 400)
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .shadow(8.dp, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // School Header on ID Card
                Image(
                    painter = painterResource(id = R.drawable.ic_school_logo),
                    contentDescription = "School Logo",
                    modifier = Modifier.size(56.dp)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "નગર પ્રાથમિક શાળા નં ૨૯\nપુરુષોતમનગર બાકરોલ",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = SchoolPrimary
                )

                Text(
                    text = "વિદ્યાર્થી ઓળખ કાર્ડ (Student ID Card)",
                    fontSize = 11.sp,
                    color = TextSecondaryLight
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Generated QR Code
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (qrBitmap != null) {
                        Image(
                            bitmap = qrBitmap.asImageBitmap(),
                            contentDescription = "Student QR Code",
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Text("QR બનાવી શકાયો નથી", fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Student Details
                Text(
                    text = student.name,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = TextPrimaryLight
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("રોલ નં", fontSize = 11.sp, color = TextSecondaryLight)
                        Text("${student.rollNo}", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("GR નં", fontSize = 11.sp, color = TextSecondaryLight)
                        Text(student.grNo, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("ધોરણ", fontSize = 11.sp, color = TextSecondaryLight)
                        Text(student.standard, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("જાતિ", fontSize = 11.sp, color = TextSecondaryLight)
                        Text(
                            if (student.isKumar) "કુમાર" else "કન્યા",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (student.isKumar) BoyBlue else GirlPink
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action buttons
                Button(
                    onClick = onTestScan,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SchoolPrimary)
                ) {
                    Icon(imageVector = Icons.Default.QrCode, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("આ QR સ્કેન ટેસ્ટ કરો (Mark Present)")
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(onClick = onDismiss) {
                    Text("બંધ કરો (Close)")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddStudentDialog(
    standards: List<String>,
    onDismiss: () -> Unit,
    onAdd: (Student) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var grNo by remember { mutableStateOf("") }
    var rollNo by remember { mutableStateOf("") }
    var standard by remember { mutableStateOf(standards.firstOrNull() ?: "ધોરણ ૫") }
    var gender by remember { mutableStateOf("KUMAR") }
    var phone by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("નવો વિદ્યાર્થી ઉમેરો") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; errorMessage = null },
                    label = { Text("વિદ્યાર્થીનું પૂરું નામ") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = rollNo,
                        onValueChange = { rollNo = it; errorMessage = null },
                        label = { Text("રોલ નં") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = grNo,
                        onValueChange = { grNo = it; errorMessage = null },
                        label = { Text("GR નં") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Standard Selector
                StandardSelectorDropdown(
                    standards = standards,
                    selected = standard,
                    onSelected = { standard = it }
                )

                // Gender Radio Buttons
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("જાતિ:", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    RadioButton(
                        selected = gender == "KUMAR",
                        onClick = { gender = "KUMAR" },
                        colors = RadioButtonDefaults.colors(selectedColor = BoyBlue)
                    )
                    Text("કુમાર", modifier = Modifier.clickable { gender = "KUMAR" })

                    Spacer(modifier = Modifier.width(16.dp))

                    RadioButton(
                        selected = gender == "KANYA",
                        onClick = { gender = "KANYA" },
                        colors = RadioButtonDefaults.colors(selectedColor = GirlPink)
                    )
                    Text("કન્યા", modifier = Modifier.clickable { gender = "KANYA" })
                }

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("વાલીનો મોબાઈલ નં") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMessage != null) {
                    Text(errorMessage ?: "", color = Color.Red, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        errorMessage = "નામ દાખલ કરવું જરૂરી છે"
                        return@Button
                    }
                    val roll = rollNo.toIntOrNull() ?: 1
                    val gr = if (grNo.isNotBlank()) grNo.trim() else System.currentTimeMillis().toString().takeLast(4)

                    val newStudent = Student(
                        name = name.trim(),
                        rollNo = roll,
                        grNo = gr,
                        standard = standard,
                        gender = gender,
                        parentPhone = phone.trim()
                    )
                    onAdd(newStudent)
                },
                colors = ButtonDefaults.buttonColors(containerColor = SchoolPrimary)
            ) {
                Text("ઉમેરો")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("રદ્દ કરો")
            }
        }
    )
}
