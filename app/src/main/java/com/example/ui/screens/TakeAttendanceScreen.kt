package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.data.model.Student
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
import com.example.util.QrCodeAnalyzer
import kotlinx.coroutines.flow.collectLatest
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TakeAttendanceScreen(
    viewModel: AttendanceViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.navigateTo(AppScreen.HOME)
    }

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val allStudents by viewModel.allStudents.collectAsState()
    val allStandards by viewModel.allStandards.collectAsState()
    val selectedStandard by viewModel.selectedStandard.collectAsState()
    val attendanceRecords by viewModel.attendanceForSelectedDate.collectAsState()
    val recentlyScanned by viewModel.recentlyScannedStudent.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    LaunchedEffect(viewModel.scanMessage) {
        viewModel.scanMessage.collectLatest { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    var manualInput by remember { mutableStateOf("") }
    var showFinalizeDialog by remember { mutableStateOf(false) }

    // Filter students by selected standard
    val filteredStudents = remember(allStudents, selectedStandard) {
        if (selectedStandard == "તમામ ધોરણ" || selectedStandard.isBlank()) {
            allStudents
        } else {
            allStudents.filter { it.standard == selectedStandard }
        }
    }

    // Determine present vs unscanned students
    val presentGrMap = remember(attendanceRecords) {
        attendanceRecords.filter { it.isPresent }.associateBy { it.studentGrNo }
    }

    val presentCount = remember(filteredStudents, presentGrMap) {
        filteredStudents.count { presentGrMap.containsKey(it.grNo) }
    }
    val unscannedCount = filteredStudents.size - presentCount

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Take Attendance (હાજરી લો)",
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
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = BackgroundLight,
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Standard / Class Selection
            StandardSelectorDropdown(
                standards = listOf("તમામ ધોરણ") + com.example.data.model.SchoolStandards.ALL,
                selected = selectedStandard,
                onSelected = { viewModel.setSelectedStandard(it) }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Camera QR Scanner Viewport
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .shadow(3.dp, RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Black)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (hasCameraPermission) {
                        CameraScanPreview(
                            onQrScanned = { code ->
                                viewModel.onQrScanned(code)
                            }
                        )
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Camera Permission",
                                tint = Color.White,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "કેમેરા પરવાનગી જરૂરી છે",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                                colors = ButtonDefaults.buttonColors(containerColor = SchoolPrimary)
                            ) {
                                Text("પરવાનગી આપો (Allow Camera)")
                            }
                        }
                    }

                    // Viewfinder Scan Frame Overlay
                    Box(
                        modifier = Modifier
                            .size(190.dp)
                            .border(2.dp, Color(0xFF00E676), RoundedCornerShape(12.dp))
                    )

                    // Top Scan hint
                    Surface(
                        color = Color.Black.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCodeScanner,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "ઓળખ કાર્ડનો QR કોડ સ્કેન કરો",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Recently Scanned Student Card Notification
            AnimatedVisibility(
                visible = recentlyScanned != null,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                recentlyScanned?.let { student ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(2.dp, RoundedCornerShape(8.dp)),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = PresentGreenBg)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Present",
                                tint = PresentGreen,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = student.name,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PresentGreen
                                )
                                Text(
                                    text = "રોલ નં: ${student.rollNo} | ${student.standard} | ${if (student.isKumar) "કુમાર" else "કન્યા"}",
                                    fontSize = 13.sp,
                                    color = TextPrimaryLight
                                )
                            }
                            Text(
                                text = "હાજર!",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = PresentGreen
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Quick / Manual Entry Section (For emulator and quick testing)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(1.dp, RoundedCornerShape(8.dp)),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "મેન્યુઅલ અથવા ઝડપી સ્કેન (Quick / Manual Scan)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimaryLight
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = manualInput,
                            onValueChange = { manualInput = it },
                            placeholder = { Text("રોલ નં અથવા GR નં (e.g. 1001)") },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("manual_qr_input")
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (manualInput.isNotBlank()) {
                                    viewModel.onQrScanned(manualInput.trim())
                                    manualInput = ""
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SchoolPrimary),
                            modifier = Modifier.testTag("btn_manual_scan")
                        ) {
                            Text("હાજર")
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "વિદ્યાર્થી પર ક્લિક કરીને પણ હાજર કરી શકો છો:",
                        fontSize = 12.sp,
                        color = TextSecondaryLight
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Quick Tap List of students in selected standard
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(filteredStudents) { student ->
                            val isPresent = presentGrMap.containsKey(student.grNo)
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (isPresent) PresentGreenBg else Color(0xFFF1F5F9),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isPresent) PresentGreen else Color(0xFFCBD5E1)
                                ),
                                modifier = Modifier.clickable {
                                    if (isPresent) {
                                        viewModel.markStudentAbsentDirectly(student)
                                    } else {
                                        viewModel.markStudentPresentDirectly(student)
                                    }
                                }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "${student.rollNo}. ${student.name.split(" ").first()}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (isPresent) PresentGreen else TextPrimaryLight
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isPresent) "✓" else "+",
                                        fontWeight = FontWeight.Bold,
                                        color = if (isPresent) PresentGreen else SchoolPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Today's Live Progress Bar
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(2.dp, RoundedCornerShape(8.dp)),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "કુલ વિદ્યાર્થી",
                            fontSize = 13.sp,
                            color = TextSecondaryLight
                        )
                        Text(
                            text = "${filteredStudents.size}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryLight
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "હાજર (Present)",
                            fontSize = 13.sp,
                            color = PresentGreen
                        )
                        Text(
                            text = "$presentCount",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = PresentGreen
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "બાકી (Unscanned)",
                            fontSize = 13.sp,
                            color = AbsentRed
                        )
                        Text(
                            text = "$unscannedCount",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = AbsentRed
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Complete Attendance Button
            Button(
                onClick = { showFinalizeDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("btn_complete_attendance"),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SchoolPrimary)
            ) {
                Icon(imageVector = Icons.Default.DoneAll, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "હાજરી પૂર્ણ કરો (Complete Attendance)",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "નોંધ: કાર્ડ સ્કેન ન થયેલા તમામ વિદ્યાર્થીઓ આપમેળે 'ગેરહાજર' તરીકે નોંધાશે.",
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                color = TextSecondaryLight,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))
        }

        // Finalize Confirmation Dialog
        if (showFinalizeDialog) {
            AlertDialog(
                onDismissRequest = { showFinalizeDialog = false },
                title = { Text("હાજરી પૂર્ણ કરવી છે?") },
                text = {
                    Text(
                        "સ્કેન થયેલ વિદ્યાર્થીઓ ($presentCount) 'હાજર' રહેશે અને બાકીના ($unscannedCount) વિદ્યાર્થીઓ 'ગેરહાજર' તરીકે નોંધાશે."
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.finalizeAttendance()
                            showFinalizeDialog = false
                            viewModel.navigateTo(AppScreen.VIEW_ATTENDANCE)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SchoolPrimary)
                    ) {
                        Text("હા, સબમિટ કરો")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showFinalizeDialog = false }) {
                        Text("રદ્દ કરો")
                    }
                }
            )
        }
    }
}

@Composable
fun CameraScanPreview(
    onQrScanned: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()

                imageAnalysis.setAnalyzer(
                    cameraExecutor,
                    QrCodeAnalyzer { scannedQr ->
                        onQrScanned(scannedQr)
                    }
                )

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageAnalysis
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, ContextCompat.getMainExecutor(ctx))

            previewView
        },
        modifier = Modifier.fillMaxSize()
    )

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StandardSelectorDropdown(
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
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            standards.forEach { std ->
                DropdownMenuItem(
                    text = { Text(std) },
                    onClick = {
                        onSelected(std)
                        expanded = false
                    }
                )
            }
        }
    }
}
