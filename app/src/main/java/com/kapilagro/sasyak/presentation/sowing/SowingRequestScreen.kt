package com.kapilagro.sasyak.presentation.sowing

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
import com.kapilagro.sasyak.domain.models.SowingDetails
import com.kapilagro.sasyak.presentation.common.components.SuccessDialog
import com.kapilagro.sasyak.presentation.common.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SowingRequestScreen(
    onTaskCreated: () -> Unit,
    onBackClick: () -> Unit,
    viewModel: SowingListViewModel = hiltViewModel()
) {
    val createSowingState by viewModel.createSowingState.collectAsState()

    // Dialog state
    var showSuccessDialog by remember { mutableStateOf(false) }
    var submittedEntry by remember { mutableStateOf<SowingDetails?>(null) }

    // Form fields
    var sowingDate by remember { mutableStateOf(LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))) }
    var cropName by remember { mutableStateOf("") }
    var cropNameExpanded by remember { mutableStateOf(false) }
    var row by remember { mutableStateOf("") }
    var rowExpanded by remember { mutableStateOf(false) }
    var fieldArea by remember { mutableStateOf("") }
    var seedVariety by remember { mutableStateOf("") }
    var seedVarietyExpanded by remember { mutableStateOf(false) }
    var seedQuantity by remember { mutableStateOf("") }
    var seedUnit by remember { mutableStateOf("kg") }
    var seedUnitExpanded by remember { mutableStateOf(false) }
    var sowingMethod by remember { mutableStateOf("") }
    var sowingMethodExpanded by remember { mutableStateOf(false) }
    var seedTreatment by remember { mutableStateOf("") }
    var spacingBetweenRows by remember { mutableStateOf("") }
    var spacingBetweenPlants by remember { mutableStateOf("") }
    var soilCondition by remember { mutableStateOf("") }
    var weatherCondition by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var imageUploaded by remember { mutableStateOf(false) }

    val crops = listOf(
        "Wheat", "Rice", "Maize", "Barley", "Sorghum",
        "Mango", "Banana", "Apple", "Papaya", "Guava",
        "Tomato", "Potato", "Onion", "Brinjal", "Cabbage",
        "Sugarcane", "Groundnut", "Cotton", "Soybean", "Mustard"
    )

    val rows = (1..20).map { it.toString() }

    val seedVarieties = mapOf(
        "Wheat" to listOf("HD-2967", "PBW-343", "WH-542", "HD-3086", "DBW-17"),
        "Rice" to listOf("Pusa Basmati-1", "IR-36", "IR-64", "MTU-7029", "Swarna"),
        "Maize" to listOf("DHM-117", "Pratap Hybrid-1", "Bio-9681", "Ganga-11", "PAC-751"),
        "Cotton" to listOf("Bt Cotton", "H-6", "F-1378", "LRA-5166", "MCU-5")
    ).withDefault { listOf("Standard", "Hybrid", "Local") }

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
            message = "Your manager will be notified when they take action on it.",
            details = details,
            description = description,
            primaryButtonText = "OK",
            onPrimaryButtonClick = {
                showSuccessDialog = false
                viewModel.clearCreateSowingState()
                onTaskCreated()
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
                                    // Reset seed variety if crop changes
                                    seedVariety = ""
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
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Row *") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = rowExpanded)
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
                    rows.forEach { rowNumber ->
                        DropdownMenuItem(
                            text = { Text(rowNumber) },
                            onClick = {
                                row = rowNumber
                                rowExpanded = false
                            }
                        )
                    }
                }
            }

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
                    onValueChange = { newValue ->
                        seedVariety = newValue
                        seedVarietyExpanded = true
                    },
                    label = { Text("Seed variety *") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = seedVarietyExpanded)
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
                    val varieties = if (cropName.isNotBlank()) seedVarieties.getValue(cropName) else emptyList()
                    varieties.filter { it.contains(seedVariety, ignoreCase = true) }
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
                    sowingMethods.filter { it.contains(sowingMethod, ignoreCase = true) }
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
                                Text("Image Uploaded", color = Color(0xFF4CAF50))
                            }
                        } else {
                            Text("Upload Sowing Image")
                        }
                    }
                }

                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(100.dp)
                        .clickable { /* Handle video upload */ },
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Upload Sowing Video")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

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
            if (createSowingState is SowingListViewModel.CreateSowingState.Loading) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AgroPrimary)
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Submit Button
            Button(
                onClick = {
                    if (cropName.isNotBlank() && row.isNotBlank() && seedVariety.isNotBlank() && sowingMethod.isNotBlank()) {
                        val sowingDetails = SowingDetails(
                            sowingDate = sowingDate,
                            cropName = cropName,
                            row = row.toInt(),
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
                            uploadedFiles = null // TODO: Handle file uploads
                        )
                        submittedEntry = sowingDetails
                        viewModel.createSowingTask(sowingDetails, description)
                    }
                },
                enabled = cropName.isNotBlank() && row.isNotBlank() && seedVariety.isNotBlank() &&
                        sowingMethod.isNotBlank() && imageUploaded &&
                        createSowingState !is SowingListViewModel.CreateSowingState.Loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AgroPrimary)
            ) {
                Text("Submit")
            }

            // Error message
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