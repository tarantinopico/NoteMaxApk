package com.example.ui.editor

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.glassSurface
import com.example.util.MarkdownParser
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun EditorScreen(
    noteId: UUID?,
    onNavigateBack: () -> Unit,
    onNavigateToNote: (UUID) -> Unit
) {
    val viewModel: EditorViewModel = viewModel(factory = EditorViewModel.provideFactory(noteId))
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    var isPreviewMode by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        viewModel.processEvent(EditorEvent.ImageSelected(uri?.toString()))
    }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is EditorEffect.RequestBiometric -> {
                    val activity = context as? FragmentActivity
                    if (activity != null) {
                        val executor = ContextCompat.getMainExecutor(activity)
                        val biometricPrompt = BiometricPrompt(activity, executor,
                            object : BiometricPrompt.AuthenticationCallback() {
                                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                                    viewModel.processEvent(EditorEvent.BiometricAuthenticated(false))
                                }
                                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                                    viewModel.processEvent(EditorEvent.BiometricAuthenticated(true))
                                }
                                override fun onAuthenticationFailed() {
                                    // Let it stay open
                                }
                            })

                        val promptInfo = BiometricPrompt.PromptInfo.Builder()
                            .setTitle("Unlock Note")
                            .setSubtitle("Use your biometric credential to unlock this note")
                            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
                            .build()

                        biometricPrompt.authenticate(promptInfo)
                    } else {
                        viewModel.processEvent(EditorEvent.BiometricAuthenticated(false))
                    }
                }
                is EditorEffect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is EditorEffect.NavigateBack -> onNavigateBack()
                is EditorEffect.NavigateToNote -> onNavigateToNote(effect.noteId)
            }
        }
    }

    BackHandler {
        viewModel.processEvent(EditorEvent.RequestSave)
        onNavigateBack()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = { 
                        viewModel.processEvent(EditorEvent.RequestSave)
                        onNavigateBack() 
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { imagePickerLauncher.launch("image/*") }) {
                        Icon(Icons.Default.Image, contentDescription = "Attach Image")
                    }
                    IconButton(onClick = { viewModel.processEvent(EditorEvent.ToggleLock) }) {
                        Icon(
                            if (state.isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                            contentDescription = "Toggle Lock"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        if (state.isLocked && !state.isUnlockedForSession) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Security, contentDescription = "Locked", modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(16.dp))
                    Text("This note is locked.", style = MaterialTheme.typography.titleMedium)
                }
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            BasicTextField(
                value = state.title,
                onValueChange = { viewModel.processEvent(EditorEvent.TitleChanged(it)) },
                textStyle = TextStyle(
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                decorationBox = { innerTextField ->
                    if (state.title.isEmpty()) {
                        Text("Title", style = TextStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant))
                    }
                    innerTextField()
                },
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
            )

            AnimatedContent(targetState = isPreviewMode, label = "EditorMode") { preview ->
                if (preview) {
                    Box(modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())) {
                        Text(
                            text = MarkdownParser.parse(state.content, MaterialTheme.colorScheme.primary),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else {
                    Box(modifier = Modifier.fillMaxSize()) {
                        BasicTextField(
                            value = state.content,
                            onValueChange = { 
                                viewModel.processEvent(EditorEvent.ContentChanged(it))
                                // Trigger wiki suggestions if ends with [[
                                val cursorIndex = it.length
                                val lastBracket = it.lastIndexOf("[[")
                                if (lastBracket != -1 && lastBracket >= it.length - 20) {
                                    val query = it.substring(lastBracket + 2)
                                    if (!query.contains("]]")) {
                                        viewModel.processEvent(EditorEvent.SuggestWikiLinks(query))
                                    } else {
                                        viewModel.processEvent(EditorEvent.SuggestWikiLinks(""))
                                    }
                                } else {
                                    viewModel.processEvent(EditorEvent.SuggestWikiLinks(""))
                                }
                            },
                            textStyle = TextStyle(
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 24.sp
                            ),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState()),
                            decorationBox = { innerTextField ->
                                if (state.content.isEmpty()) {
                                    Text("Start typing...", style = TextStyle(fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant))
                                }
                                innerTextField()
                            }
                        )

                        if (state.wikiSuggestions.isNotEmpty()) {
                            LazyColumn(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .fillMaxWidth()
                                    .glassSurface()
                                    .height(200.dp)
                            ) {
                                items(state.wikiSuggestions, key = { it.id }) { sugg ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                // Replace the partial [[query with [[title]]
                                                val lastBracket = state.content.lastIndexOf("[[")
                                                if (lastBracket != -1) {
                                                    val newContent = state.content.substring(0, lastBracket) + "[[${sugg.title}]] "
                                                    viewModel.processEvent(EditorEvent.ContentChanged(newContent))
                                                    viewModel.processEvent(EditorEvent.SuggestWikiLinks(""))
                                                }
                                            }
                                            .padding(16.dp)
                                    ) {
                                        Text(sugg.title)
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
