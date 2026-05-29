package com.example.ui.editor

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Preview
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.security.BiometricHelper
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    noteId: UUID?,
    onNavigateBack: () -> Unit,
    onNavigateToNote: (UUID) -> Unit,
    viewModel: EditorViewModel = viewModel(factory = EditorViewModel.Factory)
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current as FragmentActivity

    LaunchedEffect(noteId) {
        viewModel.loadNote(noteId)
    }

    // Photo picker
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            uri?.let {
                val file = copyUriToInternalStorage(context, it)
                if (file != null) {
                    viewModel.insertImageIntoContent("![Image](file://${file.absolutePath})")
                }
            }
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }) {
                        Icon(Icons.Default.Image, contentDescription = "Insert Image")
                    }
                    IconButton(onClick = viewModel::toggleLock) {
                        Icon(if (state.isLocked) Icons.Default.Lock else Icons.Default.LockOpen, contentDescription = "Lock")
                    }
                    IconButton(onClick = viewModel::togglePreview) {
                        Icon(if (state.isPreviewMode) Icons.Default.Edit else Icons.Default.Preview, contentDescription = "Toggle Preview")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Title Input
            BasicTextField(
                value = state.title,
                onValueChange = viewModel::updateTitle,
                textStyle = MaterialTheme.typography.titleLarge.copy(color = MaterialTheme.colorScheme.onBackground),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                decorationBox = { innerTextField ->
                    if (state.title.isEmpty()) {
                        Text("Note Title", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                    }
                    innerTextField()
                }
            )

            // Tags
            var currentTag by remember { mutableStateOf("") }
            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(state.tags) { tag ->
                    Box(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(tag, color = MaterialTheme.colorScheme.onPrimaryContainer, style = MaterialTheme.typography.labelSmall)
                    }
                }
                item {
                    BasicTextField(
                        value = currentTag,
                        onValueChange = { 
                            if (it.endsWith(",")) {
                                val tag = it.dropLast(1).trim()
                                if (tag.isNotEmpty()) viewModel.addTag(tag)
                                currentTag = ""
                            } else {
                                currentTag = it
                            }
                        },
                        textStyle = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onBackground),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            if (currentTag.isNotBlank()) viewModel.addTag(currentTag.trim())
                            currentTag = ""
                        }),
                        decorationBox = { innerTextField ->
                            if (currentTag.isEmpty()) {
                                Text("Add tag...", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                            }
                            innerTextField()
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Divider()

            AnimatedContent(
                targetState = state.isPreviewMode,
                modifier = Modifier.weight(1f).fillMaxWidth()
            ) { isPreview ->
                if (isPreview) {
                    MarkdownRenderer(
                        content = state.content,
                        onWikiLinkClicked = { /* handle wiki link navigation */ },
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    )
                } else {
                    BasicTextField(
                        value = state.content,
                        onValueChange = viewModel::updateContent,
                        textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onBackground),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        decorationBox = { innerTextField ->
                            if (state.content.isEmpty()) {
                                Text("Start typing...", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                            }
                            innerTextField()
                        }
                    )
                }
            }
        }
    }
}

fun copyUriToInternalStorage(context: android.content.Context, uri: Uri): File? {
    return try {
        val imagesDir = File(context.filesDir, "images").apply { mkdirs() }
        val file = File(imagesDir, "${UUID.randomUUID()}.jpg")
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(file).use { output ->
                input.copyTo(output)
            }
        }
        file
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
