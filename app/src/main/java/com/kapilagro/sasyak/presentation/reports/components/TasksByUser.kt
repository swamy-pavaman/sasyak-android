package com.kapilagro.sasyak.presentation.reports.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun TasksByUser(
    userData: Map<String, Int>
) {
    val maxValue = userData.values.maxOrNull() ?: 1

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(Color.White, shape = RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Text(
            text = "Tasks By User",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        userData.forEach { (user, value) ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Text(
                    text = user.replaceFirstChar { it.uppercase() },
                    modifier = Modifier.width(100.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1
                )

                // Progress bar
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .background(Color(0xFFE0E0E0), shape = RoundedCornerShape(4.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(value / maxValue.toFloat())
                            .background(Color(0xFF4A90E2), shape = RoundedCornerShape(4.dp))
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = value.toString(),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}