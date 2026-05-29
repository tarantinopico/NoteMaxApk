package com.example.ui.main

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.domain.model.Folder
import com.example.domain.model.Note
import com.example.ui.components.FolderRow
import com.example.ui.components.NoteCard
import com.example.ui.components.NoteSearchBar
import com.example.ui.components.ShimmerNoteCard
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class, ExperimentalAnimationApi::class)
@Composable
fun MainScreen(
    onNavigateToEditor: (UUID?) -> Unit,
    viewModel: MainViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is MainScreenEffect.ShowSnackbar -> {
                    val result = snackbarHostState.showSnackbar(
                        message = effect.message,
                        actionLabel = effect.actionLabel
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        effect.action?.invoke()
                    }
                }
                is MainScreenEffect.NavigateToEditor -> {
                    onNavigateToEditor(effect.noteId)
                }
                is MainScreenEffect.RequestBiometric -> {}
                is MainScreenEffect.ShareNote -> {}
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            if (state.isSearchActive) {
                Box(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
                    NoteSearchBar(
                        query = state.searchQuery,
                        onQueryChange = { viewModel.processEvent(MainScreenEvent.SearchQueryChanged(it)) },
                        onCloseSearch = { viewModel.processEvent(MainScreenEvent.ToggleSearch) },
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                MediumTopAppBar(
                    title = {
                        val title = state.currentPath.lastOrNull()?.name ?: "NoteMax"
                        Text(title)
                    },
                    navigationIcon = {
                        if (state.currentPath.isNotEmpty()) {
                            IconButton(onClick = { viewModel.processEvent(MainScreenEvent.NavigateUp) }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                            }
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.processEvent(MainScreenEvent.ToggleSearch) }) {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        }
                        IconButton(onClick = { /* Profile */ }) {
                            Icon(Icons.Default.Person, contentDescription = "Profile")
                        }
                    },
                    colors = TopAppBarDefaults.mediumTopAppBarColors(
                        containerColor = Color.Transparent,
                        scrolledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                    )
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigateToEditor(null) },
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Note")
            }
        }
    ) { padding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(padding)) {

            AnimatedContent(
                targetState = state.isSearchActive,
                label = "MainContentAnimation"
            ) { isSearch ->
                if (isSearch) {
                    SearchResultsGrid(
                        notes = state.searchResultsNotes,
                        folders = state.searchResultsFolders,
                        selectedIds = state.selectedIds,
                        onNoteClick = { onNavigateToEditor(it.id) },
                        onNoteLongClick = { viewModel.processEvent(MainScreenEvent.ToggleNoteSelection(it.id)) },
                        onSwipeToDelete = { viewModel.processEvent(MainScreenEvent.DeleteNote(it)) },
                        onFolderClick = { viewModel.processEvent(MainScreenEvent.NavigateIntoFolder(it)) }
                    )
                } else {
                    Column(modifier = Modifier.fillMaxSize()) {
                        if (state.folders.isNotEmpty()) {
                            FolderRow(
                                folders = state.folders,
                                onFolderClick = { viewModel.processEvent(MainScreenEvent.NavigateIntoFolder(it)) },
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        
                        if (state.isLoading) {
                            GridLoadingState()
                        } else {
                            NoteGrid(
                                notes = state.notes,
                                selectedIds = state.selectedIds,
                                onNoteClick = { onNavigateToEditor(it.id) },
                                onNoteLongClick = {
                                    if (!state.isSelectionMode) {
                                        viewModel.processEvent(MainScreenEvent.ToggleSelectionMode(true))
                                    }
                                    viewModel.processEvent(MainScreenEvent.ToggleNoteSelection(it.id))
                                },
                                onSwipeToDelete = { viewModel.processEvent(MainScreenEvent.DeleteNote(it)) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NoteGrid(
    notes: List<Note>,
    selectedIds: Set<UUID>,
    onNoteClick: (Note) -> Unit,
    onNoteLongClick: (Note) -> Unit,
    onSwipeToDelete: (Note) -> Unit
) {
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Adaptive(150.dp),
        contentPadding = PaddingValues(16.dp),
        verticalItemSpacing = 16.dp,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(notes, key = { it.id }) { note ->
            NoteCard(
                note = note,
                isSelected = selectedIds.contains(note.id),
                onClick = { onNoteClick(note) },
                onLongClick = { onNoteLongClick(note) },
                onSwipeToDelete = { onSwipeToDelete(note) },
                modifier = Modifier.animateItemPlacement()
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SearchResultsGrid(
    notes: List<Note>,
    folders: List<Folder>,
    selectedIds: Set<UUID>,
    onNoteClick: (Note) -> Unit,
    onNoteLongClick: (Note) -> Unit,
    onSwipeToDelete: (Note) -> Unit,
    onFolderClick: (Folder) -> Unit
) {
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Adaptive(150.dp),
        contentPadding = PaddingValues(16.dp),
        verticalItemSpacing = 16.dp,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(folders, key = { it.id }) { folder ->
            Box(modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.shapes.medium)
                .padding(16.dp)
            ) {
                 Text(folder.name, style = MaterialTheme.typography.titleMedium)
            }
        }
        items(notes, key = { it.id }) { note ->
            NoteCard(
                note = note,
                isSelected = selectedIds.contains(note.id),
                onClick = { onNoteClick(note) },
                onLongClick = { onNoteLongClick(note) },
                onSwipeToDelete = { onSwipeToDelete(note) },
                modifier = Modifier.animateItemPlacement()
            )
        }
    }
}

@Composable
fun GridLoadingState() {
    Column(modifier = Modifier.padding(16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            ShimmerNoteCard(modifier = Modifier.weight(1f).height(150.dp))
            ShimmerNoteCard(modifier = Modifier.weight(1f).height(200.dp))
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            ShimmerNoteCard(modifier = Modifier.weight(1f).height(180.dp))
            ShimmerNoteCard(modifier = Modifier.weight(1f).height(120.dp))
        }
    }
}
