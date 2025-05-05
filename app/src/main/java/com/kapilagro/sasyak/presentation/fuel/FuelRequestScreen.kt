// Updated FuelRequestScreen.kt
package com.kapilagro.sasyak.presentation.fuel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.kapilagro.sasyak.domain.models.FuelEntry
import com.kapilagro.sasyak.presentation.common.theme.AgroPrimary
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FuelRequestScreen(
    onBackClick: () -> Unit,
    onFuelRequestCreated: (FuelEntry) -> Unit
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
        SuccessDialog(
            fuelEntry = submittedEntry!!,
            onDismiss = {
                showSuccessDialog = false
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
                    onFuelRequestCreated(entry)
                    showSuccessDialog = true
                },
                enabled = openingStockLiters.isNotBlank() && vehicleType.isNotBlank() &&
                        quantityNeeded.isNotBlank() && purpose.isNotBlank() &&
                        driverName.isNotBlank() && imageUploaded &&
                        vehicleNumber.isNotBlank() && odometerReading.isNotBlank() &&
                        expectedDistance.isNotBlank() && fuelType.isNotBlank(),
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

@Composable
fun SuccessDialog(
    fuelEntry: FuelEntry,
    onDismiss: () -> Unit
) {
    // One-time animation for checkmark
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(800, easing = LinearEasing),
        label = "scaleAnimation"
    )

    Dialog(
        onDismissRequest = { /* Do nothing to prevent accidental dismissal */ }
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Success Icon with Animation
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        modifier = Modifier.size(80.dp),
                        shape = androidx.compose.foundation.shape.CircleShape,
                        color = Color(0xFF4CAF50)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Success",
                            tint = Color.White,
                            modifier = Modifier
                                .size(48.dp)
                                .padding(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Request Sent!",
                    style = MaterialTheme.typography.headlineSmall,
                    color = AgroPrimary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Your manager will notify when they take action on it.",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Request Details
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFFE0F7FA),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        DetailRow("Date", fuelEntry.date)
                        DetailRow("Vehicle Number", fuelEntry.vehicleNumber)
                        DetailRow("Vehicle Type", fuelEntry.vehicleType)
                        DetailRow("Fuel Type", fuelEntry.fuelType)
                        DetailRow("Odometer Reading", "${fuelEntry.odometerReading} km")
                        DetailRow("Opening Stock", "${fuelEntry.openingStock} liters")
                        DetailRow("Quantity Needed", "${fuelEntry.quantityIssued} liters")
                        DetailRow("Expected Distance", "${fuelEntry.expectedDistance} km")
                        DetailRow("Purpose", fuelEntry.purpose)
                        DetailRow("Driver", fuelEntry.driverName)
                        if (fuelEntry.description?.isNotBlank() == true) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Description:",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = fuelEntry.description,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AgroPrimary)
                ) {
                    Text("OK")
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
        )
    }
}