package com.kapilagro.sasyak.presentation.fuel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kapilagro.sasyak.domain.models.FuelDetails
import com.kapilagro.sasyak.presentation.common.components.SuccessDialog
import com.kapilagro.sasyak.presentation.common.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FuelRequestScreen(
    onTaskCreated: () -> Unit,
    onBackClick: () -> Unit,
    viewModel: FuelListViewModel = hiltViewModel()
) {
    val createFuelState by viewModel.createFuelState.collectAsState()

    // Dialog state
    var showSuccessDialog by remember { mutableStateOf(false) }
    var submittedEntry by remember { mutableStateOf<FuelDetails?>(null) }

    // Form fields
    var fuelDate by remember { mutableStateOf(LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))) }
    var vehicleName by remember { mutableStateOf("") }
    var vehicleNameExpanded by remember { mutableStateOf(false) }
    var vehicleNumber by remember { mutableStateOf("") }
    var fuelType by remember { mutableStateOf("") }
    var fuelTypeExpanded by remember { mutableStateOf(false) }
    var quantity by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("liters") }
    var unitExpanded by remember { mutableStateOf(false) }
    var costPerUnit by remember { mutableStateOf("") }
    var totalCost by remember { mutableStateOf("") }
    var driverName by remember { mutableStateOf("") }
    var odometer by remember { mutableStateOf("") }
    var purposeOfFuel by remember { mutableStateOf("") }
    var refillLocation by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var imageUploaded by remember { mutableStateOf(false) }

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
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Fuel Entry") },
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

            // Upload Section with clickable cards
            Text(
                text = "Upload *",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(100.dp)
                        .clickable { imageUploaded = true },
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (imageUploaded) Color(0xFFE0F7FA) else MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (imageUploaded) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Image Uploaded",
                                    tint = Color(0xFF4CAF50),
                                    modifier = Modifier.size(28.dp)
                                )
                                Text("Receipt Uploaded", color = Color(0xFF4CAF50))
                            }
                        } else {
                            Text("Upload Fuel Receipt")
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
            if (createFuelState is FuelListViewModel.CreateFuelState.Loading) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AgroPrimary)
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Submit Button
            Button(
                onClick = {
                    if (vehicleName.isNotBlank() && fuelType.isNotBlank() && quantity.isNotBlank()) {
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
                            uploadedFiles = null // TODO: Handle file uploads
                        )
                        submittedEntry = fuelDetails
                        viewModel.createFuelTask(fuelDetails, description)
                    }
                },
                enabled = vehicleName.isNotBlank() && fuelType.isNotBlank() && quantity.isNotBlank() &&
                        imageUploaded &&
                        createFuelState !is FuelListViewModel.CreateFuelState.Loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AgroPrimary)
            ) {
                Text("Submit")
            }

            // Error message
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