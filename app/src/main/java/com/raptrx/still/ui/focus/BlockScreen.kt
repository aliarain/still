package com.raptrx.still.ui.focus

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.raptrx.still.data.FocusRepository
import com.raptrx.still.utils.BlockReason
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/** What the block screen should display for the app it intercepted. */
data class BlockScreenState(
    val appLabel: String = "",
    val reason: BlockReason = BlockReason.None,
    val isLoading: Boolean = true
)

@HiltViewModel
class BlockScreenViewModel @Inject constructor(
    private val focusRepository: FocusRepository
) : ViewModel() {
    private val _state = MutableStateFlow(BlockScreenState())
    val state: StateFlow<BlockScreenState> = _state

    private var loadedPackage: String? = null

    fun load(packageName: String, appLabel: String) {
        // Guard against recomposition re-reading usage on every frame.
        if (loadedPackage == packageName) return
        loadedPackage = packageName
        viewModelScope.launch {
            _state.value = BlockScreenState(
                appLabel = appLabel,
                reason = focusRepository.blockReason(packageName),
                isLoading = false
            )
        }
    }
}

/**
 * Shown instead of the requested app once its budget is spent or a focus
 * window is open. Deliberately calm: it states the situation and offers no
 * one-tap bypass, so the limit is not a reflexive tap away.
 */
@Composable
fun BlockScreen(
    packageName: String,
    appLabel: String,
    onDismiss: () -> Unit,
    viewModel: BlockScreenViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(packageName) {
        viewModel.load(packageName, appLabel)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Time's up",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = buildMessage(state),
            fontSize = 17.sp,
            lineHeight = 26.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Go back")
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Close")
        }
    }
}

private fun buildMessage(state: BlockScreenState): String {
    val name = state.appLabel.ifBlank { "This app" }
    return when (val reason = state.reason) {
        is BlockReason.Schedule ->
            "$name is paused during ${reason.scheduleLabel}. It opens again when that window ends."

        is BlockReason.DailyLimit ->
            "$name has used its ${formatMinutes(reason.limitMinutes.toLong())} for today."

        BlockReason.None ->
            "$name is available again."
    }
}

internal fun formatMinutes(minutes: Long): String {
    if (minutes <= 0) return "0 min"
    val hours = minutes / 60
    val remainder = minutes % 60
    return when {
        hours == 0L -> "$remainder min"
        remainder == 0L -> "$hours h"
        else -> "$hours h $remainder min"
    }
}
