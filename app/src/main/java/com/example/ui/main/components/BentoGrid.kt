package com.example.ui.main.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.domain.model.Folder
import com.example.domain.model.Note

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BentoGrid(
    folders: List<Folder>,
    notes: List<Note>,
    onFolderClick: (Folder) -> Unit,
    onNoteClick: (Note) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(2),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalItemSpacing = 16.dp
    ) {
        items(
            items = folders,
            key = { "folder_${it.id}" }
        ) { folder ->
            FolderCard(
                folder = folder,
                previewNote = null, // Can map to actual preview note if available
                onClick = { onFolderClick(folder) },
                modifier = Modifier.animateItem()
            )
        }

        items(
            items = notes,
            key = { "note_${it.id}" }
        ) { note ->
            NoteCard(
                note = note,
                onClick = { onNoteClick(note) },
                modifier = Modifier.animateItem()
            )
        }
    }
}
