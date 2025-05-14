package com.kapilagro.sasyak.presentation.tasks.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kapilagro.sasyak.presentation.tasks.TaskViewModel

@Composable
fun TaskTabRow(
    selectedTab: TaskViewModel.TaskTab,
    onTabSelected: (TaskViewModel.TaskTab) -> Unit,
    pendingCount: Int = 0,
    approvedCount: Int = 0,
    rejectedCount: Int = 0,
    modifier: Modifier = Modifier
) {
    val tabs = listOf(
        TabItem(TaskViewModel.TaskTab.PENDING, "Pending", pendingCount),
        TabItem(TaskViewModel.TaskTab.APPROVED, "Approved", approvedCount),
        TabItem(TaskViewModel.TaskTab.REJECTED, "Rejected", rejectedCount)
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        tabs.forEach { tabItem ->
            TaskTab(
                title = tabItem.title,
                count = tabItem.count,
                selected = selectedTab == tabItem.tab,
                onClick = { onTabSelected(tabItem.tab) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

private data class TabItem(
    val tab: TaskViewModel.TaskTab,
    val title: String,
    val count: Int
)

@Composable
fun TaskTab(
    title: String,
    count: Int,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryGreen = Color(0xFF25D366)

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = if (selected) primaryGreen.copy(alpha = 0.2f) else Color.Transparent,
        border = if (!selected) BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f)) else null,
        modifier = modifier
            .height(40.dp)
            .clickable { onClick() }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (selected) primaryGreen else Color.Gray
            )

            if (count > 0) {
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (selected) primaryGreen else Color.Gray
                )
            }
        }
    }
}