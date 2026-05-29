package com.example.ui.main

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.domain.model.Folder
import com.example.domain.model.Note
import com.example.security.BiometricHelper
import com.example.ui.main.components.BentoGrid
import com.example.ui.main.components.BreadcrumbBar
import java.util.UUID
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onNavigateToEditor: (UUID?) -> Unit,
    viewModel: MainViewModel = viewModel(factory = MainViewModel.Factory)
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current as FragmentActivity
    var showAddDialog by remember { mutableStateOf(false) }

    val gradientBrush = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.background,
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
            MaterialTheme.colorScheme.background
        ),
        start = Offset(0f, 0f),
        end = Offset(1000f, 1000f)
    )

    Scaffold(
        modifier = Modifier.background(gradientBrush),
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("NoteMax") },
                actions = {
                    IconButton(onClick = { /* TODO: Search */ }) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = androidx.compose.ui.graphics.Color.Transparent
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            BreadcrumbBar(
                breadcrumbs = state.breadcrumbs,
                onCrumbClick = { folderId -> viewModel.navigateToFolder(folderId) }
            )
            
            AnimatedContent(
                targetState = state.currentFolderId,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "folder_transition"
            ) { _ ->
                BentoGrid(
                    folders = state.folders,
                    notes = state.notes,
                    onFolderClick = { folder ->
                        // No folder lock implemented in data currently, but ready for it.
                        viewModel.navigateToFolder(folder.id)
                    },
                    onNoteClick = { note ->
                        if (note.isLocked) {
                            BiometricHelper.showBiometricPrompt(
                                activity = context,
                                onSuccess = { onNavigateToEditor(note.id) },
                                onError = { /* handle error */ }
                            )
                        } else {
                            onNavigateToEditor(note.id)
                        }
                    }
                )
            }
        }

        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text("Create New") },
                text = {
                    Column {
                        TextButton(onClick = { 
                            showAddDialog = false
                            // Simple folder creation for now
                            viewModel.createFolder("New Folder") 
                        }) {
                            Icon(Icons.Default.CreateNewFolder, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Folder")
                        }
                        TextButton(onClick = { 
                            showAddDialog = false
                            onNavigateToEditor(null) 
                        }) {
                            Icon(Icons.Default.NoteAdd, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Note")
                        }
                    }
                },
                confirmButton = {}
            )
        }
    }
}
