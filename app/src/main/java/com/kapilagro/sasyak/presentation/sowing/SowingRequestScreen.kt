package com.kapilagro.sasyak.presentation.sowing

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
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
import com.kapilagro.sasyak.domain.models.SowingDetails
import com.kapilagro.sasyak.presentation.common.components.SuccessDialog
import com.kapilagro.sasyak.presentation.common.navigation.Screen
import com.kapilagro.sasyak.presentation.common.theme.AgroPrimary
import com.kapilagro.sasyak.presentation.home.HomeViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import com.kapilagro.sasyak.presentation.common.catalog.CategoryViewModel
import com.kapilagro.sasyak.presentation.common.catalog.CategoriesState
import java.time.Instant
import java.time.ZoneId

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SowingRequestScreen(
    onTaskCreated: () -> Unit,
    onBackClick: () -> Unit,
    navController: NavController,
    viewModel: SowingListViewModel = hiltViewModel(),
    homeViewModel: HomeViewModel = hiltViewModel(),
    categoryViewModel: CategoryViewModel = hiltViewModel(),
    @IoDispatcher ioDispatcher: CoroutineDispatcher,
    imageUploadService: ImageUploadService
) {
    val createSowingState by viewModel.createSowingState.collectAsState()
    val userRole by homeViewModel.userRole.collectAsState()
    val supervisorsListState by homeViewModel.supervisorsListState.collectAsState()
    val scope = rememberCoroutineScope()
    val categoriesStates by categoryViewModel.categoriesStates.collectAsState()

    // Dialog state
    var showSuccessDialog by remember { mutableStateOf(false) }
    var submittedEntry by remember { mutableStateOf<SowingDetails?>(null) }
    var uploadState by remember { mutableStateOf<UploadState>(UploadState.Idle) }

    // Form fields with saved state
    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
    var sowingDate by remember { mutableStateOf(
        savedStateHandle?.get<String>("sowingDate")
            ?: LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
    ) }
    var cropName by remember { mutableStateOf(savedStateHandle?.get<String>("cropName") ?: "") }
    var cropNameExpanded by remember { mutableStateOf(false) }
    var row by remember { mutableStateOf(savedStateHandle?.get<String>("row") ?: "") }
    var rowExpanded by remember { mutableStateOf(false) }
    var fieldArea by remember { mutableStateOf(savedStateHandle?.get<String>("fieldArea") ?: "") }
    var seedVariety by remember { mutableStateOf(savedStateHandle?.get<String>("seedVariety") ?: "") }
    var seedVarietyExpanded by remember { mutableStateOf(false) }
    var seedQuantity by remember { mutableStateOf(savedStateHandle?.get<String>("seedQuantity") ?: "") }
    var seedUnit by remember { mutableStateOf(savedStateHandle?.get<String>("seedUnit") ?: "kg") }
    var seedUnitExpanded by remember { mutableStateOf(false) }
    var sowingMethod by remember { mutableStateOf(savedStateHandle?.get<String>("sowingMethod") ?: "") }
    var sowingMethodExpanded by remember { mutableStateOf(false) }
    var seedTreatment by remember { mutableStateOf(savedStateHandle?.get<String>("seedTreatment") ?: "") }
    var spacingBetweenRows by remember { mutableStateOf(savedStateHandle?.get<String>("spacingBetweenRows") ?: "") }
    var spacingBetweenPlants by remember { mutableStateOf(savedStateHandle?.get<String>("spacingBetweenPlants") ?: "") }
    var soilCondition by remember { mutableStateOf(savedStateHandle?.get<String>("soilCondition") ?: "") }
    var weatherCondition by remember { mutableStateOf(savedStateHandle?.get<String>("weatherCondition") ?: "") }
    var description by remember { mutableStateOf(savedStateHandle?.get<String>("description") ?: "") }
    var imageFiles by remember { mutableStateOf<List<File>?>(null) }
    var assignedTo by remember { mutableStateOf<Int?>(savedStateHandle?.get<Int>("assignedTo")) }
    var assignedToExpanded by remember { mutableStateOf(false) }
    var dueDateText by remember {
        mutableStateOf(
            savedStateHandle?.get<String>("dueDate")
                ?: LocalDate.now().plusDays(7).format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
        )
    }
    var showDatePicker by remember { mutableStateOf(false) }


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
                "sowingDate" to sowingDate,
                "cropName" to cropName,
                "row" to row,
                "fieldArea" to fieldArea,
                "seedVariety" to seedVariety,
                "seedQuantity" to seedQuantity,
                "seedUnit" to seedUnit,
                "sowingMethod" to sowingMethod,
                "seedTreatment" to seedTreatment,
                "spacingBetweenRows" to spacingBetweenRows,
                "spacingBetweenPlants" to spacingBetweenPlants,
                "soilCondition" to soilCondition,
                "weatherCondition" to weatherCondition,
                "description" to description,
                "assignedTo" to assignedTo,
                "dueDate" to dueDateText
            )
        }.collect { state ->
            state.forEach { (key, value) ->
                savedStateHandle?.set(key, value)
            }
        }
    }

    LaunchedEffect(Unit) {
        categoryViewModel.fetchCategories("Crop")
        categoryViewModel.fetchCategories("Seed-variety")
    }

    val crops = when (val state = categoriesStates["Crop"]) {
        is CategoriesState.Success -> state.categories.map { it.value }
        else -> listOf(
            "Wheat", "Rice", "Maize", "Barley", "Sorghum",
            "Mango", "Banana", "Apple", "Papaya", "Guava",
            "Tomato", "Potato", "Onion", "Brinjal", "Cabbage",
            "Sugarcane", "Groundnut", "Cotton", "Soybean", "Mustard"
        )
    }

    val rows = (1..20).map { it.toString() }

    val seedVarieties = when (val state = categoriesStates["Seed-variety"]) {
        is CategoriesState.Success -> state.categories.map { it.value }
        else -> listOf(
            "HD-2967", "PBW-343", "WH-542", "HD-3086", "DBW-17",
            "Pusa Basmati-1", "IR-36", "IR-64", "MTU-7029", "Swarna",
            "DHM-117", "Pratap Hybrid-1", "Bio-9681", "Ganga-11", "PAC-751",
            "Bt Cotton", "H-6", "F-1378", "LRA-5166", "MCU-5"
        )
    }
    LaunchedEffect(categoriesStates) {
        Log.d("Categories", "Crops: $crops, SeedVarieties: $seedVarieties")
    }

    val units = listOf("kg", "g", "quintal", "tonnes", "bags")

    val sowingMethods = listOf(
        "Broadcasting", "Line Sowing", "Transplanting", "Dibbling",
        "Seed Drill", "Zero Tillage", "Raised Bed"
    )

    // Handle task creation success
    LaunchedEffect(createSowingState) {
        when (createSowingState) {
            is SowingListViewModel.CreateSowingState.Success -> {
                showSuccessDialog = true
            }
            else -> {
                // Handle other states if needed
            }
        }
    }

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

    // Success Dialog
    if (showSuccessDialog && submittedEntry != null) {
        val details = listOf(
            "Date" to submittedEntry!!.sowingDate,
            "Crop Name" to submittedEntry!!.cropName,
            "Row" to submittedEntry!!.row.toString(),
            "Field Area" to (submittedEntry!!.fieldArea?.plus(" acres") ?: "Not specified"),
            "Seed Variety" to submittedEntry!!.seedVariety,
            "Seed Quantity" to (submittedEntry!!.seedQuantity?.plus(" ${submittedEntry!!.seedUnit}") ?: "Not specified"),
            "Method" to submittedEntry!!.sowingMethod,
            "Seed Treatment" to (submittedEntry!!.seedTreatment ?: "Not specified"),
            "Spacing" to if (submittedEntry!!.spacingBetweenRows != null && submittedEntry!!.spacingBetweenPlants != null)
                "${submittedEntry!!.spacingBetweenRows} × ${submittedEntry!!.spacingBetweenPlants} cm"
            else "Not specified"
        )

        SuccessDialog(
            title = "Sowing Report Sent!",
            message = if (userRole=="MANAGER") "This report has been sent to the supervisor." else "This report has been sent to the manager.",
            details = details,
            description = description,
            primaryButtonText = "OK",
            onPrimaryButtonClick = {
                showSuccessDialog = false
                viewModel.clearCreateSowingState()
                onTaskCreated()
                // Clear saved state after successful submission
                savedStateHandle?.remove<String>("sowingDate")
                savedStateHandle?.remove<String>("cropName")
                savedStateHandle?.remove<String>("row")
                savedStateHandle?.remove<String>("fieldArea")
                savedStateHandle?.remove<String>("seedVariety")
                savedStateHandle?.remove<String>("seedQuantity")
                savedStateHandle?.remove<String>("seedUnit")
                savedStateHandle?.remove<String>("sowingMethod")
                savedStateHandle?.remove<String>("seedTreatment")
                savedStateHandle?.remove<String>("spacingBetweenRows")
                savedStateHandle?.remove<String>("spacingBetweenPlants")
                savedStateHandle?.remove<String>("soilCondition")
                savedStateHandle?.remove<String>("weatherCondition")
                savedStateHandle?.remove<String>("description")
                savedStateHandle?.remove<Int>("assignedTo")
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sowing") },
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
            // Sowing Date (Display only)
            OutlinedTextField(
                value = "Sowing date: $sowingDate",
                onValueChange = { },
                enabled = false,
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            )

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
                                    seedVariety = ""
                                }
                            )
                        }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Row Dropdown
            OutlinedTextField(
                value = row,
                onValueChange = {newValue ->
                    row = newValue},
                label = { Text("Row *") },
                modifier = Modifier
                    .fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            )

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

            // Seed Variety Dropdown
            ExposedDropdownMenuBox(
                expanded = seedVarietyExpanded,
                onExpandedChange = { seedVarietyExpanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = seedVariety,
                    readOnly = false,
                    onValueChange = { newValue ->
                        seedVariety = newValue
                        seedVarietyExpanded = true
                    },
                    label = { Text("Seed variety *") },
                    trailingIcon = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ){
                            if (seedVariety.isNotEmpty()) {
                                IconButton(onClick = { seedVariety = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear seed variety",
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = seedVarietyExpanded)
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    shape = RoundedCornerShape(8.dp)
                )

                ExposedDropdownMenu(
                    expanded = seedVarietyExpanded,
                    onDismissRequest = { seedVarietyExpanded = false }
                ) {
                    seedVarieties
                        .filter { it.contains(seedVariety, ignoreCase = true) }
                        .forEach { variety ->
                            DropdownMenuItem(
                                text = { Text(variety) },
                                onClick = {
                                    seedVariety = variety
                                    seedVarietyExpanded = false
                                }
                            )
                        }
                }
            }


            Spacer(modifier = Modifier.height(16.dp))

            // Seed Quantity and Unit
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = seedQuantity,
                    onValueChange = { seedQuantity = it },
                    label = { Text("Seed quantity") },
                    modifier = Modifier.weight(0.6f),
                    shape = RoundedCornerShape(8.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )

                ExposedDropdownMenuBox(
                    expanded = seedUnitExpanded,
                    onExpandedChange = { seedUnitExpanded = it },
                    modifier = Modifier.weight(0.4f)
                ) {
                    OutlinedTextField(
                        value = seedUnit,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Unit") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = seedUnitExpanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    ExposedDropdownMenu(
                        expanded = seedUnitExpanded,
                        onDismissRequest = { seedUnitExpanded = false }
                    ) {
                        units.forEach { unit ->
                            DropdownMenuItem(
                                text = { Text(unit) },
                                onClick = {
                                    seedUnit = unit
                                    seedUnitExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sowing Method Dropdown
            ExposedDropdownMenuBox(
                expanded = sowingMethodExpanded,
                onExpandedChange = { sowingMethodExpanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = sowingMethod,
                    readOnly = true,
                    onValueChange = { newValue ->
                        sowingMethod = newValue
                        sowingMethodExpanded = true
                    },
                    label = { Text("Sowing method *") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = sowingMethodExpanded)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    shape = RoundedCornerShape(8.dp)
                )

                ExposedDropdownMenu(
                    expanded = sowingMethodExpanded,
                    onDismissRequest = { sowingMethodExpanded = false }
                ) {
                    sowingMethods
                        .forEach { method ->
                            DropdownMenuItem(
                                text = { Text(method) },
                                onClick = {
                                    sowingMethod = method
                                    sowingMethodExpanded = false
                                }
                            )
                        }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Seed Treatment
            OutlinedTextField(
                value = seedTreatment,
                onValueChange = { seedTreatment = it },
                label = { Text("Seed treatment") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Spacing
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = spacingBetweenRows,
                    onValueChange = { spacingBetweenRows = it },
                    label = { Text("Row spacing (cm)") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )

                OutlinedTextField(
                    value = spacingBetweenPlants,
                    onValueChange = { spacingBetweenPlants = it },
                    label = { Text("Plant spacing (cm)") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Soil & Weather Conditions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = soilCondition,
                    onValueChange = { soilCondition = it },
                    label = { Text("Soil condition") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                )

                OutlinedTextField(
                    value = weatherCondition,
                    onValueChange = { weatherCondition = it },
                    label = { Text("Weather") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

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
            if (userRole == "MANAGER") {
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
                        navController.navigate(Screen.ImageCapture.createRoute("SOWING")) {
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

            Spacer(modifier = Modifier.height(8.dp))

            // Video Upload Card (Optional)
//            Card(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(100.dp)
//                    .clickable { /* TODO: Handle video upload */ },
//                shape = RoundedCornerShape(8.dp),
//                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
//            ) {
//                Box(
//                    modifier = Modifier.fillMaxSize(),
//                    contentAlignment = Alignment.Center
//                ) {
//                    Text("Upload Sowing Video (Optional)")
//                }
//            }
//
//            Spacer(modifier = Modifier.height(16.dp))

            // Description
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                placeholder = { Text("Enter any additional details about the sowing operation") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Loading indicator
            if (createSowingState is SowingListViewModel.CreateSowingState.Loading || uploadState is UploadState.Loading) {
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
                    if (cropName.isNotBlank() && row.isNotBlank() && seedVariety.isNotBlank() &&
                        sowingMethod.isNotBlank() &&
                        (userRole != "MANAGER" || assignedTo != null)) {
                        scope.launch(ioDispatcher) {
                            val sowingDetails = SowingDetails(
                                sowingDate = sowingDate,
                                cropName = cropName,
                                row = row.toString(),
                                fieldArea = fieldArea.ifBlank { null },
                                seedVariety = seedVariety,
                                seedQuantity = seedQuantity.ifBlank { null },
                                seedUnit = seedUnit.takeIf { seedQuantity.isNotBlank() },
                                sowingMethod = sowingMethod,
                                seedTreatment = seedTreatment.ifBlank { null },
                                spacingBetweenRows = spacingBetweenRows.ifBlank { null },
                                spacingBetweenPlants = spacingBetweenPlants.ifBlank { null },
                                soilCondition = soilCondition.ifBlank { null },
                                weatherCondition = weatherCondition.ifBlank { null },
                                dueDate = if (userRole == "MANAGER") dueDateText else null
                            )
                            submittedEntry = sowingDetails

                            if (imageFiles.isNullOrEmpty()) {
                                // No images to upload, proceed with task creation with empty image list
                                viewModel.createSowingTask(
                                    sowingDetails = sowingDetails,
                                    description = description,
                                    imagesJson = emptyList<String>(), // Pass empty list instead of null
                                    assignedToId = if (userRole == "MANAGER") assignedTo else null
                                )
                                uploadState = UploadState.Idle
                            } else {
                                // Upload images
                                uploadState = UploadState.Loading
                                val uploadResult = imageUploadService.uploadImages(imageFiles!!, "SOWING")
                                when (uploadResult) {
                                    is ApiResponse.Success -> {
                                        val imageUrls = uploadResult.data
                                        if (imageUrls.isEmpty()) {
                                            uploadState = UploadState.Error("Image upload failed, no URLs received")
                                            return@launch
                                        }
                                        viewModel.createSowingTask(
                                            sowingDetails = sowingDetails,
                                            description = description,
                                            imagesJson = imageUrls,
                                            assignedToId = if (userRole == "MANAGER") assignedTo else null
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
                enabled = cropName.isNotBlank() && row.isNotBlank() && seedVariety.isNotBlank() &&
                        sowingMethod.isNotBlank() &&
                        createSowingState !is SowingListViewModel.CreateSowingState.Loading &&
                        uploadState !is UploadState.Loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AgroPrimary)
            ) {
                Text("Submit")
            }

            // Error message for task creation
            if (createSowingState is SowingListViewModel.CreateSowingState.Error) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = (createSowingState as SowingListViewModel.CreateSowingState.Error).message,
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