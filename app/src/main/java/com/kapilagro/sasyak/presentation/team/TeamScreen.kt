package com.kapilagro.sasyak.presentation.team

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
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

// Custom Theme Colors
val PrimaryTeal = Color(0xFF00695C)
val SecondaryTeal = Color(0xFF26A69A)
val SurfaceLight = Color(0xFFF5F7FA)
val OnSurfaceDark = Color(0xFF212121)
val AccentAmber = Color(0xFFFFCA28)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamScreen(
    viewModel: TeamViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
    onTeamMemberClick: (Int) -> Unit
) {
    val state by viewModel.teamState.collectAsState()

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
                            text = "Our Team",
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
                    is TeamViewModel.TeamState.Loading -> {
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
                    is TeamViewModel.TeamState.Error -> {
                        val errorState = state as TeamViewModel.TeamState.Error
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Error",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = errorState.message,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = { viewModel.loadTeamMembers() },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = SecondaryTeal
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        "Retry",
                                        color = Color.White,
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                }
                            }
                        }
                    }
                    is TeamViewModel.TeamState.Success -> {
                        val teamMembers = (state as TeamViewModel.TeamState.Success).teamMembers
                        if (teamMembers.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "No Team Members",
                                        tint = SecondaryTeal,
                                        modifier = Modifier
                                            .size(72.dp)
                                            .padding(bottom = 16.dp)
                                    )
                                    Text(
                                        text = "No team members found",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = OnSurfaceDark.copy(alpha = 0.7f)
                                    )
                                }
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
}

@Composable
fun TeamMembersList(
    teamMembers: List<TeamMember>,
    onTeamMemberClick: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
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
    teamMember: TeamMember,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = Color.White,
            contentColor = OnSurfaceDark
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp,
            pressedElevation = 8.dp
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(64.dp),
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
                            .padding(14.dp)
                            .fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = teamMember.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = OnSurfaceDark
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = "Email Icon",
                        tint = SecondaryTeal,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = teamMember.email,
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceDark.copy(alpha = 0.7f)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Role: ${teamMember.role}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = PrimaryTeal
                )
            }
        }
    }
}