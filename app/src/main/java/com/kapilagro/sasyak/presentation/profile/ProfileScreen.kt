package com.kapilagro.sasyak.presentation.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kapilagro.sasyak.R
import com.kapilagro.sasyak.domain.models.User
import com.kapilagro.sasyak.presentation.common.theme.Green500
import coil.compose.rememberAsyncImagePainter


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onEditProfileClick: () -> Unit,
    onLogoutClick: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val profileState by viewModel.profileState.collectAsState()
    val managerState by viewModel.managerState.collectAsState()
    val userRole by viewModel.userRole.collectAsState()

// Add a key to force recomposition
    val recomposeKey = remember { mutableStateOf(0) }

// Force a refresh when profile updated
    LaunchedEffect( Unit) {
        viewModel.loadUserProfile()
    }


    // Show logout confirmation dialog
    var showLogoutDialog by remember { mutableStateOf(false) }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Logout") },
            text = { Text("Are you sure you want to logout?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        onLogoutClick()
                    }
                ) {
                    Text("Logout")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showLogoutDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
    // Then wrap the main content in this key
    key(recomposeKey.value) {

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Profile") }
            )
        }
    ) { paddingValues ->
        when (profileState) {
            is ProfileViewModel.ProfileState.Success -> {
                val user = (profileState as ProfileViewModel.ProfileState.Success).user

                // Different profile content based on role
                when (userRole) {
                    "MANAGER" -> {
                        ManagerProfileContent(
                            user = user,
                            onEditProfileClick = onEditProfileClick,
                            onLogoutClick = { showLogoutDialog = true },
                            modifier = Modifier.padding(paddingValues)
                        )
                    }

                    "SUPERVISOR" -> {
                        SupervisorProfileContent(
                            user = user,
                            manager = if (managerState is ProfileViewModel.ManagerState.Success) {
                                (managerState as ProfileViewModel.ManagerState.Success).manager
                            } else null,
                            onEditProfileClick = onEditProfileClick,
                            onLogoutClick = { showLogoutDialog = true },
                            modifier = Modifier.padding(paddingValues)
                        )
                    }

                    "ADMIN" -> {
                        AdminProfileContent(
                            user = user,
                            onEditProfileClick = onEditProfileClick,
                            onLogoutClick = { showLogoutDialog = true },
                            modifier = Modifier.padding(paddingValues)
                        )
                    }

                    else -> {
                        DefaultProfileContent(
                            user = user,
                            onEditProfileClick = onEditProfileClick,
                            onLogoutClick = { showLogoutDialog = true },
                            modifier = Modifier.padding(paddingValues)
                        )
                    }
                }
            }

            is ProfileViewModel.ProfileState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is ProfileViewModel.ProfileState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Failed to load profile",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { viewModel.loadUserProfile() }) {
                            Text("Retry")
                        }
                    }
                }
            }
        }
    }
}
}

@Composable
fun ManagerProfileContent(
    user: User,
    onEditProfileClick: () -> Unit,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Profile header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            // Profile photo
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box {
                    // Avatar
                    Image(
                        painter = rememberAsyncImagePainter(user.profileImageUrl?.ifBlank { null }
                            ?: R.drawable.ic_person),
                        contentDescription = "Profile Photo",
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )

                    // Online status indicator
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(Green500)
                            .align(Alignment.BottomEnd)
                            .offset(x = (-4).dp, y = (-4).dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Name
                Text(
                    text = user.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Role - Manager specific
                Text(
                    text = "Field Manager",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Specialization tags - Manager specific
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    SpecializationChip(label = "Team Management")
                    Spacer(modifier = Modifier.width(8.dp))
                    SpecializationChip(label = "Crop Planning")
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Edit profile button
                OutlinedButton(
                    onClick = onEditProfileClick,
                    modifier = Modifier.width(200.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Edit Profile")
                }
            }
        }

        Divider(modifier = Modifier.padding(vertical = 16.dp))

        // Common profile details
        ProfileInfoSection(user = user)

        // Manager-specific section
        Divider(modifier = Modifier.padding(vertical = 16.dp))

        Text(
            text = "Team Management",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        // Team management shortcuts
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ManagerActionItem(
                icon = Icons.Outlined.Groups,
                label = "My Team",
                onClick = { /* Navigate to team management */ }
            )

            ManagerActionItem(
                icon = Icons.Outlined.Person,
                label = "Supervisors",
                onClick = { /* Navigate to supervisors list */ }
            )

            ManagerActionItem(
                icon = Icons.Outlined.Assignment,
                label = "Tasks",
                onClick = { /* Navigate to created tasks */ }
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Logout button
        LogoutButton(onLogoutClick = onLogoutClick)
    }
}

@Composable
fun SupervisorProfileContent(
    user: User,
    manager: User?,
    onEditProfileClick: () -> Unit,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Profile header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            // Profile photo
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box {
                    // Avatar
                    Image(
                        painter = rememberAsyncImagePainter(user.profileImageUrl?.ifBlank { null }
                            ?: R.drawable.ic_person),
                        contentDescription = "Profile Photo",
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )

                    // Online status indicator
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(Green500)
                            .align(Alignment.BottomEnd)
                            .offset(x = (-4).dp, y = (-4).dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Name
                Text(
                    text = user.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Role - Supervisor specific
                Text(
                    text = "Field Supervisor",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Specialization tags - Supervisor specific
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    SpecializationChip(label = "Crops Expert")
                    Spacer(modifier = Modifier.width(8.dp))
                    SpecializationChip(label = "Field Operations")
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Edit profile button
                OutlinedButton(
                    onClick = onEditProfileClick,
                    modifier = Modifier.width(200.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Edit Profile")
                }
            }
        }

        Divider(modifier = Modifier.padding(vertical = 16.dp))

        // Common profile details
        ProfileInfoSection(user = user)

        // Show manager info for supervisors
        if (manager != null) {
            Divider(modifier = Modifier.padding(vertical = 16.dp))

            Text(
                text = "Reporting Manager",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Manager avatar
                Image(
                    painter = rememberAsyncImagePainter(user.profileImageUrl?.ifBlank { null } ?: R.drawable.ic_person),
                    contentDescription = "Manager Photo",
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = manager.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = "Field Manager",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Logout button
        LogoutButton(onLogoutClick = onLogoutClick)
    }
}

@Composable
fun AdminProfileContent(
    user: User,
    onEditProfileClick: () -> Unit,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Profile header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            // Profile photo
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box {
                    // Avatar
                    Image(
                        painter = rememberAsyncImagePainter(user.profileImageUrl?.ifBlank { null }
                            ?: R.drawable.ic_person),
                        contentDescription = "Profile Photo",
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )

                    // Online status indicator
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(Green500)
                            .align(Alignment.BottomEnd)
                            .offset(x = (-4).dp, y = (-4).dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Name
                Text(
                    text = user.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Role - Admin specific
                Text(
                    text = "Admin",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Specialization tags - Manager specific
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    SpecializationChip(label = "User Management")
                    Spacer(modifier = Modifier.width(8.dp))
                    SpecializationChip(label = "Data Auditing")
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Edit profile button
                OutlinedButton(
                    onClick = onEditProfileClick,
                    modifier = Modifier.width(200.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Edit Profile")
                }
            }
        }

        Divider(modifier = Modifier.padding(vertical = 16.dp))

        // Common profile details
        ProfileInfoSection(user = user)

        // Manager-specific section
        Divider(modifier = Modifier.padding(vertical = 16.dp))

        Text(
            text = "Team Management",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        // Team management shortcuts
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ManagerActionItem(
                icon = Icons.Outlined.Groups,
                label = "My Team",
                onClick = { /* Navigate to team management */ }
            )

            ManagerActionItem(
                icon = Icons.Outlined.Person,
                label = "Supervisors",
                onClick = { /* Navigate to supervisors list */ }
            )

            ManagerActionItem(
                icon = Icons.Outlined.Assignment,
                label = "Tasks",
                onClick = { /* Navigate to created tasks */ }
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Logout button
        LogoutButton(onLogoutClick = onLogoutClick)
    }
}

@Composable
fun DefaultProfileContent(
    user: User,
    onEditProfileClick: () -> Unit,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Profile header with basic info
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = rememberAsyncImagePainter(user.profileImageUrl?.ifBlank { null } ?: R.drawable.ic_person),
                    contentDescription = "Profile Photo",
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = user.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Staff",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = onEditProfileClick,
                    modifier = Modifier.width(200.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Edit Profile")
                }
            }
        }

        Divider(modifier = Modifier.padding(vertical = 16.dp))

        // Common profile details
        ProfileInfoSection(user = user)

        Spacer(modifier = Modifier.weight(1f))

        // Logout button
        LogoutButton(onLogoutClick = onLogoutClick)
    }
}

@Composable
fun ProfileInfoSection(user: User) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        ProfileInfoItem(
            icon = Icons.Outlined.Email,
            label = "Email",
            value = user.email
        )

        ProfileInfoItem(
            icon = Icons.Outlined.Phone,
            label = "Phone",
            value = user.phoneNumber
        )

        ProfileInfoItem(
            icon = Icons.Outlined.LocationOn,
            label = "Location",
            value = user.location ?: "Moinabad,Telangana" // Fallback
        )

//        ProfileInfoItem(
//            icon = Icons.Outlined.CalendarToday,
//            label = "Joined",
//            value = user.joinedDate ?: "April 2022" // Fallback
//        )
    }
}

@Composable
fun LogoutButton(onLogoutClick: () -> Unit) {
    Button(
        onClick = onLogoutClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.error
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.Logout,
            contentDescription = null
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text("Logout")
    }
}

@Composable
fun ManagerActionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(horizontal = 8.dp)
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(56.dp)
                .background(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = CircleShape
                )
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
fun SpecializationChip(label: String) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier.height(32.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 12.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
fun ProfileInfoItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 16.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}