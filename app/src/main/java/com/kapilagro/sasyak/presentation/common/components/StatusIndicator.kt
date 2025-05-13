package com.kapilagro.sasyak.presentation.common.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kapilagro.sasyak.presentation.common.theme.StatusApproved
import com.kapilagro.sasyak.presentation.common.theme.StatusPending
import com.kapilagro.sasyak.presentation.common.theme.StatusRejected
import java.util.Locale

@Composable
fun StatusIndicator(status: String) {
    val (color, text) = when (status.lowercase()) {
        "pending" -> Pair(StatusPending, "Pending")
        "approved" -> Pair(StatusApproved, "Approved")
        "rejected" -> Pair(StatusRejected, "Rejected")
        else -> Pair(Color.Gray, status.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() })
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.2f),
        modifier = Modifier.height(26.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = color,
                modifier = Modifier.size(8.dp)
            ) {}

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = text,
                style = MaterialTheme.typography.subtitle1.copy(fontWeight = FontWeight.Medium),
                color = color
            )
        }
    }
}