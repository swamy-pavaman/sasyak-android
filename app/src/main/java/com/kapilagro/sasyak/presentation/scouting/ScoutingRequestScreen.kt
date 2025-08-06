package com.kapilagro.sasyak.presentation.scouting

import android.content.Context
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
import com.kapilagro.sasyak.domain.models.ScoutingDetails
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
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

// Data classes for JSON parsing
@Serializable
data class CropDetails(
    val rawData: Map<String, JsonElement> = emptyMap()
) {
    val rows: Map<String, List<String>> by lazy {
        rawData.filterKeys { it != "PESTS" && it != "DISEASES" }
            .mapValues { (_, value) ->
                if (value is JsonArray) {
                    value.map { it.jsonPrimitive.content }
                } else {
                    emptyList()
                }
            }
    }

    val PESTS: List<String>? by lazy {
        rawData["PESTS"]?.jsonArray?.map { it.jsonPrimitive.content }
    }

    val DISEASES: List<String>? by lazy {
        rawData["DISEASES"]?.jsonArray?.map { it.jsonPrimitive.content }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScoutingRequestScreen(
    onTaskCreated: () -> Unit,
    onBackClick: () -> Unit,
    navController: NavController,
    viewModel: ScoutingListViewModel = hiltViewModel(),
    homeViewModel: HomeViewModel = hiltViewModel(),
    taskViewModel: TaskViewModel = hiltViewModel(),
    categoryViewModel: CategoryViewModel = hiltViewModel(),
    @IoDispatcher ioDispatcher: CoroutineDispatcher
) {
    val context = LocalContext.current
    val createScoutingState by viewModel.createScoutingState.collectAsState()
    val userRole by homeViewModel.userRole.collectAsState()
    val supervisorsListState by homeViewModel.supervisorsListState.collectAsState()
    val categoriesStates by categoryViewModel.categoriesStates.collectAsState()
    val scope = rememberCoroutineScope()
    val managersList by taskViewModel.managersList.collectAsState()
    val supervisorsList by taskViewModel.supervisorsList.collectAsState()

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
    var targetPest by remember { mutableStateOf(savedStateHandle?.get<String>("targetPest") ?: "") }
    var nameOfDiseaseExpanded by remember { mutableStateOf(false) }
    var description by remember { mutableStateOf(savedStateHandle?.get<String>("description") ?: "") }
    var assignedTo by remember { mutableStateOf<Int?>(savedStateHandle?.get<Int>("assignedTo")) }
    var assignedToExpanded by remember { mutableStateOf(false) }
    var imageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    // State for selected role and user
    var selectedRole by remember { mutableStateOf(savedStateHandle?.get<String>("selectedRole") ?:"Manager") }
    var selectedUser by remember { mutableStateOf(savedStateHandle?.get<String>("selectedUser") ?:"") }

    var category by remember { mutableStateOf(savedStateHandle?.get<String>("category") ?: "Pest") }

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
    val previewData by viewModel.previewData.collectAsState()
    val location by categoryViewModel.location.collectAsState()

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
                "targetPest" to targetPest,
                "description" to description,
                "assignedTo" to assignedTo,
                "valveName" to valveName,
                "dueDate" to dueDateText,
                "selectedRole" to selectedRole,
                "selectedUser" to selectedUser,
                "category" to category
            )
        }.collect { state ->
            state.forEach { (key, value) ->
                savedStateHandle?.set(key, value)
            }
        }
    }

    // Fetch valves
    LaunchedEffect(Unit) {
        if (categoriesStates["Valve"] !is CategoriesState.Success) {
            categoryViewModel.fetchCategories("Valve")
        }
    }

    // Extract valve details
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

    val diseaseList = listOf(
        "Malformation","Gummosis","Powdery Mildew","Canker"
    )

    val pestList = listOf(
        "Stem Borer","Nematodes","Thrips","Mealy Bugs","Scales","Hoppers","Caterpillars"
    )

    val targetItems = if (category == "Pest") pestList else diseaseList

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

     // Reset dependent fields
    LaunchedEffect(valveName) {
        if (valveName.isEmpty()) {
            cropName = ""
            row = ""
            treeNo = ""
        }
    }

    LaunchedEffect(cropName) {
        if (cropName.isEmpty()) {
            row = ""
            treeNo = ""
        }
    }

    LaunchedEffect(row) {
        if (row.isEmpty()) {
        treeNo = ""
            }
    }

    // Load supervisors list for MANAGER role
    LaunchedEffect(Unit) {
        if (userRole == "MANAGER"  && supervisorsListState !is HomeViewModel.SupervisorsListState.Success) {
            homeViewModel.loadSupervisorsList()
        }
    }

    // Listen for selected images from navigation
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
                imageUris = uriStrings.map { Uri.parse(it) }
            }
    }

    // Handle task creation success and start image upload
    LaunchedEffect(createScoutingState) {
        when (createScoutingState) {
            is ScoutingListViewModel.CreateScoutingState.Success -> {
                val createdTask = (createScoutingState as ScoutingListViewModel.CreateScoutingState.Success).task
                if (imageUris.isNotEmpty()) {

                    val imageFilePaths = imageUris.mapNotNull { uri ->
                        // Use the new static method from the ViewModel's companion object.
                        ImageCaptureViewModel.copyUriToCachedFile(context, uri)?.absolutePath
                    }
                    if (imageFilePaths.isNotEmpty()) {
                        // 1. Create the first work request for uploading files.

                        val constraints = Constraints.Builder()
                            .setRequiredNetworkType(NetworkType.CONNECTED)
                            .build()

                        val fileUploadRequest = OneTimeWorkRequestBuilder<FileUploadWorker>()

                            .setInputData(
                                FileUploadWorker.createInputData(
                                    taskId = createdTask.id,
                                    imagePaths = imageFilePaths,
                                    folder = "SCOUTING"
                                )
                            )
                            .setConstraints(constraints)
                            .addTag(FileUploadWorker.UPLOAD_TAG)
                            .build()

                        // 2. Create the second work request for attaching the URLs.
                        //    It doesn't need input data here because it gets it from the first worker.
                        val attachUrlRequest = OneTimeWorkRequestBuilder<AttachUrlWorker>()
                            .setConstraints(constraints)
                            .build()

                        // 3. Chain the requests and enqueue the sequence.
                        WorkManager.getInstance(context)
                            .beginWith(fileUploadRequest) // Start with uploading
                            .then(attachUrlRequest)       // Then, attach the URLs
                            .enqueue()

                    } else {
                        Log.e("RequestScreen", "No valid image files after URI conversion.")
                    }
                }

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
            "Disease" to (submittedEntry!!.targetPest ?: "None detected")
        )

        SuccessDialog(
            title = "Scouting Report Sent!",
            message = if (userRole=="SUPERVISOR") "This report has been sent to the manager." else "This report has been sent to the $selectedUser.",
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
                savedStateHandle?.remove<String>("valveName")
                savedStateHandle?.remove<String>("dueDate")
                savedStateHandle?.remove<List<String>>("selectedImages")
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scouting") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        if (previewData != null) {
                            cropName = previewData?.cropName ?: cropName
                            row = previewData?.row ?: row
                            valveName = previewData?.valueName ?: valveName
                            treeNo = previewData?.treeNo ?: treeNo
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

            // Tree No Dropdown
            ExposedDropdownMenuBox(
                expanded = treeNoExpanded,
                onExpandedChange = { treeNoExpanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = treeNo,
                    readOnly = false,
                    onValueChange = { newValue ->
                        treeNo = newValue
                        treeNoExpanded = true
                    },
                    label = { Text("Tree no *") },
                    trailingIcon = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (treeNo.isNotEmpty()) {
                                IconButton(onClick = { treeNo = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear tree number",
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = treeNoExpanded)
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    shape = RoundedCornerShape(8.dp)
                )

                ExposedDropdownMenu(
                    expanded = treeNoExpanded,
                    onDismissRequest = { treeNoExpanded = false }
                ) {
                    treeNumbers
                        .filter { it.contains(treeNo, ignoreCase = true) }
                        .forEach { tree ->
                            DropdownMenuItem(
                                text = { Text(tree) },
                                onClick = {
                                    treeNo = tree
                                    treeNoExpanded = false
                                }
                            )
                        }
                }
            }

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

            // Name of the Disease and Pest Dropdown
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
                        onValueChange = {},
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
                    Text("Select Media")
                }

                // Display selected images
                if (imageUris.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {


                        imageUris.forEach { uri ->

//                            val mimeType = LocalContext.current.contentResolver.getType(uri)
//                            Log.d("ScoutingPreview", "URI: $uri, MIME Type: $mimeType")

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
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        // Remove the selected image URI
                                        imageUris = imageUris.filter { it != uri }
                                        // Update saved state
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
                                        modifier = Modifier
                                            .background(
                                                color = Color.Black.copy(alpha = 0.6f),
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .padding(2.dp)
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
                    if (cropName.isNotBlank() && row.isNotBlank()
                        && valveName.isNotBlank() && treeNo.isNotBlank()
                        && (userRole != "ADMIN" || assignedTo != null) && isValidDueDate &&
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
                                targetPest = if (targetPest.isNotBlank()) "$category : $targetPest" else null,
                                valveName = valveName,
                                dueDate = if (userRole == "MANAGER" || userRole == "ADMIN") dueDateText else null,
                                latitude = location?.latitude,
                                longitude = location?.longitude
                            )
                            submittedEntry = scoutingDetails

                            // First create task, then WorkManager will handle image upload
                            viewModel.createScoutingTask(
                                scoutingDetails = scoutingDetails,
                                description = description,
                                assignedToId = if (userRole == "MANAGER" || userRole == "ADMIN") assignedTo else null
                            )
                        }
                    }
                },
                enabled = cropName.isNotBlank() && row.isNotBlank() && treeNo.isNotBlank() && isValidDueDate &&
                        (userRole != "MANAGER" || assignedTo != null) && valveName.isNotBlank() && (userRole != "ADMIN" || assignedTo != null) &&
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

//TODO put this in utilty
private fun copyUriToFile(context: Context, uri: Uri): File? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val file = File(context.cacheDir, "media_${System.currentTimeMillis()}")
        file.outputStream().use { outputStream ->
            inputStream.copyTo(outputStream)
        }
        file
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}


private sealed class UploadState {
    object Idle : UploadState()
    object Loading : UploadState()
    data class Error(val message: String) : UploadState()
}