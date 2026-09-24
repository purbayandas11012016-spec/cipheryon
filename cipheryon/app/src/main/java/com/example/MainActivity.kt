package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.cipher.CipherMode
import com.example.cipher.CipherViewModel
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: CipherViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                CipherScreen(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CipherScreen(viewModel: CipherViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val clipboardManager = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var dropdownExpanded by remember { mutableStateOf(false) }

    val isEncryptorMode = uiState.selectedMode == CipherMode.ENCRYPTOR

    LaunchedEffect(uiState.statusMessage) {
        uiState.statusMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearStatus()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Cipheryon",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Programmer's Cipher & Encryption Utility",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.clearAll() },
                        modifier = Modifier.testTag("clear_all_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Clear all text"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Text input box labeled "Input Text / Code Data" at the top
            OutlinedTextField(
                value = uiState.inputText,
                onValueChange = { viewModel.onInputChanged(it) },
                label = { Text("Input Text / Code Data") },
                placeholder = { Text("Enter plaintext to encrypt or encoded segments to decrypt...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_text_field"),
                minLines = 3,
                maxLines = 6,
                trailingIcon = {
                    if (uiState.inputText.isNotEmpty()) {
                        IconButton(
                            onClick = { viewModel.onInputChanged("") },
                            modifier = Modifier.testTag("clear_input_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear input text"
                            )
                        }
                    }
                },
                supportingText = {
                    Text(
                        text = "${uiState.inputText.length} characters",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            )

            // 2. Dropdown menu configuration to select operational Mode. Default: "Hexadecimal"
            ExposedDropdownMenuBox(
                expanded = dropdownExpanded,
                onExpandedChange = { dropdownExpanded = !dropdownExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = uiState.selectedMode.displayName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Operational Mode") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded)
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                        .testTag("mode_dropdown"),
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                )

                ExposedDropdownMenu(
                    expanded = dropdownExpanded,
                    onDismissRequest = { dropdownExpanded = false },
                    modifier = Modifier.testTag("mode_dropdown_menu")
                ) {
                    CipherMode.entries.forEach { mode ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(
                                        text = mode.displayName,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = if (mode == uiState.selectedMode) FontWeight.Bold else FontWeight.Normal
                                    )
                                    val desc = when (mode) {
                                        CipherMode.HEXADECIMAL -> "Uppercase base-16 segments with shift layer"
                                        CipherMode.BINARY -> "8-bit base-2 segments with shift layer"
                                        CipherMode.OCTAL -> "Base-8 segments with shift layer"
                                        CipherMode.ENCRYPTOR -> "CSRPNG perma-lock randomizer (chars, symbols, emojis, numbers)"
                                        CipherMode.RANDOM_SCRAMBLE -> "High-entropy 62-char seed shredder"
                                    }
                                    Text(
                                        text = desc,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            onClick = {
                                viewModel.onModeChanged(mode)
                                dropdownExpanded = false
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                        )
                    }
                }
            }

            // Quick Preset Samples
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Samples:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                listOf("Cipheryon", "Hello World", "Cyber2026", "Secure CSRPNG").forEach { sample ->
                    SuggestionChip(
                        onClick = { viewModel.loadSample(sample) },
                        label = { Text(sample) }
                    )
                }
            }

            // 3. Two equal-width buttons in a horizontal Row labeled "Encrypt" and "Decrypt"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { viewModel.encrypt() },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("encrypt_button"),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Encrypt",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Button(
                    onClick = { viewModel.decrypt() },
                    enabled = !isEncryptorMode,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("decrypt_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LockOpen,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Decrypt",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Red warning text when Encryptor mode is selected
            if (isEncryptorMode) {
                Text(
                    text = "Encryptor permanently encrypts the input, and decryption is impossible as it uses a CSRPNG algorithm to perma-lock the output to something completely untraceable to the real input.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp)
                        .testTag("encryptor_warning_text")
                )
            }

            // 4. Elevated Card component container containing a SelectionContainer with monospace typography
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("output_card"),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Output Card Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Processed Output",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Surface(
                                shape = MaterialTheme.shapes.small,
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = uiState.selectedMode.displayName,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }

                        Row {
                            if (uiState.outputText.isNotEmpty() && !uiState.isError && !isEncryptorMode) {
                                IconButton(
                                    onClick = { viewModel.swapInputOutput() },
                                    modifier = Modifier.testTag("swap_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SwapVert,
                                        contentDescription = "Move output to input"
                                    )
                                }
                            }
                            if (uiState.outputText.isNotEmpty()) {
                                IconButton(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(uiState.outputText))
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("Copied output to clipboard!")
                                        }
                                    },
                                    modifier = Modifier.testTag("copy_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copy output to clipboard"
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 10.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )

                    // SelectionContainer with Monospace output
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        SelectionContainer(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("output_selection_container")
                        ) {
                            if (uiState.outputText.isEmpty()) {
                                Text(
                                    text = "Ready. Tap 'Encrypt' or 'Decrypt' to process data.",
                                    fontFamily = FontFamily.Monospace,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    modifier = Modifier.testTag("output_text")
                                )
                            } else {
                                Text(
                                    text = uiState.outputText,
                                    fontFamily = FontFamily.Monospace,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (uiState.isError) {
                                        MaterialTheme.colorScheme.error
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    },
                                    fontWeight = if (uiState.isError) FontWeight.SemiBold else FontWeight.Normal,
                                    modifier = Modifier.testTag("output_text")
                                )
                            }
                        }
                    }

                    if (uiState.outputText.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Length: ${uiState.outputText.length} chars",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Double-tap or long-press to copy",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
