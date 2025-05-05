package com.kapilagro.sasyak.presentation.sowing

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kapilagro.sasyak.domain.models.SowingDetails
import com.kapilagro.sasyak.presentation.common.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SowingRequestScreen(
    onTaskCreated: () -> Unit,
    onBackClick: () -> Unit,
    viewModel: SowingViewModel = hiltViewModel()
) {
    val createSowingState by viewModel.createSowingState.collectAsState()

    // Dialog state
    var showSuccessDialog by remember { mutableStateOf(false) }

    // Form fields
    var sowingDate by remember { mutableStateOf(LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))) }
    var cropName by remember { mutableStateOf("") }
    var cropNameExpanded by remember { mutableStateOf(false) }
    var fieldId by remember { mutableStateOf("") }
    var fieldIdExpanded by remember { mutableStateOf(false) }
    var areaSize by remember { mutableStateOf("") }
    var seedVariety by remember { mutableStateOf("") }
    var seedVarietyExpanded by remember { mutableStateOf(false) }
    var seedQuantity by remember { mutableStateOf("") }
    var seedUnit by remember { mutableStateOf("Kg") }
    var seedUnitExpanded by remember { mutableStateOf(false) }
    var spacingBetweenPlants by remember { mutableStateOf("") }
    var spacingBetweenRows by remember { mutableStateOf("") }
    var soilCondition by remember { mutableStateOf("") }
    var weatherCondition by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    val crops = listOf(
        "Wheat", "Rice", "Maize", "Barley", "Sorghum",
        "Mango", "Banana", "Apple", "Papaya", "Guava",
        "Tomato", "Potato", "Onion", "Brinjal", "Cabbage",
        "Sugarcane", "Groundnut", "Cotton", "Soybean", "Mustard"
    )

    val fields = listOf("Field-001", "Field-002", "Field-003", "Field-004", "Field-005")

    val seedVarieties = mapOf(
        "Wheat" to listOf("HD-2967", "WH-1105", "PBW-343", "HD-3086"),
        "Rice" to listOf("Basmati-370", "IR-36", "IR-64", "Pusa Basmati"),
        "Maize" to listOf("DHM-117", "Pioneer P3501", "Dekalb DKC9144"),
        "Cotton" to listOf("LH-2086", "H-1098", "MCU-5", "Surabi"),
    )

    val units = listOf("Kg", "g", "Tonnes", "Quintals")

    // Handle task creation success
    LaunchedEffect(createSowingState) {
        if (createSowingState is SowingViewModel.CreateSowingState.Success) {
            showSuccessDialog = true
        }
    }

    // Success Dialog
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {
                showSuccessDialog = false
                viewModel.clearCreateSowingState()
                onTaskCreated()
            },
            title = { Text("Success") },
            text = { Text("Sowing task created successfully!") },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        viewModel.clearCreateSowingState()
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
                title = { Text("Sowing Request") },
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
                                    // Reset seed variety when crop changes
                                    seedVariety = ""
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
                shape = RoundedCornerShape(8.dp)
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
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Seed variety *") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = seedVarietyExpanded)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    shape = RoundedCornerShape(8.dp),
                    enabled = cropName.isNotBlank()
                )

                ExposedDropdownMenu(
                    expanded = seedVarietyExpanded,
                    onDismissRequest = { seedVarietyExpanded = false }
                ) {
                    (if (cropName.isNotBlank()) seedVarieties[cropName] ?: listOf("Other") else listOf())
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

            // Seed Quantity and Unit in same row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Seed Quantity
                OutlinedTextField(
                    value = seedQuantity,
                    onValueChange = { seedQuantity = it },
                    label = { Text("Seed quantity *") },
                    modifier = Modifier.weight(0.6f),
                    shape = RoundedCornerShape(8.dp)
                )

                // Seed Unit Dropdown
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

            // Spacing between plants
            OutlinedTextField(
                value = spacingBetweenPlants,
                onValueChange = { spacingBetweenPlants = it },
                label = { Text("Spacing between plants (cm)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Spacing between rows
            OutlinedTextField(
                value = spacingBetweenRows,
                onValueChange = { spacingBetweenRows = it },
                label = { Text("Spacing between rows (cm)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Soil Condition
            OutlinedTextField(
                value = soilCondition,
                onValueChange = { soilCondition = it },
                label = { Text("Soil condition") },
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

            // Upload Section - TODO: Implement file upload
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
                        Text("Upload Seed Image")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Description
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                placeholder = { Text("Enter any additional details about the sowing task") },
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
                        areaSize.isNotBlank() && seedVariety.isNotBlank() &&
                        seedQuantity.isNotBlank()) {
                        val sowingDetails = SowingDetails(
                            sowingDate = sowingDate,
                            cropName = cropName,
                            fieldId = fieldId,
                            areaSize = areaSize.toDoubleOrNull() ?: 0.0,
                            seedVariety = seedVariety,
                            seedQuantity = seedQuantity.toDoubleOrNull() ?: 0.0,
                            seedUnit = seedUnit,
                            spacingBetweenPlants = spacingBetweenPlants.ifBlank { null },
                            spacingBetweenRows = spacingBetweenRows.ifBlank { null },
                            soilCondition = soilCondition.ifBlank { null },
                            weatherCondition = weatherCondition.ifBlank { null },
                            uploadedFiles = null // TODO: Handle file uploads
                        )
                        viewModel.createSowingTask(sowingDetails, description)
                    }
                },
                enabled = cropName.isNotBlank() && fieldId.isNotBlank() &&
                        areaSize.isNotBlank() && seedVariety.isNotBlank() &&
                        seedQuantity.isNotBlank() &&
                        createSowingState !is SowingViewModel.CreateSowingState.Loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AgroPrimary)
            ) {
                if (createSowingState is SowingViewModel.CreateSowingState.Loading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text("Submit")
                }
            }

            // Error message
            if (createSowingState is SowingViewModel.CreateSowingState.Error) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = (createSowingState as SowingViewModel.CreateSowingState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}