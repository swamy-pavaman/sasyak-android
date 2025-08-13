package com.kapilagro.sasyak.presentation.yield

import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import coil.compose.rememberAsyncImagePainter
import com.kapilagro.sasyak.di.IoDispatcher
import com.kapilagro.sasyak.domain.models.YieldDetails
import com.kapilagro.sasyak.presentation.common.catalog.CategoriesState
import com.kapilagro.sasyak.presentation.common.catalog.CategoryViewModel
import com.kapilagro.sasyak.presentation.common.components.SuccessDialog
import com.kapilagro.sasyak.presentation.common.image.ImageCaptureViewModel
import com.kapilagro.sasyak.presentation.common.navigation.Screen
import com.kapilagro.sasyak.presentation.common.theme.AgroPrimary
import com.kapilagro.sasyak.presentation.home.HomeViewModel
import com.kapilagro.sasyak.presentation.tasks.TaskViewModel
import com.kapilagro.sasyak.worker.AttachUrlWorker
import com.kapilagro.sasyak.worker.FileUploadWorker
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import com.kapilagro.sasyak.presentation.common.components.TaskSubmittedDialog
import com.kapilagro.sasyak.utils.NetworkUtils

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YieldRequestScreen(
    onTaskCreated: () -> Unit,
    onBackClick: () -> Unit,
    navController: NavController,
    viewModel: YieldListViewModel = hiltViewModel(),
    homeViewModel: HomeViewModel = hiltViewModel(),
    taskViewModel: TaskViewModel = hiltViewModel(),
    categoryViewModel: CategoryViewModel = hiltViewModel(),
    @IoDispatcher ioDispatcher: CoroutineDispatcher,

    ) {
    val createYieldState by viewModel.createYieldState.collectAsState()
    val userRole by homeViewModel.userRole.collectAsState()
    val supervisorsListState by homeViewModel.supervisorsListState.collectAsState()
    val categoriesStates by categoryViewModel.categoriesStates.collectAsState()
    val scope = rememberCoroutineScope()
    val managersList by taskViewModel.managersList.collectAsState()
    val supervisorsList by taskViewModel.supervisorsList.collectAsState()

    val context = LocalContext.current
    var imageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }

    // Dialog state
    var showSuccessDialog by remember { mutableStateOf(false) }
    var submittedEntry by remember { mutableStateOf<YieldDetails?>(null) }
    var showWorkerDialog by remember { mutableStateOf(false) }


    // Form fields with saved state
    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
    var harvestDate by remember { mutableStateOf(
        savedStateHandle?.get<String>("harvestDate")
            ?: LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
    ) }
    var cropName by remember { mutableStateOf(savedStateHandle?.get<String>("cropName") ?: "") }
    var cropNameExpanded by remember { mutableStateOf(false) }
    var row by remember { mutableStateOf(savedStateHandle?.get<String>("row") ?: "") }
    var rowExpanded by remember { mutableStateOf(false) }
    var fieldArea by remember { mutableStateOf(savedStateHandle?.get<String>("fieldArea") ?: "") }
    var yieldQuantity by remember { mutableStateOf(savedStateHandle?.get<String>("yieldQuantity") ?: "") }
    var yieldUnit by remember { mutableStateOf(savedStateHandle?.get<String>("yieldUnit") ?: "kg") }
    var yieldUnitExpanded by remember { mutableStateOf(false) }
    var qualityGrade by remember { mutableStateOf(savedStateHandle?.get<String>("qualityGrade") ?: "") }
    var qualityGradeExpanded by remember { mutableStateOf(false) }
    var moistureContent by remember { mutableStateOf(savedStateHandle?.get<String>("moistureContent") ?: "") }
    var harvestMethod by remember { mutableStateOf(savedStateHandle?.get<String>("harvestMethod") ?: "") }
    var harvestMethodExpanded by remember { mutableStateOf(false) }
    var notes by remember { mutableStateOf(savedStateHandle?.get<String>("notes") ?: "") }
    var description by remember { mutableStateOf(savedStateHandle?.get<String>("description") ?: "") }
    var assignedTo by remember { mutableStateOf<Int?>(savedStateHandle?.get<Int>("assignedTo")) }
    var assignedToExpanded by remember { mutableStateOf(false) }
    var valveName by remember { mutableStateOf(savedStateHandle?.get<String>("valveName") ?: "") }
    var valveNameExpanded by remember { mutableStateOf(false) }

    var dueDateText by remember {
        mutableStateOf(
            savedStateHandle?.get<String>("dueDate")
                ?: LocalDate.now().plusDays(7).format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
        )
    }
    var showDatePicker by remember { mutableStateOf(false) }
    // State for selected role and user
    var selectedRole by remember { mutableStateOf(savedStateHandle?.get<String>("selectedRole") ?:"Manager") }
    var selectedUser by remember { mutableStateOf(savedStateHandle?.get<String>("selectedUser") ?:"") }


    val datePattern = Regex("\\d{2}-\\d{2}-\\d{4}")
    val isValidDueDate = dueDateText.matches(datePattern) && try {
        LocalDate.parse(dueDateText, DateTimeFormatter.ofPattern("dd-MM-yyyy")).isAfter(LocalDate.now())
    } catch (e: Exception) {
        false
    }

    // Save form state before navigating to ImageCaptureScreen
    LaunchedEffect(Unit) {
        snapshotFlow {
            mapOf(
                "harvestDate" to harvestDate,
                "cropName" to cropName,
                "row" to row,
                "fieldArea" to fieldArea,
                "yieldQuantity" to yieldQuantity,
                "yieldUnit" to yieldUnit,
                "qualityGrade" to qualityGrade,
                "moistureContent" to moistureContent,
                "harvestMethod" to harvestMethod,
                "notes" to notes,
                "description" to description,
                "assignedTo" to assignedTo,
                "dueDate" to dueDateText,
                "selectedRole" to selectedRole,
                "selectedUser" to selectedUser,
                "valveName" to valveName
            )
        }.collect { state ->
            state.forEach { (key, value) ->
                savedStateHandle?.set(key, value)
            }
        }
    }
    LaunchedEffect(Unit) {
        if (categoriesStates["Valve"] !is CategoriesState.Success) {
            categoryViewModel.fetchCategories("Valve")
        }
    }

    val valveDetails = when (val state = categoriesStates["Valve"]) {
        is CategoriesState.Success -> {
            state.valveDetails
        }else -> {
            emptyMap()
        }
    }
    // Dynamic lists
    val valves = when (val state = categoriesStates["Valve"]) {
        is CategoriesState.Success -> state.categories.map { it.value }
        else -> emptyList()
    }

    val crops by remember(valveName) {
        derivedStateOf {
            val cropList = valveDetails[valveName]?.keys?.toList() ?: emptyList()
            cropList
        }
    }

    val rows by remember(valveName, cropName) {
        derivedStateOf {
            val rowList =
                valveDetails[valveName]?.get(cropName)?.rows?.keys?.toList() ?: emptyList()
            rowList
        }
    }

    val units = listOf("kg", "tonnes", "quintals", "bags")

    val grades = listOf("A", "B", "C", "Premium", "Standard", "Low")

    val harvestMethods = listOf(
        "Manual", "Combine Harvester", "Mechanical", "Semi-mechanical"
    )
    val previewData by viewModel.previewData.collectAsState()
    val location by categoryViewModel.location.collectAsState()

    // Load supervisors list for MANAGER role
    LaunchedEffect(Unit) {
        if (userRole == "MANAGER" && supervisorsListState !is HomeViewModel.SupervisorsListState.Success) {
            homeViewModel.loadSupervisorsList()
        }
    }
    // Load managers and supervisors lists for admin
    LaunchedEffect(Unit) {
        if ((userRole == "ADMIN") && (managersList.isEmpty() || supervisorsList.isEmpty())) {
            taskViewModel.fetchManagers()
            taskViewModel.fetchSupervisors()
        }
    }

    // Handle navigation result from ImageCaptureScreen
    LaunchedEffect(navController) {
        navController.currentBackStackEntry
            ?.savedStateHandle
            ?.getStateFlow<List<String>>("selectedImages", emptyList())
            ?.collect { uriStrings ->
                imageUris = uriStrings.map { Uri.parse(it) } // <-- CHANGE THIS LOGIC
                // Also update the saved state handle so URIs persist on configuration change
                savedStateHandle?.set("selectedImages", uriStrings)
            }
    }

    // Handle task creation success
    LaunchedEffect(createYieldState) {
        when (createYieldState) {
            is YieldListViewModel.CreateYieldState.Success -> {
                val createdTask = (createYieldState as YieldListViewModel.CreateYieldState.Success).task
                if (imageUris.isNotEmpty()) {
                    val imageFilePaths = imageUris.mapNotNull { uri ->
                        ImageCaptureViewModel.copyUriToCachedFile(context, uri)?.absolutePath
                    }

                    val constraints = Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()


                    val fileUploadRequest = OneTimeWorkRequestBuilder<FileUploadWorker>()
                        .setInputData(
                            FileUploadWorker.createInputData(
                                taskId = createdTask.id,
                                imagePaths = imageFilePaths,
                                folder = "YIELD"
                            )
                        )
                        .setConstraints(constraints)
                        .addTag(FileUploadWorker.UPLOAD_TAG)
                        .build()

                    val attachUrlRequest = OneTimeWorkRequestBuilder<AttachUrlWorker>()
                        .setConstraints(constraints)
                        .build()

                    WorkManager.getInstance(context)
                        .beginWith(fileUploadRequest)
                        .then(attachUrlRequest)
                        .enqueue()
                }
                showSuccessDialog = true
            }
            is YieldListViewModel.CreateYieldState.Error -> {
                if (submittedEntry != null && !NetworkUtils.isNetworkAvailable(context)) {
                    val imageFilePaths = imageUris.mapNotNull { uri ->
                        // Use the new static method from the ViewModel's companion object.
                        ImageCaptureViewModel.copyUriToCachedFile(context, uri)?.absolutePath
                    }
                    Log.d("WORKER", "WorkerRequest started")
                    viewModel.workerYieldTask(
                        context = context,
                        yieldDetails = submittedEntry!!,
                        description = description,
                        imagesJson = imageFilePaths,
                        assignedToId = if (userRole == "MANAGER" || userRole == "ADMIN") assignedTo else null
                    )
                    showWorkerDialog = true
                }
            }

            else -> Unit
        }
    }

    if (showWorkerDialog) {
        TaskSubmittedDialog(
            navController = navController,
            onDismiss = { showWorkerDialog = false }
        )
    }

    // Resets dependent fields
    LaunchedEffect(valveName) {
        if (valveName.isEmpty()) {
            cropName = ""
            row = ""
        }
    }

    LaunchedEffect(cropName) {
        if (cropName.isEmpty()) {
            row = ""
        }
    }

    // Success Dialog
    if (showSuccessDialog && submittedEntry != null) {
        val details = listOf(
            "Date" to submittedEntry!!.harvestDate,
            "Crop Name" to submittedEntry!!.cropName,
            "Row" to submittedEntry!!.row.toString(),
            "Field Area" to (submittedEntry!!.fieldArea?.plus(" acres") ?: "Not specified"),
            "Yield" to "${submittedEntry!!.yieldQuantity} ${submittedEntry!!.yieldUnit}",
            "Quality" to (submittedEntry!!.qualityGrade ?: "Not specified"),
            "Moisture" to (submittedEntry!!.moistureContent?.plus("%") ?: "Not specified"),
            "Method" to (submittedEntry!!.harvestMethod ?: "Not specified")
        )

        SuccessDialog(
            title = "Yield Report Sent!",
            message = if (userRole=="SUPERVISOR") "This report has been sent to the manager." else "This report has been sent to the $selectedUser.",
            details = details,
            description = description,
            primaryButtonText = "OK",
            onPrimaryButtonClick = {
                showSuccessDialog = false
                viewModel.clearCreateYieldState()
                onTaskCreated()
                // Clear saved state after successful submission
                savedStateHandle?.remove<String>("harvestDate")
                savedStateHandle?.remove<String>("cropName")
                savedStateHandle?.remove<String>("row")
                savedStateHandle?.remove<String>("fieldArea")
                savedStateHandle?.remove<String>("yieldQuantity")
                savedStateHandle?.remove<String>("yieldUnit")
                savedStateHandle?.remove<String>("qualityGrade")
                savedStateHandle?.remove<String>("moistureContent")
                savedStateHandle?.remove<String>("harvestMethod")
                savedStateHandle?.remove<String>("notes")
                savedStateHandle?.remove<String>("description")
                savedStateHandle?.remove<Int>("assignedTo")
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Yield Report") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        if (previewData != null) {
                            valveName = previewData?.valueName ?: valveName
                            cropName = previewData?.cropName ?: cropName
                            row = previewData?.row ?: row
                        }
                    })
                    {
                        Icon(Icons.Default.Autorenew, contentDescription = "Preview")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Harvest Date (Display only)
            OutlinedTextField(
                value = "Harvest date: $harvestDate",
                onValueChange = { },
                enabled = false,
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Valve Name Dropdown
            ExposedDropdownMenuBox(
                expanded = valveNameExpanded,
                onExpandedChange = { valveNameExpanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = valveName,
                    readOnly = false,
                    onValueChange = { newValue ->
                        valveName = newValue
                        valveNameExpanded = true
                    },
                    label = { Text("Valve name *") },
                    trailingIcon = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ){
                            if (valveName.isNotEmpty()) {
                                IconButton(onClick = { valveName = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear valve name",
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = valveNameExpanded)
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    shape = RoundedCornerShape(8.dp)
                )

                ExposedDropdownMenu(
                    expanded = valveNameExpanded,
                    onDismissRequest = { valveNameExpanded = false }
                ) {
                    valves
                        .filter { it.contains(valveName, ignoreCase = true) }
                        .forEach { valve ->
                            DropdownMenuItem(
                                text = { Text(valve)},
                                onClick = {
                                    valveName = valve
                                    valveNameExpanded = false
                                }
                            )
                        }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Crop Name Dropdown
            ExposedDropdownMenuBox(
                expanded = cropNameExpanded,
                onExpandedChange = { cropNameExpanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = cropName,
                    readOnly = false,
                    onValueChange = { newValue ->
                        cropName = newValue
                        cropNameExpanded = true
                    },
                    label = { Text("Crop name *") },
                    trailingIcon = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ){
                            if (cropName.isNotEmpty()) {
                                IconButton(onClick = { cropName = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear crop name",
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = cropNameExpanded)
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    shape = RoundedCornerShape(8.dp)
                )

                ExposedDropdownMenu(
                    expanded = cropNameExpanded,
                    onDismissRequest = { cropNameExpanded = false }
                ) {
                    crops
                        .filter { it.contains(cropName, ignoreCase = true) }
                        .forEach { crop ->
                            DropdownMenuItem(
                                text = { Text(crop) },
                                onClick = {
                                    cropName = crop
                                    cropNameExpanded = false
                                }
                            )
                        }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Row Dropdown
            ExposedDropdownMenuBox(
                expanded = rowExpanded,
                onExpandedChange = { rowExpanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = row,
                    readOnly = false,
                    onValueChange = { newValue ->
                        row = newValue
                        rowExpanded = true
                    },
                    label = { Text("Row *") },
                    trailingIcon = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (row.isNotEmpty()) {
                                IconButton(onClick = { row = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear row",
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = rowExpanded)
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    shape = RoundedCornerShape(8.dp)
                )

                ExposedDropdownMenu(
                    expanded = rowExpanded,
                    onDismissRequest = { rowExpanded = false }
                ) {
                    rows
                        .filter { it.contains(row, ignoreCase = true) }
                        .forEach { rowItem ->
                            DropdownMenuItem(
                                text = { Text(rowItem) },
                                onClick = {
                                    row = rowItem
                                    rowExpanded = false
                                }
                            )
                        }
                }
            }


            Spacer(modifier = Modifier.height(16.dp))

            // Field Area
            OutlinedTextField(
                value = fieldArea,
                onValueChange = { fieldArea = it },
                label = { Text("Field area (acres)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Yield Quantity and Unit
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = yieldQuantity,
                    onValueChange = { yieldQuantity = it },
                    label = { Text("Yield quantity *") },
                    modifier = Modifier.weight(0.6f),
                    shape = RoundedCornerShape(8.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )

                ExposedDropdownMenuBox(
                    expanded = yieldUnitExpanded,
                    onExpandedChange = { yieldUnitExpanded = it },
                    modifier = Modifier.weight(0.4f)
                ) {
                    OutlinedTextField(
                        value = yieldUnit,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Unit *") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = yieldUnitExpanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    ExposedDropdownMenu(
                        expanded = yieldUnitExpanded,
                        onDismissRequest = { yieldUnitExpanded = false }
                    ) {
                        units.forEach { unit ->
                            DropdownMenuItem(
                                text = { Text(unit) },
                                onClick = {
                                    yieldUnit = unit
                                    yieldUnitExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Quality Grade Dropdown
            ExposedDropdownMenuBox(
                expanded = qualityGradeExpanded,
                onExpandedChange = { qualityGradeExpanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = qualityGrade,
                    readOnly = true,
                    onValueChange = { newValue ->
                        qualityGrade = newValue
                        qualityGradeExpanded = true
                    },
                    label = { Text("Quality grade") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = qualityGradeExpanded)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    shape = RoundedCornerShape(8.dp)
                )

                ExposedDropdownMenu(
                    expanded = qualityGradeExpanded,
                    onDismissRequest = { qualityGradeExpanded = false }
                ) {
                    grades
                        .forEach { grade ->
                            DropdownMenuItem(
                                text = { Text(grade) },
                                onClick = {
                                    qualityGrade = grade
                                    qualityGradeExpanded = false
                                }
                            )
                        }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Moisture Content
            OutlinedTextField(
                value = moistureContent,
                onValueChange = { moistureContent = it },
                label = { Text("Moisture content (%)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Harvest Method Dropdown
            ExposedDropdownMenuBox(
                expanded = harvestMethodExpanded,
                onExpandedChange = { harvestMethodExpanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = harvestMethod,
                    readOnly = true,
                    onValueChange = { newValue ->
                        harvestMethod = newValue
                        harvestMethodExpanded = true
                    },
                    label = { Text("Harvest method") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = harvestMethodExpanded)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    shape = RoundedCornerShape(8.dp)
                )

                ExposedDropdownMenu(
                    expanded = harvestMethodExpanded,
                    onDismissRequest = { harvestMethodExpanded = false }
                ) {
                    harvestMethods
                        .forEach { method ->
                            DropdownMenuItem(
                                text = { Text(method) },
                                onClick = {
                                    harvestMethod = method
                                    harvestMethodExpanded = false
                                }
                            )
                        }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Assign to Dropdown (for MANAGER role)
            if (userRole == "MANAGER") {
                val supervisors = when (supervisorsListState) {
                    is HomeViewModel.SupervisorsListState.Success ->
                        (supervisorsListState as HomeViewModel.SupervisorsListState.Success).supervisors
                    else -> emptyList()
                }

                val selectedSupervisorName = supervisors.find { it.supervisorId == assignedTo }?.supervisorName ?: ""

                ExposedDropdownMenuBox(
                    expanded = assignedToExpanded,
                    onExpandedChange = { assignedToExpanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedSupervisorName,
                        onValueChange = {}, // Read-only
                        label = { Text("Assign to *") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = assignedToExpanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        readOnly = true,
                        shape = RoundedCornerShape(8.dp)
                    )

                    ExposedDropdownMenu(
                        expanded = assignedToExpanded,
                        onDismissRequest = { assignedToExpanded = false }
                    ) {
                        supervisors.forEach { supervisor ->
                            DropdownMenuItem(
                                text = { Text(supervisor.supervisorName) },
                                onClick = {
                                    assignedTo = supervisor.supervisorId
                                    selectedUser = supervisor.supervisorName
                                    assignedToExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
            if (userRole == "ADMIN") {
                val managerList = managersList
                val supervisorList = supervisorsList

                // State for dropdown
                var expanded by remember { mutableStateOf(false) }


                val userList = if (selectedRole == "Manager") managerList else supervisorList

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Text(
                        text = "Select Role",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedRole == "Manager",
                            onClick = {
                                selectedRole = "Manager"
                                assignedTo = null
                                selectedUser = "" // reset dropdown selection
                            }
                        )
                        Text(text = "Manager")

                        Spacer(modifier = Modifier.width(16.dp))

                        RadioButton(
                            selected = selectedRole == "Supervisor",
                            onClick = {
                                selectedRole = "Supervisor"
                                assignedTo = null
                                selectedUser = "" // reset dropdown selection
                            }
                        )
                        Text(text = "Supervisor")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = selectedUser,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Select $selectedRole") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                            },
                            modifier = Modifier.menuAnchor(),
                            shape = RoundedCornerShape(8.dp)
                        )

                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            userList.forEach { user ->
                                DropdownMenuItem(
                                    text = { Text(user.name) },
                                    onClick = {
                                        selectedUser = user.name
                                        assignedTo = user.id
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
            if (userRole == "MANAGER" || userRole == "ADMIN") {
                Spacer(modifier = Modifier.height(16.dp))

                // Due Date Validation
                val datePattern = Regex("\\d{2}-\\d{2}-\\d{4}")
                val isValidDueDate = dueDateText.matches(datePattern) && try {
                    LocalDate.parse(dueDateText, DateTimeFormatter.ofPattern("dd-MM-yyyy")).isAfter(LocalDate.now())
                } catch (e: Exception) {
                    false
                }

                // Due Date Field
                val datePickerState = rememberDatePickerState()
                OutlinedTextField(
                    value = dueDateText,
                    onValueChange = { /* Read-only, updated via date picker */ },
                    label = { Text("Due Date *") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker = true },
                    enabled = false,
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Calendar",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.primary,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurface
                    ),
                    shape = RoundedCornerShape(8.dp),
                    placeholder = { Text("dd-MM-yyyy") }
                )

                if (!isValidDueDate && dueDateText.isNotBlank()) {
                    Text(
                        text = "Please select a valid future date (dd-MM-yyyy)",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }

                if (showDatePicker) {
                    DatePickerDialog(
                        onDismissRequest = { showDatePicker = false },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    val selectedDateMillis = datePickerState.selectedDateMillis
                                    if (selectedDateMillis != null) {
                                        val selectedDate = Instant.ofEpochMilli(selectedDateMillis)
                                            .atZone(ZoneId.systemDefault())
                                            .toLocalDate()
                                        dueDateText = selectedDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))

                                        Log.d("due date ",dueDateText)
                                    }
                                    showDatePicker = false
                                }
                            ) { Text("OK") }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
                        }
                    ) {
                        DatePicker(state = datePickerState)
                    }
                }

                LaunchedEffect(dueDateText) {
                    savedStateHandle?.set("dueDate", dueDateText)
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Upload Section
            Text(
                text ="Upload",

                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Column {
                Button(
                    onClick = {
                        navController.navigate(Screen.ImageCapture.createRoute("YIELD")) {
                            launchSingleTop = true
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Select Media")
                }

                if (imageUris.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        imageUris.forEach { uri ->
                            Box(
                                modifier = Modifier.size(80.dp)
                            ) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clickable {
                                            // Optional: Add click handling for image preview
                                        },
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Image(
                                        painter = rememberAsyncImagePainter(uri),
                                        contentDescription = "Selected image",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        imageUris = imageUris.filter { it != uri }
                                        savedStateHandle?.set("selectedImages", imageUris.map { it.toString() })
                                    },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .size(24.dp)
                                        .offset(x = 4.dp, y = (-4).dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove image",
                                        tint = Color.White,
                                        modifier = Modifier.background(
                                            color = Color.Black.copy(alpha = 0.6f),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Notes/Description
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Notes/Description") },
                placeholder = { Text("Enter details about the harvest, quality, and any observations") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Loading indicator
            if (createYieldState is YieldListViewModel.CreateYieldState.Loading ) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AgroPrimary)
                }
                Spacer(modifier = Modifier.height(16.dp))
            }



            // Submit Button
            Button(
                onClick = {
                    if (cropName.isNotBlank() && row.isNotBlank() && yieldQuantity.isNotBlank() && valveName.isNotBlank() &&
                        yieldUnit.isNotBlank() && isValidDueDate
                        && (userRole != "ADMIN" || assignedTo != null) &&
                        (userRole != "MANAGER" || assignedTo != null)) {
                        scope.launch(ioDispatcher) {
                            val yieldDetails = YieldDetails(
                                harvestDate = harvestDate,
                                cropName = cropName,
                                row = row.toString(),
                                valveName = valveName,
                                fieldArea = fieldArea.ifBlank { null },
                                yieldQuantity = yieldQuantity,
                                yieldUnit = yieldUnit,
                                qualityGrade = qualityGrade.ifBlank { null },
                                moistureContent = moistureContent.ifBlank { null },
                                harvestMethod = harvestMethod.ifBlank { null },
                                notes = notes.ifBlank { null },
                                dueDate = if (userRole == "MANAGER" || userRole == "ADMIN") dueDateText else null,
                                latitude = location?.latitude,
                                longitude = location?.longitude
                            )
                            submittedEntry = yieldDetails

                            viewModel.createYieldTask(
                                yieldDetails = yieldDetails,
                                description = description,
                                imagesJson = emptyList<String>(), // Pass empty list instead of null
                                assignedToId = if (userRole == "MANAGER" || userRole == "ADMIN") assignedTo else null
                            )


                        }
                    }
                },
                enabled = cropName.isNotBlank() && row.isNotBlank() && yieldQuantity.isNotBlank() && valveName.isNotBlank() &&
                        yieldUnit.isNotBlank() && (userRole != "ADMIN" || assignedTo != null) &&
                        (userRole != "MANAGER" || assignedTo != null) && isValidDueDate &&
                        createYieldState !is YieldListViewModel.CreateYieldState.Loading,

                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AgroPrimary)
            ) {
                Text("Submit")
            }

            // Error message for task creation
            if (createYieldState is YieldListViewModel.CreateYieldState.Error) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = (createYieldState as YieldListViewModel.CreateYieldState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

