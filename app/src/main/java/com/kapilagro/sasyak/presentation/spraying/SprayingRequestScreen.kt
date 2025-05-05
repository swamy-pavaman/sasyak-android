package com.kapilagro.sasyak.presentation.spraying

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
import com.kapilagro.sasyak.domain.models.SprayingDetails
import com.kapilagro.sasyak.presentation.common.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SprayingRequestScreen(
    onTaskCreated: () -> Unit,
    onBackClick: () -> Unit,
    viewModel: SprayingViewModel = hiltViewModel()
) {
    val createSprayingState by viewModel.createSprayingState.collectAsState()

    // Dialog state
    var showSuccessDialog by remember { mutableStateOf(false) }

    // Form fields
    var sprayingDate by remember { mutableStateOf(LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))) }
    var cropName by remember { mutableStateOf("") }
    var cropNameExpanded by remember { mutableStateOf(false) }
    var fieldId by remember { mutableStateOf("") }
    var fieldIdExpanded by remember { mutableStateOf(false) }
    var areaSize by remember { mutableStateOf("") }
    var pesticideName by remember { mutableStateOf("") }
    var pesticideNameExpanded by remember { mutableStateOf(false) }
    var pesticideQuantity by remember { mutableStateOf("") }
    var pesticideUnit by remember { mutableStateOf("L") }
    var pesticideUnitExpanded by remember { mutableStateOf(false) }
    var waterQuantity by remember { mutableStateOf("") }
    var waterUnit by remember { mutableStateOf("L") }
    var waterUnitExpanded by remember { mutableStateOf(false) }
    var targetPests by remember { mutableStateOf("") }
    var weatherCondition by remember { mutableStateOf("") }
    var temperature by remember { mutableStateOf("") }
    var windSpeed by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    val crops = listOf(
        "Wheat", "Rice", "Maize", "Barley", "Sorghum",
        "Mango", "Banana", "Apple", "Papaya", "Guava",
        "Tomato", "Potato", "Onion", "Brinjal", "Cabbage",
        "Sugarcane", "Groundnut", "Cotton", "Soybean", "Mustard"
    )

    val fields = listOf("Field-001", "Field-002", "Field-003", "Field-004", "Field-005")

    val pesticides = listOf(
        "Deltamethrin", "Chlorpyrifos", "Cypermethrin", "Imidacloprid", "Glyphosate",
        "Malathion", "Acetamiprid", "Fipronil", "Carbaryl", "Permethrin",
        "Neem Oil", "Bacillus thuringiensis", "Azadirachtin", "Lambdacyhalothrin", "Spinosad"
    )

    val liquidUnits = listOf("mL", "L", "gal")
    val volumeUnits = listOf("L", "gal")

    // Handle task creation success
    LaunchedEffect(createSprayingState) {
        if (createSprayingState is SprayingViewModel.CreateSprayingState.Success) {
            showSuccessDialog = true
        }
    }

    // Success Dialog
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {
                showSuccessDialog = false
                viewModel.clearCreateSprayingState()
                onTaskCreated()
            },
            title = { Text("Success") },
            text = { Text("Spraying task created successfully!") },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        viewModel.clearCreateSprayingState()
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
                title = { Text("Spraying Request") },
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

            // Pesticide Name Dropdown
            ExposedDropdownMenuBox(
                expanded = pesticideNameExpanded,
                onExpandedChange = { pesticideNameExpanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = pesticideName,
                    onValueChange = { newValue ->
                        pesticideName = newValue
                        pesticideNameExpanded = true
                    },
                    label = { Text("Pesticide name *") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = pesticideNameExpanded)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    shape = RoundedCornerShape(8.dp)
                )

                ExposedDropdownMenu(
                    expanded = pesticideNameExpanded,
                    onDismissRequest = { pesticideNameExpanded = false }
                ) {
                    pesticides.filter { it.contains(pesticideName, ignoreCase = true) }
                        .forEach { pesticide ->
                            DropdownMenuItem(
                                text = { Text(pesticide) },
                                onClick = {
                                    pesticideName = pesticide
                                    pesticideNameExpanded = false
                                }
                            )
                        }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Pesticide Quantity and Unit in same row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Pesticide Quantity
                OutlinedTextField(
                    value = pesticideQuantity,
                    onValueChange = { pesticideQuantity = it },
                    label = { Text("Pesticide quantity *") },
                    modifier = Modifier.weight(0.6f),
                    shape = RoundedCornerShape(8.dp)
                )

                // Pesticide Unit Dropdown
                ExposedDropdownMenuBox(
                    expanded = pesticideUnitExpanded,
                    onExpandedChange = { pesticideUnitExpanded = it },
                    modifier = Modifier.weight(0.4f)
                ) {
                    OutlinedTextField(
                        value = pesticideUnit,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Unit") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = pesticideUnitExpanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    ExposedDropdownMenu(
                        expanded = pesticideUnitExpanded,
                        onDismissRequest = { pesticideUnitExpanded = false }
                    ) {
                        liquidUnits.forEach { unit ->
                            DropdownMenuItem(
                                text = { Text(unit) },
                                onClick = {
                                    pesticideUnit = unit
                                    pesticideUnitExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Water Quantity and Unit in same row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Water Quantity
                OutlinedTextField(
                    value = waterQuantity,
                    onValueChange = { waterQuantity = it },
                    label = { Text("Water quantity *") },
                    modifier = Modifier.weight(0.6f),
                    shape = RoundedCornerShape(8.dp)
                )

                // Water Unit Dropdown
                ExposedDropdownMenuBox(
                    expanded = waterUnitExpanded,
                    onExpandedChange = { waterUnitExpanded = it },
                    modifier = Modifier.weight(0.4f)
                ) {
                    OutlinedTextField(
                        value = waterUnit,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Unit") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = waterUnitExpanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    ExposedDropdownMenu(
                        expanded = waterUnitExpanded,
                        onDismissRequest = { waterUnitExpanded = false }
                    ) {
                        volumeUnits.forEach { unit ->
                            DropdownMenuItem(
                                text = { Text(unit) },
                                onClick = {
                                    waterUnit = unit
                                    waterUnitExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Target Pests
            OutlinedTextField(
                value = targetPests,
                onValueChange = { targetPests = it },
                label = { Text("Target pests") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            )

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

            // Temperature and Wind Speed in same row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Temperature
                OutlinedTextField(
                    value = temperature,
                    onValueChange = { temperature = it },
                    label = { Text("Temperature (°C)") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                )

                // Wind Speed
                OutlinedTextField(
                    value = windSpeed,
                    onValueChange = { windSpeed = it },
                    label = { Text("Wind speed (km/h)") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                )
            }

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
                        Text("Upload Field Image")
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
                        Text("Upload Equipment Image")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Description
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                placeholder = { Text("Enter any additional details about the spraying task") },
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
                        areaSize.isNotBlank() && pesticideName.isNotBlank() &&
                        pesticideQuantity.isNotBlank() && waterQuantity.isNotBlank()) {
                        val sprayingDetails = SprayingDetails(
                            sprayingDate = sprayingDate,
                            cropName = cropName,
                            fieldId = fieldId,
                            areaSize = areaSize.toDoubleOrNull() ?: 0.0,
                            pesticideName = pesticideName,
                            pesticideQuantity = pesticideQuantity.toDoubleOrNull() ?: 0.0,
                            pesticideUnit = pesticideUnit,
                            waterQuantity = waterQuantity.toDoubleOrNull() ?: 0.0,
                            waterUnit = waterUnit,
                            targetPests = targetPests.ifBlank { null },
                            weatherCondition = weatherCondition.ifBlank { null },
                            temperature = temperature.ifBlank { null },
                            windSpeed = windSpeed.ifBlank { null },
                            uploadedFiles = null // TODO: Handle file uploads
                        )
                        viewModel.createSprayingTask(sprayingDetails, description)
                    }
                },
                enabled = cropName.isNotBlank() && fieldId.isNotBlank() &&
                        areaSize.isNotBlank() && pesticideName.isNotBlank() &&
                        pesticideQuantity.isNotBlank() && waterQuantity.isNotBlank() &&
                        createSprayingState !is SprayingViewModel.CreateSprayingState.Loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AgroPrimary)
            ) {
                if (createSprayingState is SprayingViewModel.CreateSprayingState.Loading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text("Submit")
                }
            }

            // Error message
            if (createSprayingState is SprayingViewModel.CreateSprayingState.Error) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = (createSprayingState as SprayingViewModel.CreateSprayingState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}