// Update presentation/scouting/ScoutingScreen.kt
package com.kapilagro.sasyak.presentation.scouting

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
import com.kapilagro.sasyak.domain.models.ScoutingDetails
import com.kapilagro.sasyak.presentation.common.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScoutingScreen(
    onTaskCreated: () -> Unit,
    onBackClick: () -> Unit,
    viewModel: ScoutingViewModel = hiltViewModel()
) {
    val createScoutingState by viewModel.createScoutingState.collectAsState()

    // Dialog state
    var showSuccessDialog by remember { mutableStateOf(false) }

    // Form fields
    var scoutingDate by remember { mutableStateOf(LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))) }
    var cropName by remember { mutableStateOf("") }
    var cropNameExpanded by remember { mutableStateOf(false) }
    var row by remember { mutableStateOf("") }
    var rowExpanded by remember { mutableStateOf(false) }
    var treeNo by remember { mutableStateOf("") }
    var treeNoExpanded by remember { mutableStateOf(false) }
    var noOfFruitSeen by remember { mutableStateOf("") }
    var noOfFlowersSeen by remember { mutableStateOf("") }
    var noOfFruitsDropped by remember { mutableStateOf("") }
    var nameOfDisease by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    val crops = listOf(
        "Wheat", "Rice", "Maize", "Barley", "Sorghum",
        "Mango", "Banana", "Apple", "Papaya", "Guava",
        "Tomato", "Potato", "Onion", "Brinjal", "Cabbage",
        "Sugarcane", "Groundnut", "Cotton", "Soybean", "Mustard"
    )
    val rows = (1..20).map { it.toString() }
    val trees = (1..50).map { it.toString() }

    // Handle task creation success
    LaunchedEffect(createScoutingState) {
        if (createScoutingState is ScoutingViewModel.CreateScoutingState.Success) {
            showSuccessDialog = true
        }
    }

    // Success Dialog
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {
                showSuccessDialog = false
                viewModel.clearCreateScoutingState()
                onTaskCreated()
            },
            title = { Text("Success") },
            text = { Text("Scouting task created successfully!") },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        viewModel.clearCreateScoutingState()
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
                title = { Text("Scouting") },
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
            // Scouting Date (Display only)
            OutlinedTextField(
                value = "Scouting date: $scoutingDate",
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

            // Tree No Dropdown
            ExposedDropdownMenuBox(
                expanded = treeNoExpanded,
                onExpandedChange = { treeNoExpanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = treeNo,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Tree no *") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = treeNoExpanded)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    shape = RoundedCornerShape(8.dp)
                )

                ExposedDropdownMenu(
                    expanded = treeNoExpanded,
                    onDismissRequest = { treeNoExpanded = false }
                ) {
                    trees.forEach { treeNumber ->
                        DropdownMenuItem(
                            text = { Text(treeNumber) },
                            onClick = {
                                treeNo = treeNumber
                                treeNoExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Number of Fruits Seen
            OutlinedTextField(
                value = noOfFruitSeen,
                onValueChange = { noOfFruitSeen = it },
                label = { Text("No of fruit seen") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Number of Flowers Seen
            OutlinedTextField(
                value = noOfFlowersSeen,
                onValueChange = { noOfFlowersSeen = it },
                label = { Text("No of Flowers Seen") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Number of Fruits Dropped
            OutlinedTextField(
                value = noOfFruitsDropped,
                onValueChange = { noOfFruitsDropped = it },
                label = { Text("No of Fruits Dropped") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Name of the Disease
            OutlinedTextField(
                value = nameOfDisease,
                onValueChange = { nameOfDisease = it },
                label = { Text("Name of the Disease") },
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
                        Text("Scan Plant Image")
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
                        Text("Scan Plant Video")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Description
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                placeholder = { Text("Enter the description of the plant or disease") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Submit Button
            Button(
                onClick = {
                    if (cropName.isNotBlank() && row.isNotBlank() && treeNo.isNotBlank()) {
                        val scoutingDetails = ScoutingDetails(
                            scoutingDate = scoutingDate,
                            cropName = cropName,
                            row = row.toInt(),
                            treeNo = treeNo.toInt(),
                            noOfFruitSeen = noOfFruitSeen.ifBlank { null },
                            noOfFlowersSeen = noOfFlowersSeen.ifBlank { null },
                            noOfFruitsDropped = noOfFruitsDropped.ifBlank { null },
                            nameOfDisease = nameOfDisease.ifBlank { null },
                            uploadedFiles = null // TODO: Handle file uploads
                        )
                        viewModel.createScoutingTask(scoutingDetails, description)
                    }
                },
                enabled = cropName.isNotBlank() && row.isNotBlank() && treeNo.isNotBlank() &&
                        createScoutingState !is ScoutingViewModel.CreateScoutingState.Loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AgroPrimary)
            ) {
                if (createScoutingState is ScoutingViewModel.CreateScoutingState.Loading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text("Submit")
                }
            }

            // Error message
            if (createScoutingState is ScoutingViewModel.CreateScoutingState.Error) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = (createScoutingState as ScoutingViewModel.CreateScoutingState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}