package com.kapilagro.sasyak.presentation.tasks.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun TaskActionButtons(
    onApproveClick: () -> Unit,
    onRejectClick: () -> Unit,
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Button(
            onClick = onApproveClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            ),
            modifier = Modifier.weight(1f),
            enabled = isEnabled
        ) {
            Icon(
                imageVector = Icons.Outlined.Check,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Approve")
        }

        Spacer(modifier = Modifier.width(16.dp))

        Button(
            onClick = onRejectClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error,
                disabledContainerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
            ),
            modifier = Modifier.weight(1f),
            enabled = isEnabled
        ) {
            Icon(
                imageVector = Icons.Outlined.Close,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Reject")
        }
    }
}