package com.raptrx.still.ui.backup

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.raptrx.still.R
import com.raptrx.still.data.BackupManager
import com.raptrx.still.ui.theme.Dimens
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BackupViewModel @Inject constructor(
    private val backupManager: BackupManager
) : ViewModel() {
    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    fun export(uri: android.net.Uri) {
        viewModelScope.launch {
            _message.value = backupManager.exportTo(uri).fold(
                onSuccess = { "Backup written." },
                onFailure = { "Could not write backup: ${it.message}" }
            )
        }
    }

    fun import(uri: android.net.Uri) {
        viewModelScope.launch {
            _message.value = backupManager.importFrom(uri).fold(
                onSuccess = { count -> "Restored $count items." },
                onFailure = { "Could not restore: ${it.message}" }
            )
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupScreen(
    onBackClick: () -> Unit,
    viewModel: BackupViewModel = hiltViewModel()
) {
    val message by viewModel.message.collectAsStateWithLifecycle()
    var pendingImport by remember { mutableStateOf<android.net.Uri?>(null) }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri -> uri?.let(viewModel::export) }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri -> if (uri != null) pendingImport = uri }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Backup & Restore") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back),
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Text(
                text = "Everything you have set up — favourites, folders, hidden apps, focus limits and preferences — is stored in one file.",
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(
                    horizontal = Dimens.APP_HORIZONTAL_SPACING,
                    vertical = 16.dp
                )
            )

            ActionItem(
                name = "Export backup",
                onClick = { exportLauncher.launch("still-backup.json") }
            )
            ActionItem(
                name = "Restore from backup",
                onClick = { importLauncher.launch(arrayOf("application/json")) }
            )

            message?.let { text ->
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = text,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.clearMessage() }
                        .padding(
                            horizontal = Dimens.APP_HORIZONTAL_SPACING,
                            vertical = 12.dp
                        )
                )
            }
        }
    }

    pendingImport?.let { uri ->
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { pendingImport = null },
            text = { Text("Restoring merges this file into your current setup. Continue?") },
            confirmButton = {
                androidx.compose.material3.TextButton(
                    onClick = {
                        viewModel.import(uri)
                        pendingImport = null
                    }
                ) { Text("Restore") }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(
                    onClick = { pendingImport = null }
                ) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun ActionItem(name: String, onClick: () -> Unit) {
    Text(
        text = name,
        color = MaterialTheme.colorScheme.onSurface,
        fontSize = 18.sp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = Dimens.APP_HORIZONTAL_SPACING, vertical = 16.dp)
    )
}
