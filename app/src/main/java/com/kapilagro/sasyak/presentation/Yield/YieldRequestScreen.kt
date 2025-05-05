package com.kapilagro.sasyak.presentation.yield

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kapilagro.sasyak.domain.models.YieldDetails
import com.kapilagro.sasyak.presentation.common.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YieldRequestScreen(
    onTaskCreated: () -> Unit,
    onBackClick: () -> Unit,
    viewModel: YieldViewModel = hiltViewModel()
) {
    val createYieldState by viewModel.createYieldState.collectAsState()

    // Dialog state
    var showSuccessDialog by remember { mutableStateOf(false) }

    // Form fields
    var harvestDate by remember { mutableStateOf(LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))) }
    var cropName by remember { mutableStateOf("") }
    var cropNameExpanded by remember { mutableStateOf(false) }
    var fieldId by remember { mutableStateOf("") }
    var fieldIdExpanded by remember { mutableStateOf(false) }
    var areaSize by remember { mutableStateOf("") }
    var harvestedQuantity by remember { mutableStateOf("") }
    var yieldUnit by remember { mutableStateOf("Tonnes") }
    var yieldUnitExpanded by remember { mutableStateOf(false) }
    var grainMoisture by remember { mutableStateOf("") }
    var grainQuality by remember { mutableStateOf("") }
    var grainQualityExpanded by remember { mutableStateOf(false) }
    var harvestMethod by remember { mutableStateOf("") }
    var harvestMethodExpanded by remember { mutableStateOf(false) }
    var laborHours by remember { mutableStateOf("") }
    var fuelUsed by remember { mutableStateOf("") }
    var fuelUnit by remember { mutableStateOf("L") }
    var fuelUnitExpanded by remember { mutableStateOf(false) }
    var weatherCondition by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val crops = listOf(
        "Wheat", "Rice", "Maize", "Barley", "Sorghum",
        "Mango", "Banana", "Apple", "Papaya", "Guava",
        "Tomato", "Potato", "Onion", "Brinjal", "Cabbage",
        "Sugarcane", "Groundnut", "Cotton", "Soybean", "Mustard"
    )

    val fields = listOf("Field-001", "Field-002", "Field-003", "Field-004", "Field-005")
    val yieldUnits = listOf("Kg", "Quintals", "Tonnes", "Bushels", "lb")
    val fuelUnits = listOf("L", "gal")
    val grainQualities = listOf("Premium", "Grade A", "Grade B", "Standard", "Below Standard")
    val harvestMethods = listOf("Combine Harvester", "Manual", "Semi-mechanized", "Custom Harvester")

    // Handle task creation success
    LaunchedEffect(createYieldState) {
        if (createYieldState is YieldViewModel.CreateYieldState.Success) {
            showSuccessDialog = true
        }
    }

    // Success Dialog
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {
                showSuccessDialog = false
                viewModel.clearCreateYieldState()
                onTaskCreated()
            },
            title = { Text("Success") },
            text = { Text("Yield record created successfully!") },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        viewModel.clearCreateYieldState()
                        onTaskCreated()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AgroPrimary)
                ) {
                    Text("OK")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Yield Report") },
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

            // Crop Name Dropdown
            ExposedDropdownMenuBox(
                expanded = cropNameExpanded,
                onExpandedChange = { cropNameExpanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = cropName,
                    onValueChange = { newValue ->
                        cropName = newValue
                        cropNameExpanded = true
                    },
                    label = { Text("Crop name *") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = cropNameExpanded)
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
                    crops.filter { it.contains(cropName, ignoreCase = true) }
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

            // Field ID Dropdown
            ExposedDropdownMenuBox(
                expanded = fieldIdExpanded,
                onExpandedChange = { fieldIdExpanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = fieldId,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Field ID *") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = fieldIdExpanded)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    shape = RoundedCornerShape(8.dp)
                )

                ExposedDropdownMenu(
                    expanded = fieldIdExpanded,
                    onDismissRequest = { fieldIdExpanded = false }
                ) {
                    fields.forEach { field ->
                        DropdownMenuItem(
                            text = { Text(field) },
                            onClick = {
                                fieldId = field
                                fieldIdExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Area Size
            OutlinedTextField(
                value = areaSize,
                onValueChange = { areaSize = it },
                label = { Text("Area size (acres) *") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),

                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Harvested Quantity and Unit in same row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Harvested Quantity
                OutlinedTextField(
                    value = harvestedQuantity,
                    onValueChange = { harvestedQuantity = it },
                    label = { Text("Harvested quantity *") },
                    modifier = Modifier.weight(0.6f),
                    shape = RoundedCornerShape(8.dp),

                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                // Yield Unit Dropdown
                ExposedDropdownMenuBox(
                    expanded = yieldUnitExpanded,
                    onExpandedChange = { yieldUnitExpanded = it },
                    modifier = Modifier.weight(0.4f)
                ) {
                    OutlinedTextField(
                        value = yieldUnit,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Unit") },
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
                        yieldUnits.forEach { unit ->
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

            // Grain Moisture
            OutlinedTextField(
                value = grainMoisture,
                onValueChange = { grainMoisture = it },
                label = { Text("Grain moisture (%)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),

                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Grain Quality Dropdown
            ExposedDropdownMenuBox(
                expanded = grainQualityExpanded,
                onExpandedChange = { grainQualityExpanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = grainQuality,
                    onValueChange = { grainQuality = it },
                    label = { Text("Grain quality") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = grainQualityExpanded)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    shape = RoundedCornerShape(8.dp)
                )

                ExposedDropdownMenu(
                    expanded = grainQualityExpanded,
                    onDismissRequest = { grainQualityExpanded = false }
                ) {
                    grainQualities.forEach { quality ->
                        DropdownMenuItem(
                            text = { Text(quality) },
                            onClick = {
                                grainQuality = quality
                                grainQualityExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Harvest Method Dropdown
            ExposedDropdownMenuBox(
                expanded = harvestMethodExpanded,
                onExpandedChange = { harvestMethodExpanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = harvestMethod,
                    onValueChange = { harvestMethod = it },
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
                    harvestMethods.forEach { method ->
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

            // Labor Hours
            OutlinedTextField(
                value = laborHours,
                onValueChange = { laborHours = it },
                label = { Text("Labor hours") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),

                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Fuel Used and Unit in same row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Fuel Used
                OutlinedTextField(
                    value = fuelUsed,
                    onValueChange = { fuelUsed = it },
                    label = { Text("Fuel used") },
                    modifier = Modifier.weight(0.6f),
                    shape = RoundedCornerShape(8.dp),

                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                // Fuel Unit Dropdown
                ExposedDropdownMenuBox(
                    expanded = fuelUnitExpanded,
                    onExpandedChange = { fuelUnitExpanded = it },
                    modifier = Modifier.weight(0.4f)
                ) {
                    OutlinedTextField(
                        value = fuelUnit,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Unit") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = fuelUnitExpanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    ExposedDropdownMenu(
                        expanded = fuelUnitExpanded,
                        onDismissRequest = { fuelUnitExpanded = false }
                    ) {
                        fuelUnits.forEach { unit ->
                            DropdownMenuItem(
                                text = { Text(unit) },
                                onClick = {
                                    fuelUnit = unit
                                    fuelUnitExpanded = false
                                }
                            )
                        }
                    }
                }
            }

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

            // Upload Section
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
                        .height(100.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Upload Harvest Image")
                    }
                }

                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(100.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Upload Grain Sample Image")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Notes
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes") },
                placeholder = { Text("Enter any additional details about the harvest") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Submit Button
            Button(
                onClick = {
                    if (cropName.isNotBlank() && fieldId.isNotBlank() &&
                        areaSize.isNotBlank() && harvestedQuantity.isNotBlank()) {
                        val yieldDetails = YieldDetails(
                            harvestDate = harvestDate,
                            cropName = cropName,
                            fieldId = fieldId,
                            areaSize = areaSize.toDoubleOrNull() ?: 0.0,
                            harvestedQuantity = harvestedQuantity.toDoubleOrNull() ?: 0.0,
                            yieldUnit = yieldUnit,
                            grainMoisture = grainMoisture.ifBlank { null },
                            grainQuality = grainQuality.ifBlank { null },
                            harvestMethod = harvestMethod.ifBlank { null },
                            laborHours = laborHours.toDoubleOrNull(),
                            fuelUsed = fuelUsed.toDoubleOrNull(),
                            fuelUnit = if (fuelUsed.isBlank()) null else fuelUnit,
                            weatherCondition = weatherCondition.ifBlank { null },
                            notes = notes.ifBlank { null },
                            uploadedFiles = null // TODO: Handle file uploads
                        )
                        viewModel.createYieldTask(yieldDetails, notes)
                    }
                },
                enabled = cropName.isNotBlank() && fieldId.isNotBlank() &&
                        areaSize.isNotBlank() && harvestedQuantity.isNotBlank() &&
                        createYieldState !is YieldViewModel.CreateYieldState.Loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AgroPrimary)
            ) {
                if (createYieldState is YieldViewModel.CreateYieldState.Loading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text("Submit")
                }
            }

            // Error message
            if (createYieldState is YieldViewModel.CreateYieldState.Error) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = (createYieldState as YieldViewModel.CreateYieldState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}