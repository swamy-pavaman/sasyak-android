package com.kapilagro.sasyak.presentation.team

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kapilagro.sasyak.presentation.common.components.ErrorView

@Composable
fun TeamMemberDetailScreen(
    teamMemberId: Int,
    viewModel: TeamMemberDetailViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    val state by viewModel.teamMemberState.collectAsState()

    // Load the team member data
    viewModel.loadTeamMemberDetails(teamMemberId)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Team Member Details") },
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
                is TeamMemberDetailViewModel.TeamMemberState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is TeamMemberDetailViewModel.TeamMemberState.Error -> {
                    val errorState = state as TeamMemberDetailViewModel.TeamMemberState.Error
                    ErrorView(
                        message = errorState.message,
                        onRetry = { viewModel.loadTeamMemberDetails(teamMemberId) }
                    )
                }
                is TeamMemberDetailViewModel.TeamMemberState.Success -> {
                    val teamMember = (state as TeamMemberDetailViewModel.TeamMemberState.Success).teamMember

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = 4.dp
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = teamMember.name,
                                    style = MaterialTheme.typography.h5,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                teamMember.phoneNumber?.let {
                                    Text(
                                        text = "Phone: $it",
                                        style = MaterialTheme.typography.body1
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                }
                                teamMember.location?.let {
                                    Text(
                                        text = "Location: $it",
                                        style = MaterialTheme.typography.body1
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                }
                                Text(
                                    text = "Role: ${teamMember.role}",
                                    style = MaterialTheme.typography.body1
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "Statistics",
                            style = MaterialTheme.typography.h6,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // In a real implementation, you would load and display
                        // statistics for this team member here
                        Text(
                            text = "Stats will be displayed here",
                            style = MaterialTheme.typography.body1
                        )
                    }
                }
            }
        }
    }
}