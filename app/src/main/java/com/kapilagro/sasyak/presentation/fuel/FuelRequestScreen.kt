package com.kapilagro.sasyak.presentation.fuel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kapilagro.sasyak.domain.models.FuelEntry
import com.kapilagro.sasyak.presentation.common.components.SuccessDialog
import com.kapilagro.sasyak.presentation.common.theme.AgroPrimary
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FuelRequestScreen(
    onBackClick: () -> Unit,
    viewModel: FuelViewModel = hiltViewModel()
) {
    var date by remember { mutableStateOf(LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))) }
    var openingStockLiters by remember { mutableStateOf("") }
    var imageUploaded by remember { mutableStateOf(false) }
    var vehicleType by remember { mutableStateOf("") }
    var vehicleTypeExpanded by remember { mutableStateOf(false) }
    var quantityNeeded by remember { mutableStateOf("") }
    var purpose by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var driverName by remember { mutableStateOf("") }
    var driverNameExpanded by remember { mutableStateOf(false) }
    // New fields
    var vehicleNumber by remember { mutableStateOf("") }
    var odometerReading by remember { mutableStateOf("") }
    var expectedDistance by remember { mutableStateOf("") }
    var fuelType by remember { mutableStateOf("") }
    var fuelTypeExpanded by remember { mutableStateOf(false) }

    var showSuccessDialog by remember { mutableStateOf(false) }
    var submittedEntry by remember { mutableStateOf<FuelEntry?>(null) }

    // Observe the ViewModel state
    val fuelRequestState by viewModel.createFuelRequestState.collectAsState()

    // Process state changes
    LaunchedEffect(fuelRequestState) {
        when (fuelRequestState) {
            is FuelViewModel.CreateFuelRequestState.Success -> {
                showSuccessDialog = true
            }
            is FuelViewModel.CreateFuelRequestState.Error -> {
                // Handle error state (could show a snackbar or error dialog)
            }
            else -> {
                // Handle other states if needed
            }
        }
    }

    // Mock data for dropdowns
    val vehicleTypes = listOf("Tractor", "Harvester", "Truck", "Bike", "Car", "Loader", "Tiller")
    val driverNames = listOf(
        "Rajesh Kumar", "Suresh Patel", "Amit Singh", "Vijay Sharma",
        "Rahul Verma", "Ajay Yadav", "Sanjay Gupta", "Mahesh Joshi",
        "Deepak Mishra", "Vinod Tiwari", "Ramesh Choudhary"
    )
    val fuelTypes = listOf("Diesel", "Petrol", "CNG", "Electric")

    // Success Dialog
    if (showSuccessDialog && submittedEntry != null) {
        val details = listOf(
            "Date" to submittedEntry!!.date,
            "Vehicle Number" to submittedEntry!!.vehicleNumber,
            "Vehicle Type" to submittedEntry!!.vehicleType,
            "Fuel Type" to submittedEntry!!.fuelType,
            "Odometer Reading" to "${submittedEntry!!.odometerReading} km",
            "Opening Stock" to "${submittedEntry!!.openingStock} liters",
            "Quantity Needed" to "${submittedEntry!!.quantityIssued} liters",
            "Expected Distance" to "${submittedEntry!!.expectedDistance} km",
            "Purpose" to submittedEntry!!.purpose,
            "Driver" to submittedEntry!!.driverName
        )

        SuccessDialog(
            title = "Request Sent!",
            message = "Your manager will notify when they take action on it.",
            details = details,
            description = submittedEntry!!.description,
            primaryButtonText = "OK",
            onPrimaryButtonClick = {
                showSuccessDialog = false
                viewModel.clearCreateFuelRequestState()
                // No auto navigation - only close the dialog
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Fuel Request") },
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
            // Date (Display only)
            OutlinedTextField(
                value = "Date: $date",
                onValueChange = {},
                enabled = false,
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Vehicle Type Dropdown
            ExposedDropdownMenuBox(
                expanded = vehicleTypeExpanded,
                onExpandedChange = { vehicleTypeExpanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = vehicleType,
                    onValueChange = { vehicleType = it },
                    label = { Text("Type of Vehicle") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = vehicleTypeExpanded)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    shape = RoundedCornerShape(8.dp)
                )
                ExposedDropdownMenu(
                    expanded = vehicleTypeExpanded,
                    onDismissRequest = { vehicleTypeExpanded = false }
                ) {
                    vehicleTypes.filter { it.contains(vehicleType, ignoreCase = true) }
                        .forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type) },
                                onClick = {
                                    vehicleType = type
                                    vehicleTypeExpanded = false
                                }
                            )
                        }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Vehicle Number (NEW)
            OutlinedTextField(
                value = vehicleNumber,
                onValueChange = { vehicleNumber = it },
                label = { Text("Vehicle Number") },
                placeholder = { Text("Eg: MH12AB1234") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Fuel Type Dropdown (NEW)
            ExposedDropdownMenuBox(
                expanded = fuelTypeExpanded,
                onExpandedChange = { fuelTypeExpanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = fuelType,
                    onValueChange = { fuelType = it },
                    label = { Text("Fuel Type") },
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

            // Odometer Reading (combined with image upload)
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Vehicle Odometer Reading",
                    style = MaterialTheme.typography.labelLarge
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Image upload section
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                        .clickable { imageUploaded = true },
                    contentAlignment = Alignment.Center
                ) {
                    if (imageUploaded) {
                        Surface(
                            color = Color(0xFFE0F7FA),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Image Uploaded",
                                    tint = Color(0xFF4CAF50),
                                    modifier = Modifier.size(40.dp)
                                )
                                Text("Odometer Reading Image Uploaded", color = Color(0xFF4CAF50))
                            }
                        }
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoCamera,
                                contentDescription = "Upload Reading Image",
                                tint = Color.Gray,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Upload Odometer Reading Image", color = Color.Gray)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Manual odometer input
                OutlinedTextField(
                    value = odometerReading,
                    onValueChange = { odometerReading = it },
                    label = { Text("Current Odometer Reading (km)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Opening Stock
            OutlinedTextField(
                value = openingStockLiters,
                onValueChange = { openingStockLiters = it },
                label = { Text("Opening Stock (liters)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Quantity Needed
            OutlinedTextField(
                value = quantityNeeded,
                onValueChange = { quantityNeeded = it },
                label = { Text("Quantity Needed (liters)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Expected Distance (NEW)
            OutlinedTextField(
                value = expectedDistance,
                onValueChange = { expectedDistance = it },
                label = { Text("Expected Distance (km)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Purpose
            OutlinedTextField(
                value = purpose,
                onValueChange = { purpose = it },
                label = { Text("Purpose") },
                placeholder = { Text("Eg: Harvesting, Transportation") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Description
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Driver Name Dropdown
            ExposedDropdownMenuBox(
                expanded = driverNameExpanded,
                onExpandedChange = { driverNameExpanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = driverName,
                    onValueChange = { driverName = it },
                    label = { Text("Driver Name") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = driverNameExpanded)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    shape = RoundedCornerShape(8.dp)
                )
                ExposedDropdownMenu(
                    expanded = driverNameExpanded,
                    onDismissRequest = { driverNameExpanded = false }
                ) {
                    driverNames.filter { it.contains(driverName, ignoreCase = true) }
                        .forEach { name ->
                            DropdownMenuItem(
                                text = { Text(name) },
                                onClick = {
                                    driverName = name
                                    driverNameExpanded = false
                                }
                            )
                        }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Loading indicator
            if (fuelRequestState is FuelViewModel.CreateFuelRequestState.Loading) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AgroPrimary)
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Submit Button
            Button(
                onClick = {
                    val entry = FuelEntry(
                        date = date,
                        openingStock = openingStockLiters,
                        vehicleType = vehicleType,
                        quantityIssued = quantityNeeded,
                        closingStock = "", // Not used in this version
                        purpose = purpose,
                        requestedBy = "", // Not used in this version
                        driverName = driverName,
                        description = description,
                        vehicleNumber = vehicleNumber,
                        odometerReading = odometerReading,
                        expectedDistance = expectedDistance,
                        fuelType = fuelType
                    )
                    submittedEntry = entry
                    viewModel.createFuelRequest(entry, "Fuel request for $vehicleType - $purpose")
                },
                enabled = openingStockLiters.isNotBlank() && vehicleType.isNotBlank() &&
                        quantityNeeded.isNotBlank() && purpose.isNotBlank() &&
                        driverName.isNotBlank() && imageUploaded &&
                        vehicleNumber.isNotBlank() && odometerReading.isNotBlank() &&
                        expectedDistance.isNotBlank() && fuelType.isNotBlank() &&
                        fuelRequestState != FuelViewModel.CreateFuelRequestState.Loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AgroPrimary)
            ) {
                Text("Submit")
            }
        }
    }
}