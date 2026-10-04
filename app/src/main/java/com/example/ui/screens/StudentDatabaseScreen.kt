package com.example.ui.screens

import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.SchoolStandards
import com.example.data.model.Student
import com.example.ui.AppScreen
import com.example.ui.AttendanceViewModel
import com.example.ui.components.SchoolLogoBadge
import com.example.ui.theme.BackgroundLight
import com.example.ui.theme.BoyBlue
import com.example.ui.theme.GirlPink
import com.example.ui.theme.SchoolPrimary
import com.example.ui.theme.TextPrimaryLight
import com.example.ui.theme.TextSecondaryLight
import com.example.util.CommunicationUtil
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

    val context = LocalContext.current

    val allStudents by viewModel.allStudents.collectAsState()
    val allStandards by viewModel.allStandards.collectAsState()
    val selectedStandard by viewModel.selectedStandard.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var viewingStudentIdCard by remember { mutableStateOf<Student?>(null) }
    var studentToDelete by remember { mutableStateOf<Student?>(null) }

    // Excel / CSV Import & Export states
    var importedStudentsPreview by remember { mutableStateOf<List<Student>?>(null) }
    var showPasteCsvDialog by remember { mutableStateOf(false) }
    var showOptionsMenu by remember { mutableStateOf(false) }
    var isLoadingFile by remember { mutableStateOf(false) }

    // Android File Picker Launcher for Excel/CSV file upload
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            isLoadingFile = true
            viewModel.parseStudentsFromUri(uri) { result ->
                isLoadingFile = false
                result.onSuccess { parsedList ->
                    importedStudentsPreview = parsedList
                }.onFailure { ex ->
                    Toast.makeText(context, "ભૂલ: ${ex.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

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
                    // Excel Action button
                    IconButton(onClick = { showOptionsMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "વધુ વિકલ્પો",
                            tint = Color.White
                        )
                    }

                    DropdownMenu(
                        expanded = showOptionsMenu,
                        onDismissRequest = { showOptionsMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("📥 Excel નમૂનો (Template) મેળવો") },
                            leadingIcon = { Icon(Icons.Default.CloudDownload, contentDescription = null, tint = SchoolPrimary) },
                            onClick = {
                                showOptionsMenu = false
                                viewModel.shareTemplateCsv(context)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("📤 Excel / CSV ફાઇલ અપલોડ કરો") },
                            leadingIcon = { Icon(Icons.Default.CloudUpload, contentDescription = null, tint = Color(0xFF2E7D32)) },
                            onClick = {
                                showOptionsMenu = false
                                filePickerLauncher.launch("*/*")
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("📋 લખાણ / ટેક્સ્ટ પેસ્ટ કરો") },
                            leadingIcon = { Icon(Icons.Default.ContentPaste, contentDescription = null, tint = Color(0xFFE65100)) },
                            onClick = {
                                showOptionsMenu = false
                                showPasteCsvDialog = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("💾 હાલના વિદ્યાર્થીઓ એક્સપોર્ટ કરો") },
                            leadingIcon = { Icon(Icons.Default.FileDownload, contentDescription = null, tint = Color(0xFF1565C0)) },
                            onClick = {
                                showOptionsMenu = false
                                viewModel.exportCurrentStudentsCsv(context)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("🔄 નમૂના વિદ્યાર્થીઓ રીસેટ કરો") },
                            leadingIcon = { Icon(Icons.Default.RestartAlt, contentDescription = null, tint = Color.Red) },
                            onClick = {
                                showOptionsMenu = false
                                viewModel.resetData()
                            }
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
            // Excel Upload Banner Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(2.dp, RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFDCFCE7)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.TableChart,
                                contentDescription = null,
                                tint = Color(0xFF166534),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Excel / CSV ફાઇલ દ્વારા વિદ્યાર્થીઓ ઉમેરો",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF166534)
                            )
                            Text(
                                text = "Excel નમૂનો ડાઉનલોડ કરી વિગતો ભરો અને અપલોડ કરતાં જ બધા વિદ્યાર્થીઓ કાયમી સેવ થઈ જશે.",
                                fontSize = 11.sp,
                                color = Color(0xFF15803D),
                                lineHeight = 15.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // 1. Download Template Button
                        OutlinedButton(
                            onClick = { viewModel.shareTemplateCsv(context) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FileDownload,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = Color(0xFF166534)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("1. નમૂનો ડાઉનલોડ", fontSize = 11.sp, color = Color(0xFF166534), fontWeight = FontWeight.Bold)
                        }

                        // 2. Upload Excel/CSV File Button
                        Button(
                            onClick = { filePickerLauncher.launch("*/*") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF166534)),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FileUpload,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("2. ફાઇલ અપલોડ", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Standard filter dropdown
            StandardSelectorDropdown(
                standards = listOf("તમામ ધોરણ") + SchoolStandards.ALL,
                selected = selectedStandard,
                onSelected = { viewModel.setSelectedStandard(it) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Student count summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "કુલ વિદ્યાર્થીઓ: ${filteredStudents.size}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondaryLight
                )

                TextButton(onClick = { viewModel.exportCurrentStudentsCsv(context) }) {
                    Icon(
                        imageVector = Icons.Default.FileDownload,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = SchoolPrimary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Excel ડાઉનલોડ", fontSize = 12.sp, color = SchoolPrimary)
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

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

        // Add Student Dialog (Manual Single Student)
        if (showAddDialog) {
            AddStudentDialog(
                standards = SchoolStandards.ALL,
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
                title = { Text("વિદ્યાર્થી રદ્દ કરો?") },
                text = { Text("${student.name} (GR: ${student.grNo}) ને ડેટાબેઝમાંથી કાયમી રદ્દ કરવા માંગો છો?") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteStudent(student)
                            studentToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) {
                        Text("રદ્દ કરો")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { studentToDelete = null }) {
                        Text("ના")
                    }
                }
            )
        }

        // Excel / CSV Import Confirmation & Preview Dialog
        importedStudentsPreview?.let { parsedStudents ->
            ImportPreviewDialog(
                students = parsedStudents,
                onDismiss = { importedStudentsPreview = null },
                onConfirm = { replaceExisting ->
                    viewModel.saveImportedStudents(parsedStudents, replaceExisting)
                    importedStudentsPreview = null
                    Toast.makeText(context, "${parsedStudents.size} વિદ્યાર્થીઓ કાયમી સેવ થઈ ગયા છે!", Toast.LENGTH_SHORT).show()
                }
            )
        }

        // Paste CSV text dialog
        if (showPasteCsvDialog) {
            PasteCsvDialog(
                onDismiss = { showPasteCsvDialog = false },
                onImportText = { text ->
                    viewModel.parseStudentsFromText(text) { result ->
                        result.onSuccess { parsedList ->
                            showPasteCsvDialog = false
                            importedStudentsPreview = parsedList
                        }.onFailure { ex ->
                            Toast.makeText(context, "ભૂલ: ${ex.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            )
        }
    }
}

/**
 * Excel ફાઇલમાંથી વંચાયેલા વિદ્યાર્થીઓનું પ્રિવ્યુ અને સેવ કરવાનો ડાયલોગ
 */
@Composable
fun ImportPreviewDialog(
    students: List<Student>,
    onDismiss: () -> Unit,
    onConfirm: (replaceExisting: Boolean) -> Unit
) {
    var replaceExisting by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFDCFCE7)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.TableChart,
                            contentDescription = null,
                            tint = Color(0xFF166534),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Excel ફાઇલ પ્રિવ્યુ",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF166534)
                        )
                        Text(
                            text = "કુલ ${students.size} વિદ્યાર્થીઓની માહિતી મળી",
                            fontSize = 12.sp,
                            color = TextSecondaryLight
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "ચકાસણી (પ્રથમ ૫ વિદ્યાર્થીઓ):",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimaryLight
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Scrollable preview table
                Surface(
                    color = Color(0xFFF8FAFC),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                ) {
                    LazyColumn(modifier = Modifier.padding(8.dp)) {
                        items(students.take(8)) { st ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${st.rollNo}.",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SchoolPrimary,
                                    modifier = Modifier.width(28.dp)
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = st.name,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = TextPrimaryLight
                                    )
                                    Text(
                                        text = "${st.standard} | GR: ${st.grNo} | ${st.genderLabel} | મો: ${st.parentPhone}",
                                        fontSize = 10.sp,
                                        color = TextSecondaryLight
                                    )
                                }
                            }
                        }
                    }
                }

                if (students.size > 8) {
                    Text(
                        text = "...અને બીજા ${students.size - 8} વિદ્યાર્થીઓ",
                        fontSize = 11.sp,
                        color = TextSecondaryLight,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Mode Selection
                Text("સેવ કરવાનો વિકલ્પ:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = !replaceExisting,
                        onClick = { replaceExisting = false },
                        colors = RadioButtonDefaults.colors(selectedColor = SchoolPrimary)
                    )
                    Text("હાલના ડેટામાં ઉમેરો (Merge)", fontSize = 12.sp, modifier = Modifier.clickable { replaceExisting = false })
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = replaceExisting,
                        onClick = { replaceExisting = true },
                        colors = RadioButtonDefaults.colors(selectedColor = Color.Red)
                    )
                    Text("જૂનો ડેટા કાઢીને નવો સેવ કરો (Replace)", fontSize = 12.sp, modifier = Modifier.clickable { replaceExisting = true })
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("રદ કરો")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onConfirm(replaceExisting) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF166534))
                    ) {
                        Text("💾 કાયમી સેવ કરો", color = Color.White)
                    }
                }
            }
        }
    }
}

/**
 * ટેક્સ્ટ પેસ્ટ કરવાનો ડાયલોગ (Copy-Paste Dialog)
 */
@Composable
fun PasteCsvDialog(
    onDismiss: () -> Unit,
    onImportText: (String) -> Unit
) {
    var textContent by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("CSV / ટેક્સ્ટ પેસ્ટ કરો") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Excel અથવા WhatsApp માંથી વિદ્યાર્થીઓની લાઇન કોપી કરીને અહીં પેસ્ટ કરો:\n(ફોર્મેટ: રોલ, GR, નામ, ધોરણ, જાતિ, મોબાઇલ)",
                    fontSize = 12.sp,
                    color = TextSecondaryLight
                )
                OutlinedTextField(
                    value = textContent,
                    onValueChange = { textContent = it },
                    placeholder = { Text("1, 101, પટેલ આરવ, બાલવાટિકા, કુમાર, 9825123401") },
                    minLines = 5,
                    maxLines = 8,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onImportText(textContent) },
                enabled = textContent.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = SchoolPrimary)
            ) {
                Text("ચકાસો અને આગળ વધો")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("રદ કરો")
            }
        }
    )
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
            .clickable(onClick = onViewCard),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Roll No Circle
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (student.isKumar) Color(0xFFE3F2FD) else Color(0xFFFCE4EC)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${student.rollNo}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (student.isKumar) BoyBlue else GirlPink
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = student.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimaryLight
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "GR: ${student.grNo}",
                        fontSize = 11.sp,
                        color = TextSecondaryLight
                    )
                    Text("•", fontSize = 11.sp, color = TextSecondaryLight)
                    Text(
                        text = student.standard,
                        fontSize = 11.sp,
                        color = TextSecondaryLight
                    )
                    Text("•", fontSize = 11.sp, color = TextSecondaryLight)
                    Text(
                        text = student.genderLabel,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (student.isKumar) BoyBlue else GirlPink
                    )
                }
                if (student.parentPhone.isNotBlank()) {
                    Text(
                        text = "વાલી મો: ${student.parentPhone}",
                        fontSize = 11.sp,
                        color = Color(0xFF0F766E),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // QR Code icon preview shortcut
            IconButton(onClick = onViewCard) {
                Icon(
                    imageVector = Icons.Default.QrCode,
                    contentDescription = "QR જુઓ",
                    tint = SchoolPrimary
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "રદ્દ કરો",
                    tint = Color.LightGray,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * વિદ્યાર્થી ઓળખ કાર્ડ (Student ID Card Dialog)
 */
@Composable
fun StudentIdCardDialog(
    student: Student,
    onDismiss: () -> Unit,
    onTestScan: () -> Unit
) {
    val qrBitmap: Bitmap? = remember(student.qrCode) {
        QrCodeUtil.generateQrBitmap(student.qrCode, 350)
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // School Header on ID Card
                SchoolLogoBadge(
                    size = 62.dp,
                    showDetails = false
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "નગર પ્રાથમિક શાળા નં ૨૯\nપુરુષોતમનગર બાકરોલ (આણંદ)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = Color(0xFFBA1B1D)
                )

                Text(
                    text = "વિદ્યાર્થી ઓળખ કાર્ડ (Student ID Card)",
                    fontSize = 11.sp,
                    color = TextSecondaryLight
                )

                Spacer(modifier = Modifier.height(12.dp))

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

                Spacer(modifier = Modifier.height(12.dp))

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

                if (student.parentPhone.isNotBlank()) {
                    val context = LocalContext.current
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        color = Color(0xFFF1F5F9),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "વાલી મો: ${student.parentPhone}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimaryLight
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                TextButton(
                                    onClick = {
                                        val msg = CommunicationUtil.createAbsentMessage(
                                            student.name, student.standard, student.rollNo, "આજે"
                                        )
                                        CommunicationUtil.sendWhatsApp(context, student.parentPhone, msg)
                                    },
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(4.dp)
                                ) {
                                    Text("WhatsApp", fontSize = 11.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                                }

                                TextButton(
                                    onClick = {
                                        val msg = CommunicationUtil.createAbsentMessage(
                                            student.name, student.standard, student.rollNo, "આજે"
                                        )
                                        CommunicationUtil.sendSms(context, student.parentPhone, msg)
                                    },
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(4.dp)
                                ) {
                                    Text("SMS", fontSize = 11.sp, color = SchoolPrimary, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

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

                Spacer(modifier = Modifier.height(6.dp))

                TextButton(onClick = onDismiss) {
                    Text("બંધ કરો (Close)")
                }
            }
        }
    }
}

/**
 * મેન્યુઅલ એક વિદ્યાર્થી ઉમેરવાનો ડાયલોગ
 */
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
    var standard by remember { mutableStateOf(standards.firstOrNull() ?: SchoolStandards.STD_1) }
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
                        division = "અ",
                        parentPhone = phone.trim(),
                        qrCode = gr
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
                Text("રદ કરો")
            }
        }
    )
}
