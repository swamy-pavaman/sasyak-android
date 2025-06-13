package com.kapilagro.sasyak.presentation.fuel

import android.os.Build
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
import com.kapilagro.sasyak.domain.models.FuelDetails
import com.kapilagro.sasyak.presentation.common.components.SuccessDialog
import com.kapilagro.sasyak.presentation.common.navigation.Screen
import com.kapilagro.sasyak.presentation.common.theme.AgroPrimary
import com.kapilagro.sasyak.presentation.common.theme.FuelColor
import com.kapilagro.sasyak.presentation.common.theme.FuelIcon
import com.kapilagro.sasyak.presentation.common.theme.ScoutingColor
import com.kapilagro.sasyak.presentation.home.HomeViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FuelRequestScreen(
    onTaskCreated: () -> Unit,
    onBackClick: () -> Unit,
    navController: NavController,
    viewModel: FuelListViewModel = hiltViewModel(),
    homeViewModel: HomeViewModel = hiltViewModel(),
    @IoDispatcher ioDispatcher: CoroutineDispatcher,
    imageUploadService: ImageUploadService
) {
    val createFuelState by viewModel.createFuelState.collectAsState()
    val userRole by homeViewModel.userRole.collectAsState()
    val supervisorsListState by homeViewModel.supervisorsListState.collectAsState()
    val scope = rememberCoroutineScope()

    // Dialog state
    var showSuccessDialog by remember { mutableStateOf(false) }
    var submittedEntry by remember { mutableStateOf<FuelDetails?>(null) }
    var uploadState by remember { mutableStateOf<UploadState>(UploadState.Idle) }

    // Form fields with saved state
    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
    var fuelDate by remember { mutableStateOf(
        savedStateHandle?.get<String>("fuelDate")
            ?: LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
    ) }
    var vehicleName by remember { mutableStateOf(savedStateHandle?.get<String>("vehicleName") ?: "") }
    var vehicleNameExpanded by remember { mutableStateOf(false) }
    var vehicleNumber by remember { mutableStateOf(savedStateHandle?.get<String>("vehicleNumber") ?: "") }
    var fuelType by remember { mutableStateOf(savedStateHandle?.get<String>("fuelType") ?: "") }
    var fuelTypeExpanded by remember { mutableStateOf(false) }
    var quantity by remember { mutableStateOf(savedStateHandle?.get<String>("quantity") ?: "") }
    var unit by remember { mutableStateOf(savedStateHandle?.get<String>("unit") ?: "liters") }
    var unitExpanded by remember { mutableStateOf(false) }
    var costPerUnit by remember { mutableStateOf(savedStateHandle?.get<String>("costPerUnit") ?: "") }
    var totalCost by remember { mutableStateOf(savedStateHandle?.get<String>("totalCost") ?: "") }
    var driverName by remember { mutableStateOf(savedStateHandle?.get<String>("driverName") ?: "") }
    var odometer by remember { mutableStateOf(savedStateHandle?.get<String>("odometer") ?: "") }
    var purposeOfFuel by remember { mutableStateOf(savedStateHandle?.get<String>("purposeOfFuel") ?: "") }
    var refillLocation by remember { mutableStateOf(savedStateHandle?.get<String>("refillLocation") ?: "") }
    var notes by remember { mutableStateOf(savedStateHandle?.get<String>("notes") ?: "") }
    var description by remember { mutableStateOf(savedStateHandle?.get<String>("description") ?: "") }
    var imageFiles by remember { mutableStateOf<List<File>?>(null) }
    var assignedTo by remember { mutableStateOf<Int?>(savedStateHandle?.get<Int>("assignedTo")) }
    var assignedToExpanded by remember { mutableStateOf(false) }

    // Save form state before navigating to ImageCaptureScreen
    LaunchedEffect(Unit) {
        snapshotFlow {
            mapOf(
                "fuelDate" to fuelDate,
                "vehicleName" to vehicleName,
                "vehicleNumber" to vehicleNumber,
                "fuelType" to fuelType,
                "quantity" to quantity,
                "unit" to unit,
                "costPerUnit" to costPerUnit,
                "totalCost" to totalCost,
                "driverName" to driverName,
                "odometer" to odometer,
                "purposeOfFuel" to purposeOfFuel,
                "refillLocation" to refillLocation,
                "notes" to notes,
                "description" to description,
                "assignedTo" to assignedTo
            )
        }.collect { state ->
            state.forEach { (key, value) ->
                savedStateHandle?.set(key, value)
            }
        }
    }

    val vehicles = listOf(
        "Tractor - John Deere",
        "Tractor - Mahindra",
        "Truck - Tata",
        "Harvester",
        "Water Pump",
        "Generator",
        "Sprayer",
        "Pickup Truck",
        "Farm Utility Vehicle"
    )

    val fuelTypes = listOf(
        "Diesel", "Petrol", "CNG", "LPG", "Bio-diesel", "Electric"
    )

    val units = listOf("liters", "gallons", "kWh")

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
    LaunchedEffect(createFuelState) {
        when (createFuelState) {
            is FuelListViewModel.CreateFuelState.Success -> {
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
            "Date" to submittedEntry!!.fuelDate,
            "Vehicle" to submittedEntry!!.vehicleName,
            "Vehicle Number" to (submittedEntry!!.vehicleNumber ?: "Not specified"),
            "Fuel Type" to submittedEntry!!.fuelType,
            "Quantity" to "${submittedEntry!!.quantity} ${submittedEntry!!.unit}",
            "Cost" to if (submittedEntry!!.totalCost != null) "₹${submittedEntry!!.totalCost}" else "Not specified",
            "Driver" to (submittedEntry!!.driverName ?: "Not specified"),
            "Location" to (submittedEntry!!.refillLocation ?: "Not specified")
        )

        SuccessDialog(
            title = "Fuel Entry Sent!",
            message = "Your manager will be notified when they take action on it.",
            details = details,
            description = description,
            primaryButtonText = "OK",
            onPrimaryButtonClick = {
                showSuccessDialog = false
                viewModel.clearCreateFuelState()
                onTaskCreated()
                // Clear saved state after successful submission
                savedStateHandle?.remove<String>("fuelDate")
                savedStateHandle?.remove<String>("vehicleName")
                savedStateHandle?.remove<String>("vehicleNumber")
                savedStateHandle?.remove<String>("fuelType")
                savedStateHandle?.remove<String>("quantity")
                savedStateHandle?.remove<String>("unit")
                savedStateHandle?.remove<String>("costPerUnit")
                savedStateHandle?.remove<String>("totalCost")
                savedStateHandle?.remove<String>("driverName")
                savedStateHandle?.remove<String>("odometer")
                savedStateHandle?.remove<String>("purposeOfFuel")
                savedStateHandle?.remove<String>("refillLocation")
                savedStateHandle?.remove<String>("notes")
                savedStateHandle?.remove<String>("description")
                savedStateHandle?.remove<Int>("assignedTo")
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Fuel Entry", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                }, colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = FuelIcon
                )
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
            // Fuel Date (Display only)
            OutlinedTextField(
                value = "Fuel date: $fuelDate",
                onValueChange = { },
                enabled = false,
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Vehicle Name Dropdown
            ExposedDropdownMenuBox(
                expanded = vehicleNameExpanded,
                onExpandedChange = { vehicleNameExpanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = vehicleName,
                    onValueChange = { newValue ->
                        vehicleName = newValue
                        vehicleNameExpanded = true
                    },
                    label = { Text("Vehicle name *") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = vehicleNameExpanded)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    shape = RoundedCornerShape(8.dp)
                )

                ExposedDropdownMenu(
                    expanded = vehicleNameExpanded,
                    onDismissRequest = { vehicleNameExpanded = false }
                ) {
                    vehicles.filter { it.contains(vehicleName, ignoreCase = true) }
                        .forEach { vehicle ->
                            DropdownMenuItem(
                                text = { Text(vehicle) },
                                onClick = {
                                    vehicleName = vehicle
                                    vehicleNameExpanded = false
                                }
                            )
                        }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Vehicle Number
            OutlinedTextField(
                value = vehicleNumber,
                onValueChange = { vehicleNumber = it },
                label = { Text("Vehicle number") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Fuel Type Dropdown
            ExposedDropdownMenuBox(
                expanded = fuelTypeExpanded,
                onExpandedChange = { fuelTypeExpanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = fuelType,
                    onValueChange = { newValue ->
                        fuelType = newValue
                        fuelTypeExpanded = true
                    },
                    label = { Text("Fuel type *") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = fuelTypeExpanded)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    shape = RoundedCornerShape(8.dp)
                )

                ExposedDropdownMenu(
                    expanded = fuelTypeExpanded,
                    onDismissRequest = { fuelTypeExpanded = false }
                ) {
                    fuelTypes.filter { it.contains(fuelType, ignoreCase = true) }
                        .forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type) },
                                onClick = {
                                    fuelType = type
                                    fuelTypeExpanded = false
                                }
                            )
                        }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Quantity and Unit
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Quantity *") },
                    modifier = Modifier.weight(0.6f),
                    shape = RoundedCornerShape(8.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )

                ExposedDropdownMenuBox(
                    expanded = unitExpanded,
                    onExpandedChange = { unitExpanded = it },
                    modifier = Modifier.weight(0.4f)
                ) {
                    OutlinedTextField(
                        value = unit,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Unit") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitExpanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    ExposedDropdownMenu(
                        expanded = unitExpanded,
                        onDismissRequest = { unitExpanded = false }
                    ) {
                        units.forEach { unitOption ->
                            DropdownMenuItem(
                                text = { Text(unitOption) },
                                onClick = {
                                    unit = unitOption
                                    unitExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Cost Fields
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = costPerUnit,
                    onValueChange = {
                        costPerUnit = it
                        // Calculate total cost if both fields have values
                        if (it.isNotBlank() && quantity.isNotBlank()) {
                            try {
                                val cost = it.toFloat()
                                val qty = quantity.toFloat()
                                totalCost = (cost * qty).toString()
                            } catch (e: Exception) {
                                // Handle parsing error
                            }
                        }
                    },
                    label = { Text("Cost per unit (₹)") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )

                OutlinedTextField(
                    value = totalCost,
                    onValueChange = { totalCost = it },
                    label = { Text("Total cost (₹)") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Driver Name
            OutlinedTextField(
                value = driverName,
                onValueChange = { driverName = it },
                label = { Text("Driver name") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Odometer and Purpose
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = odometer,
                    onValueChange = { odometer = it },
                    label = { Text("Odometer (km)") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                OutlinedTextField(
                    value = purposeOfFuel,
                    onValueChange = { purposeOfFuel = it },
                    label = { Text("Purpose") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Refill Location
            OutlinedTextField(
                value = refillLocation,
                onValueChange = { refillLocation = it },
                label = { Text("Refill location") },
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

            // Upload Section
            Text(
                text = "Upload *",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Column {
                Button(
                    onClick = {
                        navController.navigate(Screen.ImageCapture.createRoute("FUEL")) {
                            launchSingleTop = true
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = FuelIcon)
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

            // Notes/Description
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Notes") },
                placeholder = { Text("Enter any additional notes about this fuel entry") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Loading indicator
            if (createFuelState is FuelListViewModel.CreateFuelState.Loading || uploadState is UploadState.Loading) {
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
                    if (vehicleName.isNotBlank() && fuelType.isNotBlank() && quantity.isNotBlank() &&
                        imageFiles != null && (userRole != "MANAGER" || assignedTo != null)) {
                        scope.launch(ioDispatcher) {
                            // Upload images
                            uploadState = UploadState.Loading
                            val uploadResult = imageUploadService.uploadImages(imageFiles!!, "FUEL")
                            when (uploadResult) {
                                is ApiResponse.Success -> {
                                    val imageUrls = uploadResult.data
                                    if (imageUrls.isEmpty()) {
                                        uploadState = UploadState.Error("Image upload failed, no URLs received")
                                        return@launch
                                    }
                                    val fuelDetails = FuelDetails(
                                        fuelDate = fuelDate,
                                        vehicleName = vehicleName,
                                        vehicleNumber = vehicleNumber.ifBlank { null },
                                        fuelType = fuelType,
                                        quantity = quantity,
                                        unit = unit,
                                        costPerUnit = costPerUnit.ifBlank { null },
                                        totalCost = totalCost.ifBlank { null },
                                        driverName = driverName.ifBlank { null },
                                        odometer = odometer.ifBlank { null },
                                        purposeOfFuel = purposeOfFuel.ifBlank { null },
                                        refillLocation = refillLocation.ifBlank { null },
                                        notes = notes.ifBlank { null },
                                       // uploadedFiles = imageUrls
                                    )
                                    submittedEntry = fuelDetails
                                    viewModel.createFuelTask(
                                        fuelDetails = fuelDetails,
                                        description = description,
                                         imageUrls,
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
                },
                enabled = vehicleName.isNotBlank() && fuelType.isNotBlank() && quantity.isNotBlank() &&
                        imageFiles != null && (userRole != "MANAGER" || assignedTo != null) &&
                        createFuelState !is FuelListViewModel.CreateFuelState.Loading &&
                        uploadState !is UploadState.Loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = FuelIcon)
            ) {
                Text("Submit")
            }

            // Error message for task creation
            if (createFuelState is FuelListViewModel.CreateFuelState.Error) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = (createFuelState as FuelListViewModel.CreateFuelState.Error).message,
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