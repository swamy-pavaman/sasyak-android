package com.kapilagro.sasyak.presentation.scouting

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
import com.kapilagro.sasyak.domain.models.ScoutingDetails
import com.kapilagro.sasyak.presentation.common.catalog.CategoryViewModel
import com.kapilagro.sasyak.presentation.common.catalog.CategoriesState
import com.kapilagro.sasyak.presentation.common.components.SuccessDialog
import com.kapilagro.sasyak.presentation.common.navigation.Screen
import com.kapilagro.sasyak.presentation.common.theme.AgroPrimary
import com.kapilagro.sasyak.presentation.home.HomeViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.collections.filter

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScoutingRequestScreen(
    onTaskCreated: () -> Unit,
    onBackClick: () -> Unit,
    navController: NavController,
    viewModel: ScoutingListViewModel = hiltViewModel(),
    homeViewModel: HomeViewModel = hiltViewModel(),
    categoryViewModel: CategoryViewModel = hiltViewModel(),
    @IoDispatcher ioDispatcher: CoroutineDispatcher,
    imageUploadService: ImageUploadService
) {
    val createScoutingState by viewModel.createScoutingState.collectAsState()
    val userRole by homeViewModel.userRole.collectAsState()
    val supervisorsListState by homeViewModel.supervisorsListState.collectAsState()
    val categoriesStates by categoryViewModel.categoriesStates.collectAsState()
    val scope = rememberCoroutineScope()

    // Dialog state
    var showSuccessDialog by remember { mutableStateOf(false) }
    var submittedEntry by remember { mutableStateOf<ScoutingDetails?>(null) }
    var uploadState by remember { mutableStateOf<UploadState>(UploadState.Idle) }

    // Form fields with saved state
    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
    var scoutingDate by remember { mutableStateOf(
        savedStateHandle?.get<String>("scoutingDate")
            ?: LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
    ) }
    var cropName by remember { mutableStateOf(savedStateHandle?.get<String>("cropName") ?: "") }
    var valveName by remember { mutableStateOf(savedStateHandle?.get<String>("valveName") ?: "") }
    var cropNameExpanded by remember { mutableStateOf(false) }
    var valveNameExpanded by remember { mutableStateOf(false) }
    var row by remember { mutableStateOf(savedStateHandle?.get<String>("row") ?: "") }
    var rowExpanded by remember { mutableStateOf(false) }
    var treeNo by remember { mutableStateOf(savedStateHandle?.get<String>("treeNo") ?: "") }
    var treeNoExpanded by remember { mutableStateOf(false) }
    var noOfFruitSeen by remember { mutableStateOf(savedStateHandle?.get<String>("noOfFruitSeen") ?: "") }
    var noOfFlowersSeen by remember { mutableStateOf(savedStateHandle?.get<String>("noOfFlowersSeen") ?: "") }
    var noOfFruitsDropped by remember { mutableStateOf(savedStateHandle?.get<String>("noOfFruitsDropped") ?: "") }
    var nameOfDisease by remember { mutableStateOf(savedStateHandle?.get<String>("nameOfDisease") ?: "") }
    var nameOfDiseaseExpanded by remember { mutableStateOf(false) }
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
                "scoutingDate" to scoutingDate,
                "cropName" to cropName,
                "row" to row,
                "treeNo" to treeNo,
                "noOfFruitSeen" to noOfFruitSeen,
                "noOfFlowersSeen" to noOfFlowersSeen,
                "noOfFruitsDropped" to noOfFruitsDropped,
                "nameOfDisease" to nameOfDisease,
                "description" to description,
                "assignedTo" to assignedTo,
                "valveName" to valveName,
                "dueDate" to dueDateText
            )
        }.collect { state ->
            state.forEach { (key, value) ->
                savedStateHandle?.set(key, value)
            }
        }
    }

    // Fetch crops and diseases
    LaunchedEffect(Unit) {
        categoryViewModel.fetchCategories("Crop")
        categoryViewModel.fetchCategories("Disease")
        categoryViewModel.fetchCategories("Valve")
    }

    // Extract crops and diseases
    val crops = when (val state = categoriesStates["Crop"]) {
        is CategoriesState.Success -> state.categories.map { it.value }
        else -> listOf(
            "Wheat", "Rice", "Maize", "Barley", "Sorghum",
            "Mango", "Banana", "Apple", "Papaya", "Guava",
            "Tomato", "Potato", "Onion", "Brinjal", "Cabbage",
            "Sugarcane", "Groundnut", "Cotton", "Soybean", "Mustard"
        )
    }
    val diseases = when (val state = categoriesStates["Disease"]) {
        is CategoriesState.Success -> state.categories.map { it.value }
        else -> listOf(
            "Powdery mildew", "Downy mildew", "Late blight", "Early blight", "Rust", "Fusarium wilt",
            "Verticillium wilt", "Anthracnose", "Leaf spot", "Damping-off", "Fire blight",
            "Bacterial blight", "Mosaic virus", "Black spot", "Citrus canker"
        )
    }
    val valves = when (val state = categoriesStates["Valve"]) {
        is CategoriesState.Success -> state.categories.map { it.value }
        else -> listOf(
            "Valve 1", "Valve 2", "Valve 3"
        )
    }

    // Log states for debugging
    LaunchedEffect(categoriesStates) {
        Log.d("Categories", "Crops: $crops, Diseases: $diseases")
    }

    val rows = (1..20).map { it.toString() }
    val trees = (1..50).map { it.toString() }

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
    LaunchedEffect(createScoutingState) {
        when (createScoutingState) {
            is ScoutingListViewModel.CreateScoutingState.Success -> {
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
            "Date" to submittedEntry!!.scoutingDate,
            "Crop Name" to submittedEntry!!.cropName,
            "Row" to submittedEntry!!.row.toString(),
            "Tree No" to submittedEntry!!.treeNo.toString(),
            "Fruits Seen" to (submittedEntry!!.noOfFruitSeen ?: "Not specified"),
            "Flowers Seen" to (submittedEntry!!.noOfFlowersSeen ?: "Not specified"),
            "Fruits Dropped" to (submittedEntry!!.noOfFruitsDropped ?: "Not specified"),
            "Disease" to (submittedEntry!!.nameOfDisease ?: "None detected")
        )

        SuccessDialog(
            title = "Scouting Report Sent!",
            message = if (userRole=="MANAGER") "This report has been sent to the supervisor." else "This report has been sent to the manager.",
            details = details,
            description = description,
            primaryButtonText = "OK",
            onPrimaryButtonClick = {
                showSuccessDialog = false
                viewModel.clearCreateScoutingState()
                onTaskCreated()
                savedStateHandle?.remove<String>("scoutingDate")
                savedStateHandle?.remove<String>("cropName")
                savedStateHandle?.remove<String>("row")
                savedStateHandle?.remove<String>("treeNo")
                savedStateHandle?.remove<String>("noOfFruitSeen")
                savedStateHandle?.remove<String>("noOfFlowersSeen")
                savedStateHandle?.remove<String>("noOfFruitsDropped")
                savedStateHandle?.remove<String>("nameOfDisease")
                savedStateHandle?.remove<String>("description")
                savedStateHandle?.remove<Int>("assignedTo")
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scouting") },
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
            // Scouting Date (Display only)
            OutlinedTextField(
                value = "Scouting date: $scoutingDate",
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
            OutlinedTextField(
                value = row,
                onValueChange = { newValue ->
                    row = newValue
                },
                label = { Text("Row *") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Tree No Dropdown
            OutlinedTextField(
                value = treeNo,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                onValueChange = { newValue ->
                    treeNo = newValue
                },
                label = { Text("Tree no *") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            )


            Spacer(modifier = Modifier.height(16.dp))

            // Number of Fruits Seen
            OutlinedTextField(
                value = noOfFruitSeen,
                onValueChange = { noOfFruitSeen = it },
                label = { Text("No of fruit seen") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Number of Flowers Seen
            OutlinedTextField(
                value = noOfFlowersSeen,
                onValueChange = { noOfFlowersSeen = it },
                label = { Text("No of Flowers Seen") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Number of Fruits Dropped
            OutlinedTextField(
                value = noOfFruitsDropped,
                onValueChange = { noOfFruitsDropped = it },
                label = { Text("No of Fruits Dropped") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Name of the Disease Dropdown
            ExposedDropdownMenuBox(
                expanded = nameOfDiseaseExpanded,
                onExpandedChange = { nameOfDiseaseExpanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = nameOfDisease,
                    readOnly = false,
                    onValueChange = { newValue ->
                        nameOfDisease = newValue
                        nameOfDiseaseExpanded = true
                    },
                    label = { Text("Name of the Disease") },
                    trailingIcon = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ){
                            if (nameOfDisease.isNotEmpty()) {
                                IconButton(onClick = { nameOfDisease = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear name of the disease",
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = nameOfDiseaseExpanded)
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    shape = RoundedCornerShape(8.dp)
                )

                ExposedDropdownMenu(
                    expanded = nameOfDiseaseExpanded,
                    onDismissRequest = { nameOfDiseaseExpanded = false }
                ) {
                    diseases
                        .filter { it.contains(nameOfDisease, ignoreCase = true) }
                        .forEach { disease ->
                            DropdownMenuItem(
                                text = { Text(disease) },
                                onClick = {
                                    nameOfDisease = disease
                                    nameOfDiseaseExpanded = false
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
                                    assignedToExpanded = false
                                }
                            )
                        }
                    }
                }


                // TODO ADD Due Date only to manager not for supervisor


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
                        navController.navigate(Screen.ImageCapture.createRoute("SCOUTING")) {
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
                placeholder = { Text("Enter the description of the plant or disease") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Loading indicator
            if (createScoutingState is ScoutingListViewModel.CreateScoutingState.Loading || uploadState is UploadState.Loading) {
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
                    if (cropName.isNotBlank() && row.isNotBlank() && valveName.isNotBlank() && treeNo.isNotBlank() &&
                        (userRole != "MANAGER" || assignedTo != null)){
                        scope.launch(ioDispatcher) {
                            val scoutingDetails = ScoutingDetails(
                                scoutingDate = scoutingDate,
                                cropName = cropName,
                                row = row.toString(),
                                treeNo = treeNo.toString(),
                                noOfFruitSeen = noOfFruitSeen.ifBlank { null },
                                noOfFlowersSeen = noOfFlowersSeen.ifBlank { null },
                                noOfFruitsDropped = noOfFruitsDropped.ifBlank { null },
                                nameOfDisease = nameOfDisease.ifBlank { null },
                                valveName = valveName,
                                dueDate = if (userRole == "MANAGER") dueDateText else null
                            )
                            submittedEntry = scoutingDetails

                            if (imageFiles.isNullOrEmpty()) {
                                // No images to upload, proceed with task creation
                                viewModel.createScoutingTask(
                                    scoutingDetails = scoutingDetails,
                                    description = description,
                                    imagesJson = emptyList<String>(), // Pass null for imagesJson
                                    assignedToId = if (userRole == "MANAGER") assignedTo else null
                                )
                                uploadState = UploadState.Idle
                            } else {
                                // Upload images
                                uploadState = UploadState.Loading
                                val uploadResult = imageUploadService.uploadImages(imageFiles!!, "SCOUTING")
                                when (uploadResult) {
                                    is ApiResponse.Success -> {
                                        val imageUrls = uploadResult.data
                                        if (imageUrls.isEmpty()) {
                                            uploadState = UploadState.Error("Image upload failed, no URLs received")
                                            return@launch
                                        }
                                        viewModel.createScoutingTask(
                                            scoutingDetails = scoutingDetails,
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
                enabled = cropName.isNotBlank() && row.isNotBlank() && treeNo.isNotBlank() &&
                        (userRole != "MANAGER" || assignedTo != null) && valveName.isNotBlank() &&
                        createScoutingState !is ScoutingListViewModel.CreateScoutingState.Loading &&
                        uploadState !is UploadState.Loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AgroPrimary)
            ) {
                Text("Submit")
            }

            // Error message for task creation
            if (createScoutingState is ScoutingListViewModel.CreateScoutingState.Error) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = (createScoutingState as ScoutingListViewModel.CreateScoutingState.Error).message,
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