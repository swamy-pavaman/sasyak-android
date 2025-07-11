//package com.kapilagro.sasyak.presentation.tasks.components
//
//import androidx.compose.foundation.BorderStroke
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.width
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Surface
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import com.kapilagro.sasyak.presentation.tasks.TaskViewModel
//
//@Composable
//fun TaskTabRow(
//    selectedTab: TaskViewModel.TaskTab,
//    onTabSelected: (TaskViewModel.TaskTab) -> Unit,
//    pendingCount: Int = 0,
//    approvedCount: Int = 0,
//    rejectedCount: Int = 0,
//    modifier: Modifier = Modifier
//) {
//    val tabs = listOf(
//        TabItem(TaskViewModel.TaskTab.PENDING, "Pending", pendingCount),
//        TabItem(TaskViewModel.TaskTab.APPROVED, "Approved", approvedCount),
//        TabItem(TaskViewModel.TaskTab.REJECTED, "Rejected", rejectedCount)
//    )
//
//    Row(
//        modifier = modifier
//            .fillMaxWidth()
//            .padding(horizontal = 16.dp, vertical = 8.dp),
//        horizontalArrangement = Arrangement.spacedBy(8.dp)
//    ) {
//        tabs.forEach { tabItem ->
//            TaskTab(
//                title = tabItem.title,
//                count = tabItem.count,
//                selected = selectedTab == tabItem.tab,
//                onClick = { onTabSelected(tabItem.tab) },
//                modifier = Modifier.weight(1f)
//            )
//        }
//    }
//}
//
//private data class TabItem(
//    val tab: TaskViewModel.TaskTab,
//    val title: String,
//    val count: Int
//)
//
//@Composable
//fun TaskTab(
//    title: String,
//    count: Int,
//    selected: Boolean,
//    onClick: () -> Unit,
//    modifier: Modifier = Modifier
//) {
//    val primaryGreen = Color(0xFF25D366)
//
//    Surface(
//        shape = RoundedCornerShape(24.dp),
//        color = if (selected) primaryGreen.copy(alpha = 0.2f) else Color.Transparent,
//        border = if (!selected) BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f)) else null,
//        modifier = modifier
//            .height(40.dp)
//            .clickable { onClick() }
//    ) {
//        Row(
//            verticalAlignment = Alignment.CenterVertically,
//            horizontalArrangement = Arrangement.Center,
//            modifier = Modifier.padding(horizontal = 12.dp)
//        ) {
//            Text(
//                text = title,
//                style = MaterialTheme.typography.bodyMedium,
//                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
//                color = if (selected) primaryGreen else Color.Gray
//            )
//
//            if (count > 0) {
//                Spacer(modifier = Modifier.width(4.dp))
//                Text(
//                    text = count.toString(),
//                    style = MaterialTheme.typography.bodyMedium,
//                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
//                    color = if (selected) primaryGreen else Color.Gray
//                )
//            }
//        }
//    }
//}

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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * A reusable TabRow composable that displays a list of tabs with titles and optional counts.
 *
 * @param selectedTab The currently selected tab identifier.
 * @param tabs The list of tabs to display, each with an identifier, title, and count.
 * @param onTabSelected Callback invoked when a tab is selected, passing the tab's identifier.
 * @param modifier Modifier for the TabRow.
 * @param selectedColor The color used for the selected tab's background and text. Defaults to a green shade.
 * @param unselectedBorderColor The border color for unselected tabs. Defaults to light gray.
 */
@Composable
fun <T> TaskTabRow(
    selectedTab: T,
    tabs: List<TabItem<T>>,
    onTabSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    selectedColor: Color = Color(0xFF25D366), // Primary green
    unselectedBorderColor: Color = Color.LightGray.copy(alpha = 0.5f)
) {
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
                selected = selectedTab == tabItem.id,
                onClick = { onTabSelected(tabItem.id) },
                modifier = Modifier.weight(1f),
                selectedColor = selectedColor,
                unselectedBorderColor = unselectedBorderColor
            )
        }
    }
}

/**
 * Data class representing a single tab item.
 *
 * @param id The unique identifier for the tab (can be any type).
 * @param title The display title of the tab.
 * @param count The optional count to display next to the title (e.g., number of items).
 */
data class TabItem<T>(
    val id: T,
    val title: String,
    val count: Int = 0
)

/**
 * A single tab composable used within TaskTabRow.
 *
 * @param title The title of the tab.
 * @param count The count to display next to the title (if > 0).
 * @param selected Whether this tab is currently selected.
 * @param onClick Callback invoked when the tab is clicked.
 * @param modifier Modifier for the tab.
 * @param selectedColor The color for the selected tab's background and text.
 * @param unselectedBorderColor The border color for unselected tabs.
 */
@Composable
fun TaskTab(
    title: String,
    count: Int,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selectedColor: Color,
    unselectedBorderColor: Color
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = if (selected) selectedColor.copy(alpha = 0.2f) else Color.Transparent,
        border = if (!selected) BorderStroke(1.dp, unselectedBorderColor) else null,
        modifier = modifier
            .height(40.dp)
            .clickable { onClick() }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (selected) selectedColor else Color.Gray,
                maxLines = 1,
            )

//            if (count > 0) {
//                Spacer(modifier = Modifier.width(4.dp))
//                Text(
//                    text = count.toString(),
//                    style = MaterialTheme.typography.bodyMedium,
//                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
//                    color = if (selected) selectedColor else Color.Gray
//                )
//            }
        }
    }
}