package com.kapilagro.sasyak.presentation.yield

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
import com.kapilagro.sasyak.domain.models.YieldDetails
import com.kapilagro.sasyak.presentation.common.components.SuccessDialog
import com.kapilagro.sasyak.presentation.common.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YieldRequestScreen(
    onTaskCreated: () -> Unit,
    onBackClick: () -> Unit,
    viewModel: YieldListViewModel = hiltViewModel()
) {
    val createYieldState by viewModel.createYieldState.collectAsState()

    // Dialog state
    var showSuccessDialog by remember { mutableStateOf(false) }
    var submittedEntry by remember { mutableStateOf<YieldDetails?>(null) }

    // Form fields
    var harvestDate by remember { mutableStateOf(LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))) }
    var cropName by remember { mutableStateOf("") }
    var cropNameExpanded by remember { mutableStateOf(false) }
    var row by remember { mutableStateOf("") }
    var rowExpanded by remember { mutableStateOf(false) }
    var fieldArea by remember { mutableStateOf("") }
    var yieldQuantity by remember { mutableStateOf("") }
    var yieldUnit by remember { mutableStateOf("kg") }
    var yieldUnitExpanded by remember { mutableStateOf(false) }
    var qualityGrade by remember { mutableStateOf("") }
    var qualityGradeExpanded by remember { mutableStateOf(false) }
    var moistureContent by remember { mutableStateOf("") }
    var harvestMethod by remember { mutableStateOf("") }
    var harvestMethodExpanded by remember { mutableStateOf(false) }
    var notes by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var imageUploaded by remember { mutableStateOf(false) }

    val crops = listOf(
        "Wheat", "Rice", "Maize", "Barley", "Sorghum",
        "Mango", "Banana", "Apple", "Papaya", "Guava",
        "Tomato", "Potato", "Onion", "Brinjal", "Cabbage",
        "Sugarcane", "Groundnut", "Cotton", "Soybean", "Mustard"
    )

    val rows = (1..20).map { it.toString() }

    val units = listOf("kg", "tonnes", "quintals", "bags")

    val grades = listOf("A", "B", "C", "Premium", "Standard", "Low")

    val harvestMethods = listOf(
        "Manual", "Combine Harvester", "Mechanical", "Semi-mechanical"
    )

    // Handle task creation success
    LaunchedEffect(createYieldState) {
        when (createYieldState) {
            is YieldListViewModel.CreateYieldState.Success -> {
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
            "Date" to submittedEntry!!.harvestDate,
            "Crop Name" to submittedEntry!!.cropName,
            "Row" to submittedEntry!!.row.toString(),
            "Field Area" to (submittedEntry!!.fieldArea?.plus(" acres") ?: "Not specified"),
            "Yield" to "${submittedEntry!!.yieldQuantity} ${submittedEntry!!.yieldUnit}",
            "Quality" to (submittedEntry!!.qualityGrade ?: "Not specified"),
            "Moisture" to (submittedEntry!!.moistureContent?.plus("%") ?: "Not specified"),
            "Method" to (submittedEntry!!.harvestMethod ?: "Not specified")
        )

        SuccessDialog(
            title = "Yield Report Sent!",
            message = "Your manager will be notified when they take action on it.",
            details = details,
            description = description,
            primaryButtonText = "OK",
            onPrimaryButtonClick = {
                showSuccessDialog = false
                viewModel.clearCreateYieldState()
                onTaskCreated()
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

            // Yield Quantity and Unit
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = yieldQuantity,
                    onValueChange = { yieldQuantity = it },
                    label = { Text("Yield quantity *") },
                    modifier = Modifier.weight(0.6f),
                    shape = RoundedCornerShape(8.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )

                ExposedDropdownMenuBox(
                    expanded = yieldUnitExpanded,
                    onExpandedChange = { yieldUnitExpanded = it },
                    modifier = Modifier.weight(0.4f)
                ) {
                    OutlinedTextField(
                        value = yieldUnit,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Unit *") },
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
                        units.forEach { unit ->
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

            // Quality Grade Dropdown
            ExposedDropdownMenuBox(
                expanded = qualityGradeExpanded,
                onExpandedChange = { qualityGradeExpanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = qualityGrade,
                    onValueChange = { newValue ->
                        qualityGrade = newValue
                        qualityGradeExpanded = true
                    },
                    label = { Text("Quality grade") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = qualityGradeExpanded)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    shape = RoundedCornerShape(8.dp)
                )

                ExposedDropdownMenu(
                    expanded = qualityGradeExpanded,
                    onDismissRequest = { qualityGradeExpanded = false }
                ) {
                    grades.filter { it.contains(qualityGrade, ignoreCase = true) }
                        .forEach { grade ->
                            DropdownMenuItem(
                                text = { Text(grade) },
                                onClick = {
                                    qualityGrade = grade
                                    qualityGradeExpanded = false
                                }
                            )
                        }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Moisture Content
            OutlinedTextField(
                value = moistureContent,
                onValueChange = { moistureContent = it },
                label = { Text("Moisture content (%)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Harvest Method Dropdown
            ExposedDropdownMenuBox(
                expanded = harvestMethodExpanded,
                onExpandedChange = { harvestMethodExpanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = harvestMethod,
                    onValueChange = { newValue ->
                        harvestMethod = newValue
                        harvestMethodExpanded = true
                    },
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
                    harvestMethods.filter { it.contains(harvestMethod, ignoreCase = true) }
                        .forEach { method ->
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
                            Text("Upload Harvest Image")
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
                        Text("Upload Harvest Video")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Notes/Description
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Notes/Description") },
                placeholder = { Text("Enter details about the harvest, quality, and any observations") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Loading indicator
            if (createYieldState is YieldListViewModel.CreateYieldState.Loading) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AgroPrimary)
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Submit Button
            Button(
                onClick = {
                    if (cropName.isNotBlank() && row.isNotBlank() && yieldQuantity.isNotBlank() && yieldUnit.isNotBlank()) {
                        val yieldDetails = YieldDetails(
                            harvestDate = harvestDate,
                            cropName = cropName,
                            row = row.toInt(),
                            fieldArea = fieldArea.ifBlank { null },
                            yieldQuantity = yieldQuantity,
                            yieldUnit = yieldUnit,
                            qualityGrade = qualityGrade.ifBlank { null },
                            moistureContent = moistureContent.ifBlank { null },
                            harvestMethod = harvestMethod.ifBlank { null },
                            notes = notes.ifBlank { null },
                            uploadedFiles = null // TODO: Handle file uploads
                        )
                        submittedEntry = yieldDetails
                        viewModel.createYieldTask(yieldDetails, description)
                    }
                },
                enabled = cropName.isNotBlank() && row.isNotBlank() && yieldQuantity.isNotBlank() &&
                        yieldUnit.isNotBlank() && imageUploaded &&
                        createYieldState !is YieldListViewModel.CreateYieldState.Loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AgroPrimary)
            ) {
                Text("Submit")
            }

            // Error message
            if (createYieldState is YieldListViewModel.CreateYieldState.Error) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = (createYieldState as YieldListViewModel.CreateYieldState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}