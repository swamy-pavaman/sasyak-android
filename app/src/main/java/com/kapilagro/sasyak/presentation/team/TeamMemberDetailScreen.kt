package com.kapilagro.sasyak.presentation.team

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.kapilagro.sasyak.domain.models.TeamMember
import com.kapilagro.sasyak.presentation.common.components.ErrorView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamMemberDetailScreen(
    teamMemberId: Int,
    viewModel: TeamMemberDetailViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    val state by viewModel.teamMemberState.collectAsState()

    // Load the team member data
    viewModel.loadTeamMemberDetails(teamMemberId)

    MaterialTheme(
        colorScheme = MaterialTheme.colorScheme.copy(
            primary = PrimaryTeal,
            secondary = SecondaryTeal,
            surface = SurfaceLight,
            onSurface = OnSurfaceDark,
            primaryContainer = AccentAmber
        )
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Team Member Details",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = OnSurfaceDark
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = PrimaryTeal
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = SurfaceLight,
                        titleContentColor = OnSurfaceDark
                    )
                )
            },
            containerColor = SurfaceLight
        ) { paddingValues ->
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                color = SurfaceLight
            ) {
                when (state) {
                    is TeamMemberDetailViewModel.TeamMemberState.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = PrimaryTeal,
                                strokeWidth = 4.dp
                            )
                        }
                    }
                    is TeamMemberDetailViewModel.TeamMemberState.Error -> {
                        val errorState = state as TeamMemberDetailViewModel.TeamMemberState.Error
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Error",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                ErrorView(
                                    message = errorState.message,
                                    onRetry = { viewModel.loadTeamMemberDetails(teamMemberId) }
                                )
                            }
                        }
                    }
                    is TeamMemberDetailViewModel.TeamMemberState.Success -> {
                        val teamMember = (state as TeamMemberDetailViewModel.TeamMemberState.Success).teamMember
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp, vertical = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Profile Image or Placeholder
                            Surface(
                                modifier = Modifier.size(120.dp),
                                shape = CircleShape,
                                color = SecondaryTeal.copy(alpha = 0.1f),
                                tonalElevation = 6.dp
                            ) {
                                if (teamMember.profileImageUrl != null) {
                                    Image(
                                        painter = rememberAsyncImagePainter(teamMember.profileImageUrl),
                                        contentDescription = "${teamMember.name}'s Profile Image",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape)
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "Profile Icon",
                                        tint = SecondaryTeal,
                                        modifier = Modifier
                                            .padding(24.dp)
                                            .fillMaxSize()
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Team Member Info Card
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.White,
                                    contentColor = OnSurfaceDark
                                ),
                                elevation = CardDefaults.cardElevation(
                                    defaultElevation = 4.dp
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp)
                                ) {
                                    Text(
                                        text = teamMember.name,
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = OnSurfaceDark
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Email,
                                            contentDescription = "Email Icon",
                                            tint = SecondaryTeal,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = teamMember.email,
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = OnSurfaceDark.copy(alpha = 0.7f)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    teamMember.phoneNumber?.let {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Call,
                                                contentDescription = "Phone Icon",
                                                tint = SecondaryTeal,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Phone: $it",
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = OnSurfaceDark.copy(alpha = 0.7f)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }
                                    teamMember.location?.let {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.LocationOn,
                                                contentDescription = "Location Icon",
                                                tint = SecondaryTeal,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Location: $it",
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = OnSurfaceDark.copy(alpha = 0.7f)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = "Role Icon",
                                            tint = AccentAmber,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Role: ${teamMember.role}",
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Medium,
                                            color = PrimaryTeal
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // Statistics Section
                            Text(
                                text = "Statistics",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = OnSurfaceDark
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.White,
                                    contentColor = OnSurfaceDark
                                ),
                                elevation = CardDefaults.cardElevation(
                                    defaultElevation = 4.dp
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Text(
                                        text = "Stats will be displayed here",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = OnSurfaceDark.copy(alpha = 0.7f)
                                    )
                                    // Add placeholder for stats (e.g., tasks completed, projects, etc.)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = "Stats Icon",
                                            tint = AccentAmber,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Tasks Completed: N/A",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = OnSurfaceDark
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}