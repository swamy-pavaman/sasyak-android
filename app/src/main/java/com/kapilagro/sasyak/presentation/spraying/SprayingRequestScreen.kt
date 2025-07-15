package com.kapilagro.sasyak.presentation.spraying

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.kapilagro.sasyak.data.api.ImageUploadService
import com.kapilagro.sasyak.di.IoDispatcher
import com.kapilagro.sasyak.domain.models.ApiResponse
import com.kapilagro.sasyak.domain.models.SprayingDetails
import com.kapilagro.sasyak.presentation.common.components.SuccessDialog
import com.kapilagro.sasyak.presentation.common.navigation.Screen
import com.kapilagro.sasyak.presentation.common.theme.AgroPrimary
import com.kapilagro.sasyak.presentation.home.HomeViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import com.kapilagro.sasyak.presentation.common.catalog.CategoryViewModel
import com.kapilagro.sasyak.presentation.common.catalog.CategoriesState
import com.kapilagro.sasyak.presentation.tasks.TaskViewModel
import java.time.Instant
import java.time.ZoneId

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SprayingRequestScreen(
    onTaskCreated: () -> Unit,
    onBackClick: () -> Unit,
    navController: NavController,
    viewModel: SprayingListViewModel = hiltViewModel(),
    homeViewModel: HomeViewModel = hiltViewModel(),
    taskViewModel: TaskViewModel = hiltViewModel(),
    categoryViewModel: CategoryViewModel = hiltViewModel(),
    @IoDispatcher ioDispatcher: CoroutineDispatcher,
    imageUploadService: ImageUploadService
) {
    val createSprayingState by viewModel.createSprayingState.collectAsState()
    val userRole by homeViewModel.userRole.collectAsState()
    val supervisorsListState by homeViewModel.supervisorsListState.collectAsState()
    val scope = rememberCoroutineScope()
    val categoriesStates by categoryViewModel.categoriesStates.collectAsState()
    val managersList by taskViewModel.managersList.collectAsState()
    val supervisorsList by taskViewModel.supervisorsList.collectAsState()

    // Dialog state
    var showSuccessDialog by remember { mutableStateOf(false) }
    var submittedEntry by remember { mutableStateOf<SprayingDetails?>(null) }
    var uploadState by remember { mutableStateOf<UploadState>(UploadState.Idle) }

    // Form fields with saved state
    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
    var sprayingDate by remember { mutableStateOf(
        savedStateHandle?.get<String>("sprayingDate")
            ?: LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
    ) }
    var cropName by remember { mutableStateOf(savedStateHandle?.get<String>("cropName") ?: "") }
    var cropNameExpanded by remember { mutableStateOf(false) }
    var row by remember { mutableStateOf(savedStateHandle?.get<String>("row") ?: "") }
    var rowExpanded by remember { mutableStateOf(false) }
    var fieldArea by remember { mutableStateOf(savedStateHandle?.get<String>("fieldArea") ?: "") }
    var chemicalName by remember { mutableStateOf(savedStateHandle?.get<String>("chemicalName") ?: "") }
    var chemicalNameExpanded by remember { mutableStateOf(false) }
    var dosage by remember { mutableStateOf(savedStateHandle?.get<String>("dosage") ?: "") }
    var dosageType by remember { mutableStateOf(savedStateHandle?.get<String>("dosageType") ?: "ml") }
    var sprayingMethod by remember { mutableStateOf(savedStateHandle?.get<String>("sprayingMethod") ?: "") }
    var sprayingMethodExpanded by remember { mutableStateOf(false) }
    var target by remember { mutableStateOf(savedStateHandle?.get<String>("target") ?: "") }
    var weatherCondition by remember { mutableStateOf(savedStateHandle?.get<String>("weatherCondition") ?: "") }
    var description by remember { mutableStateOf(savedStateHandle?.get<String>("description") ?: "") }
    var imageFiles by remember { mutableStateOf<List<File>?>(null) }
    var assignedTo by remember { mutableStateOf<Int?>(savedStateHandle?.get<Int>("assignedTo")) }
    var assignedToExpanded by remember { mutableStateOf(false) }
    var category by remember { mutableStateOf(savedStateHandle?.get<String>("category") ?: "Pest") }
    var targetPest by remember { mutableStateOf(savedStateHandle?.get<String>("targetPest") ?: "") }
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
                "sprayingDate" to sprayingDate,
                "cropName" to cropName,
                "row" to row,
                "fieldArea" to fieldArea,
                "chemicalName" to chemicalName,
                "dosage" to dosage,
                "dosageType" to dosageType,
                "sprayingMethod" to sprayingMethod,
                "target" to target,
                "targetPest" to targetPest,
                "category" to category,
                "weatherCondition" to weatherCondition,
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
        categoryViewModel.fetchCategories("Valve")
        categoryViewModel.fetchCategories("Fertilizer")
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

    val diseaseList by remember(valveName, cropName) {
        derivedStateOf {
            val diseaseList = valveDetails[valveName]?.get(cropName)?.DISEASES?.map { it.trim() } ?: emptyList()
            diseaseList
        }
    }

    val pestList by remember(valveName, cropName) {
        derivedStateOf {
            val pestList =
                valveDetails[valveName]?.get(cropName)?.PESTS?.map { it.trim() } ?: emptyList()
            pestList
        }
    }

    val rows by remember(valveName, cropName) {
        derivedStateOf {
            val rowList =
                valveDetails[valveName]?.get(cropName)?.rows?.keys?.toList() ?: emptyList()
            rowList
        }
    }

    val treeNumbers by remember(valveName, cropName, row) {
        derivedStateOf {
            val treeList = valveDetails[valveName]?.get(cropName)?.rows?.get(row) ?: emptyList()
            treeList
        }
    }

    val chemicals = when (val state = categoriesStates["Fertilizer"]) {
        is CategoriesState.Success -> state.categories.map { it.value }
        else -> listOf(
            "Glyphosate", "2,4-D", "Atrazine", "Paraquat", "Pendimethalin",
            "Chlorpyrifos", "Cypermethrin", "Lambda-cyhalothrin", "Carbofuran", "Imidacloprid",
            "Mancozeb", "Copper Oxychloride", "Carbendazim", "Metalaxyl", "Thiram"
        )
    }

    val targetItems = if (category == "Pest") pestList else diseaseList

    val sprayingMethods = listOf(
        "Backpack Sprayer", "Boom Sprayer", "Aerial Spraying", "Drip Application",
        "Fogger", "Mist Blower", "Hand Sprayer", "Tractor Mounted Sprayer"
    )

    // Load supervisors list for MANAGER role
    LaunchedEffect(Unit) {
        if (userRole == "MANAGER") {
            homeViewModel.loadSupervisorsList()
        }
    }

    // Handle navigation result from ImageCaptureScreen
    LaunchedEffect(navController) {
        navController.currentBackStackEntry?.savedStateHandle?.getStateFlow<List<File>>("selectedImages", emptyList())
            ?.collect { files ->
                imageFiles = files
            }
    }

    // Handle task creation success
    LaunchedEffect(createSprayingState) {
        when (createSprayingState) {
            is SprayingListViewModel.CreateSprayingState.Success -> {
                showSuccessDialog = true
            }
            else -> {
                // Handle other states if needed
            }
        }
    }

    // Success Dialog
    if (showSuccessDialog && submittedEntry != null) {
        val details = listOf(
            "Date" to submittedEntry!!.sprayingDate,
            "Crop Name" to submittedEntry!!.cropName,
            "Row" to submittedEntry!!.row.toString(),
            "Field Area" to (submittedEntry!!.fieldArea?.plus(" acres") ?: "Not specified"),
            "Chemical" to submittedEntry!!.chemicalName,
            "Dosage" to (submittedEntry!!.dosage ?: "Not specified"),
            "Method" to submittedEntry!!.sprayingMethod,
            "Target Pest" to (submittedEntry!!.target ?: "Not specified"),
            "Weather" to (submittedEntry!!.weatherCondition ?: "Not specified")
        )

        SuccessDialog(
            title = "Spraying Report Sent!",
            message = if (userRole=="MANAGER") "This report has been sent to the supervisor." else "This report has been sent to the manager.",
            details = details,
            description = description,
            primaryButtonText = "OK",
            onPrimaryButtonClick = {
                showSuccessDialog = false
                viewModel.clearCreateSprayingState()
                onTaskCreated()
                // Clear saved state after successful submission
                savedStateHandle?.remove<String>("sprayingDate")
                savedStateHandle?.remove<String>("cropName")
                savedStateHandle?.remove<String>("row")
                savedStateHandle?.remove<String>("fieldArea")
                savedStateHandle?.remove<String>("chemicalName")
                savedStateHandle?.remove<String>("dosage")
                savedStateHandle?.remove<String>("sprayingMethod")
                savedStateHandle?.remove<String>("targetPest")
                savedStateHandle?.remove<String>("weatherCondition")
                savedStateHandle?.remove<String>("description")
                savedStateHandle?.remove<Int>("assignedTo")
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Spraying") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
            // Spraying Date (Display only)
            OutlinedTextField(
                value = "Spraying date: $sprayingDate",
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

            // Chemical Name Dropdown
            ExposedDropdownMenuBox(
                expanded = chemicalNameExpanded,
                onExpandedChange = { chemicalNameExpanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = chemicalName,
                    readOnly = false,
                    onValueChange = { newValue ->
                        chemicalName = newValue
                        chemicalNameExpanded = true
                    },
                    label = { Text("Chemical name *") },
                    trailingIcon = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ){
                        if (chemicalName.isNotEmpty()) {
                            IconButton(onClick = { chemicalName = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear chemical name",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = chemicalNameExpanded)
                        Spacer(modifier = Modifier.width(8.dp))
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    shape = RoundedCornerShape(8.dp)
                )

                ExposedDropdownMenu(
                    expanded = chemicalNameExpanded,
                    onDismissRequest = { chemicalNameExpanded = false }
                ) {
                    chemicals
                        .filter { it.contains(chemicalName, ignoreCase = true) }
                        .forEach { chemical ->
                            DropdownMenuItem(
                                text = { Text(chemical) },
                                onClick = {
                                    chemicalName = chemical
                                    chemicalNameExpanded = false
                                }
                            )
                        }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Dosage
            Row (
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ){
                OutlinedTextField(
                    value = dosage,
                    onValueChange = { dosage = it },
                    label = { Text("Dosage") },
                    modifier = Modifier.fillMaxWidth()
                        .weight(0.6f),
                    shape = RoundedCornerShape(8.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                //Spacer(modifier = Modifier.width(8.dp))
                var dosageTypeExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = dosageTypeExpanded,
                    onExpandedChange = { dosageTypeExpanded = it },
                    modifier = Modifier
                        .weight(0.4f)
                        .fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = dosageType,
                        onValueChange = { /* readOnly, handled by dropdown */ },
                        readOnly = true,
                        label = { Text("Unit") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = dosageTypeExpanded)
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    ExposedDropdownMenu(
                        expanded = dosageTypeExpanded,
                        onDismissRequest = { dosageTypeExpanded = false }
                    ) {
                        listOf("ml", "L","grams").forEach { unit ->
                            DropdownMenuItem(
                                text = { Text(unit) },
                                onClick = {
                                    dosageType = unit
                                    dosageTypeExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Spraying Method Dropdown
            ExposedDropdownMenuBox(
                expanded = sprayingMethodExpanded,
                onExpandedChange = { sprayingMethodExpanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = sprayingMethod,
                    readOnly = false,
                    onValueChange = { newValue ->
                        sprayingMethod = newValue
                        sprayingMethodExpanded = true
                    },
                    label = { Text("Spraying method *") },
                    trailingIcon = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ){
                            if (sprayingMethod.isNotEmpty()) {
                                IconButton(onClick = { sprayingMethod = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear spraying method",
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = sprayingMethodExpanded)
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    shape = RoundedCornerShape(8.dp)
                )

                ExposedDropdownMenu(
                    expanded = sprayingMethodExpanded,
                    onDismissRequest = { sprayingMethodExpanded = false }
                ) {
                    sprayingMethods
                        .filter { it.contains(sprayingMethod, ignoreCase = true) }
                        .forEach { method ->
                            DropdownMenuItem(
                                text = { Text(method) },
                                onClick = {
                                    sprayingMethod = method
                                    sprayingMethodExpanded = false
                                }
                            )
                        }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Target Pest/disease
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // First dropdown
                var categoryExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = it },
                    modifier = Modifier
                        .weight(0.4f)
                        .fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded)
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        listOf("Pest", "Disease").forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item) },
                                onClick = {
                                    category = item
                                    targetPest = "" // Reset target when category changes
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }

                // Second dropdown
                var targetExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = targetExpanded,
                    onExpandedChange = { targetExpanded = it },
                    modifier = Modifier
                        .weight(0.6f)
                        .fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = targetPest,
                        onValueChange = { targetPest = it },
                        readOnly = false,
                        label = { Text("Target ${category.lowercase()}") },
                        trailingIcon = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ){
                                if (targetPest.isNotEmpty()) {
                                    IconButton(onClick = { targetPest = "" }) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Clear target pest",
                                            tint = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = targetExpanded)
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    ExposedDropdownMenu(
                        expanded = targetExpanded,
                        onDismissRequest = { targetExpanded = false }
                    ) {
                        targetItems
                            .filter { it.contains(targetPest, ignoreCase = true) }
                            .forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item) },
                                onClick = {
                                    targetPest = item
                                    targetExpanded = false
                                }
                            )
                        }
                    }
                }
            }
            //target = "$category : $targetPest"
            Spacer(modifier = Modifier.height(16.dp))

            // Weather Condition
            OutlinedTextField(
                value = weatherCondition,
                onValueChange = { weatherCondition = it },
                label = { Text("Weather condition") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            )

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
                text = "Upload",

                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Column {
                Button(
                    onClick = {
                        navController.navigate(Screen.ImageCapture.createRoute("SPRAYING")) {
                            launchSingleTop = true
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Select Images")
                }

                if (imageFiles != null && imageFiles!!.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        imageFiles!!.forEach { file ->
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
                                        painter = rememberAsyncImagePainter(file),
                                        contentDescription = "Selected image",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        imageFiles = imageFiles?.filter { it != file }
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

            // Description
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                placeholder = { Text("Enter description of the spraying activity") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Loading indicator
            if (createSprayingState is SprayingListViewModel.CreateSprayingState.Loading || uploadState is UploadState.Loading) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AgroPrimary)
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Error message for upload
            if (uploadState is UploadState.Error) {
                Text(
                    text = (uploadState as UploadState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Submit Button
            Button(
                onClick = {
                    if (cropName.isNotBlank() && row.isNotBlank() && chemicalName.isNotBlank() && valveName.isNotBlank() &&
                        sprayingMethod.isNotBlank() && isValidDueDate
                        && (userRole != "ADMIN" || assignedTo != null) &&
                        (userRole != "MANAGER" || assignedTo != null)) {
                        scope.launch(ioDispatcher) {
                            val sprayingDetails = SprayingDetails(
                                sprayingDate = sprayingDate,
                                cropName = cropName,
                                row = row.toString(),
                                fieldArea = fieldArea.ifBlank { null },
                                chemicalName = chemicalName,
                                dosage = if (dosage.isNotBlank()) "$dosage $dosageType" else null,
                                sprayingMethod = sprayingMethod,
                                target = if (targetPest.isNotBlank()) "$category : $targetPest" else null,
                                weatherCondition = weatherCondition.ifBlank { null },
                                valveName = valveName,
                                dueDate = if (userRole == "MANAGER" || userRole == "ADMIN") dueDateText else null
                            )
                            submittedEntry = sprayingDetails

                            if (imageFiles.isNullOrEmpty()) {
                                // No images to upload, proceed with task creation with empty image list
                                viewModel.createSprayingTask(
                                    sprayingDetails = sprayingDetails,
                                    description = description,
                                    imagesJson = emptyList<String>(), // Pass empty list instead of null
                                    assignedToId = if (userRole == "MANAGER" || userRole == "ADMIN") assignedTo else null
                                )
                                uploadState = UploadState.Idle
                            } else {
                                // Upload images
                                uploadState = UploadState.Loading
                                val uploadResult = imageUploadService.uploadImages(imageFiles!!, "SPRAYING")
                                when (uploadResult) {
                                    is ApiResponse.Success -> {
                                        val imageUrls = uploadResult.data
                                        if (imageUrls.isEmpty()) {
                                            uploadState = UploadState.Error("Image upload failed, no URLs received")
                                            return@launch
                                        }
                                        viewModel.createSprayingTask(
                                            sprayingDetails = sprayingDetails,
                                            description = description,
                                            imagesJson = imageUrls,
                                            assignedToId = if (userRole == "MANAGER" || userRole == "ADMIN") assignedTo else null
                                        )
                                        uploadState = UploadState.Idle
                                    }
                                    is ApiResponse.Error -> {
                                        uploadState = UploadState.Error("Image upload failed: ${uploadResult.errorMessage}")
                                    }
                                    is ApiResponse.Loading -> {
                                        uploadState = UploadState.Loading
                                    }
                                }
                            }
                        }
                    }
                },
                enabled = cropName.isNotBlank() && row.isNotBlank() && chemicalName.isNotBlank() && valveName.isNotBlank() &&
                        sprayingMethod.isNotBlank() && (userRole != "ADMIN" || assignedTo != null) &&
                        (userRole != "MANAGER" || assignedTo != null) && isValidDueDate &&
                        createSprayingState !is SprayingListViewModel.CreateSprayingState.Loading &&
                        uploadState !is UploadState.Loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AgroPrimary)
            ) {
                Text("Submit")
            }

            // Error message for task creation
            if (createSprayingState is SprayingListViewModel.CreateSprayingState.Error) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = (createSprayingState as SprayingListViewModel.CreateSprayingState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

private sealed class UploadState {
    object Idle : UploadState()
    object Loading : UploadState()
    data class Error(val message: String) : UploadState()
}