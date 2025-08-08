package com.kapilagro.sasyak.presentation.common.components

import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.navigation.NavController

@Composable
fun TaskSubmittedDialog(
    navController: NavController,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text("Task Submitted") },
        text = { Text("Your task has been queued for upload in the background.") },
        confirmButton = {
            TextButton(onClick = { onDismiss() ; navController.popBackStack()}) {
                Text("OK")
            }
        }
    )
}
