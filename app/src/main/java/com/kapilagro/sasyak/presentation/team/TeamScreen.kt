package com.kapilagro.sasyak.presentation.team
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kapilagro.sasyak.domain.models.User
import com.kapilagro.sasyak.presentation.common.components.ErrorView

@Composable
fun TeamScreen(
    viewModel: TeamViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
    onTeamMemberClick: (Int) -> Unit
) {
    val state by viewModel.teamState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Team Members") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (state) {
                is TeamViewModel.TeamState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is TeamViewModel.TeamState.Error -> {
                    val errorState = state as TeamViewModel.TeamState.Error
                    ErrorView(
                        message = errorState.message,
                        onRetry = { viewModel.loadTeamMembers() }
                    )
                }
                is TeamViewModel.TeamState.Success -> {
                    val teamMembers = (state as TeamViewModel.TeamState.Success).teamMembers
                    if (teamMembers.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No team members found",
                                style = MaterialTheme.typography.h6
                            )
                        }
                    } else {
                        TeamMembersList(
                            teamMembers = teamMembers,
                            onTeamMemberClick = onTeamMemberClick
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TeamMembersList(
    teamMembers: List<User>,
    onTeamMemberClick: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(teamMembers) { teamMember ->
            TeamMemberCard(
                teamMember = teamMember,
                onClick = { onTeamMemberClick(teamMember.id) }
            )
        }
    }
}

@Composable
fun TeamMemberCard(
    teamMember: User,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = teamMember.name,
                style = MaterialTheme.typography.h6,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            teamMember.phoneNumber?.let {
                Text(
                    text = "Phone: $it",
                    style = MaterialTheme.typography.body2
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
            teamMember.location?.let {
                Text(
                    text = "Location: $it",
                    style = MaterialTheme.typography.body2
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
            Text(
                text = "Role: ${teamMember.role}",
                style = MaterialTheme.typography.body2
            )
        }
    }
}