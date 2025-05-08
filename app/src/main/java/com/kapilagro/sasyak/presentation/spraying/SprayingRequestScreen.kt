package com.kapilagro.sasyak.presentation.spraying

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
import com.kapilagro.sasyak.domain.models.SprayingDetails
import com.kapilagro.sasyak.presentation.common.components.SuccessDialog
import com.kapilagro.sasyak.presentation.common.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SprayingRequestScreen(
    onTaskCreated: () -> Unit,
    onBackClick: () -> Unit,
    viewModel: SprayingListViewModel = hiltViewModel()
) {
    val createSprayingState by viewModel.createSprayingState.collectAsState()

    // Dialog state
    var showSuccessDialog by remember { mutableStateOf(false) }
    var submittedEntry by remember { mutableStateOf<SprayingDetails?>(null) }

    // Form fields
    var sprayingDate by remember { mutableStateOf(LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))) }
    var cropName by remember { mutableStateOf("") }
    var cropNameExpanded by remember { mutableStateOf(false) }
    var row by remember { mutableStateOf("") }
    var rowExpanded by remember { mutableStateOf(false) }
    var fieldArea by remember { mutableStateOf("") }
    var chemicalName by remember { mutableStateOf("") }
    var chemicalNameExpanded by remember { mutableStateOf(false) }
    var dosage by remember { mutableStateOf("") }
    var sprayingMethod by remember { mutableStateOf("") }
    var sprayingMethodExpanded by remember { mutableStateOf(false) }
    var targetPest by remember { mutableStateOf("") }
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

    val chemicals = listOf(
        "Glyphosate", "2,4-D", "Atrazine", "Paraquat", "Pendimethalin",
        "Chlorpyrifos", "Cypermethrin", "Lambda-cyhalothrin", "Carbofuran", "Imidacloprid",
        "Mancozeb", "Copper Oxychloride", "Carbendazim", "Metalaxyl", "Thiram"
    )

    val sprayingMethods = listOf(
        "Backpack Sprayer", "Boom Sprayer", "Aerial Spraying", "Drip Application",
        "Fogger", "Mist Blower", "Hand Sprayer", "Tractor Mounted Sprayer"
    )

    // Handle task creation success
    LaunchedEffect(createSprayingState) {
        when (createSprayingState) {
            is SprayingListViewModel.CreateSprayingState.Success -> {
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
            "Date" to submittedEntry!!.sprayingDate,
            "Crop Name" to submittedEntry!!.cropName,
            "Row" to submittedEntry!!.row.toString(),
            "Field Area" to (submittedEntry!!.fieldArea?.plus(" acres") ?: "Not specified"),
            "Chemical" to submittedEntry!!.chemicalName,
            "Dosage" to (submittedEntry!!.dosage ?: "Not specified"),
            "Method" to submittedEntry!!.sprayingMethod,
            "Target Pest" to (submittedEntry!!.targetPest ?: "Not specified"),
            "Weather" to (submittedEntry!!.weatherCondition ?: "Not specified")
        )

        SuccessDialog(
            title = "Spraying Report Sent!",
            message = "Your manager will be notified when they take action on it.",
            details = details,
            description = description,
            primaryButtonText = "OK",
            onPrimaryButtonClick = {
                showSuccessDialog = false
                viewModel.clearCreateSprayingState()
                onTaskCreated()
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Spraying") },
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

            // Chemical Name Dropdown
            ExposedDropdownMenuBox(
                expanded = chemicalNameExpanded,
                onExpandedChange = { chemicalNameExpanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = chemicalName,
                    onValueChange = { newValue ->
                        chemicalName = newValue
                        chemicalNameExpanded = true
                    },
                    label = { Text("Chemical name *") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = chemicalNameExpanded)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    shape = RoundedCornerShape(8.dp)
                )

                ExposedDropdownMenu(
                    expanded = chemicalNameExpanded,
                    onDismissRequest = { chemicalNameExpanded = false }
                ) {
                    chemicals.filter { it.contains(chemicalName, ignoreCase = true) }
                        .forEach { chemical ->
                            DropdownMenuItem(
                                text = { Text(chemical) },
                                onClick = {
                                    chemicalName = chemical
                                    chemicalNameExpanded = false
                                }
                            )
                        }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Dosage
            OutlinedTextField(
                value = dosage,
                onValueChange = { dosage = it },
                label = { Text("Dosage (ml/litre)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Spraying Method Dropdown
            ExposedDropdownMenuBox(
                expanded = sprayingMethodExpanded,
                onExpandedChange = { sprayingMethodExpanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = sprayingMethod,
                    onValueChange = { newValue ->
                        sprayingMethod = newValue
                        sprayingMethodExpanded = true
                    },
                    label = { Text("Spraying method *") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = sprayingMethodExpanded)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    shape = RoundedCornerShape(8.dp)
                )

                ExposedDropdownMenu(
                    expanded = sprayingMethodExpanded,
                    onDismissRequest = { sprayingMethodExpanded = false }
                ) {
                    sprayingMethods.filter { it.contains(sprayingMethod, ignoreCase = true) }
                        .forEach { method ->
                            DropdownMenuItem(
                                text = { Text(method) },
                                onClick = {
                                    sprayingMethod = method
                                    sprayingMethodExpanded = false
                                }
                            )
                        }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Target Pest
            OutlinedTextField(
                value = targetPest,
                onValueChange = { targetPest = it },
                label = { Text("Target pest/disease") },
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
                            Text("Upload Spraying Image")
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
                        Text("Upload Spraying Video")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Description
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                placeholder = { Text("Enter description of the spraying activity") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Loading indicator
            if (createSprayingState is SprayingListViewModel.CreateSprayingState.Loading) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AgroPrimary)
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Submit Button
            Button(
                onClick = {
                    if (cropName.isNotBlank() && row.isNotBlank() && chemicalName.isNotBlank() && sprayingMethod.isNotBlank()) {
                        val sprayingDetails = SprayingDetails(
                            sprayingDate = sprayingDate,
                            cropName = cropName,
                            row = row.toInt(),
                            fieldArea = fieldArea.ifBlank { null },
                            chemicalName = chemicalName,
                            dosage = dosage.ifBlank { null },
                            sprayingMethod = sprayingMethod,
                            targetPest = targetPest.ifBlank { null },
                            weatherCondition = weatherCondition.ifBlank { null },
                            uploadedFiles = null // TODO: Handle file uploads
                        )
                        submittedEntry = sprayingDetails
                        viewModel.createSprayingTask(sprayingDetails, description)
                    }
                },
                enabled = cropName.isNotBlank() && row.isNotBlank() && chemicalName.isNotBlank() &&
                        sprayingMethod.isNotBlank() && imageUploaded &&
                        createSprayingState !is SprayingListViewModel.CreateSprayingState.Loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AgroPrimary)
            ) {
                Text("Submit")
            }

            // Error message
            if (createSprayingState is SprayingListViewModel.CreateSprayingState.Error) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = (createSprayingState as SprayingListViewModel.CreateSprayingState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}