package com.raptrx.still.ui.focus

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
import com.raptrx.still.data.FocusRepository
import com.raptrx.still.data.LimitStatus
import com.raptrx.still.ui.theme.Dimens
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FocusScreenState(
    val limits: List<LimitStatus> = emptyList(),
    val scheduleLabels: List<String> = emptyList(),
    val activeScheduleLabel: String? = null
)

@HiltViewModel
class FocusViewModel @Inject constructor(
    private val focusRepository: FocusRepository
) : ViewModel() {
    private val dismissals = MutableStateFlow<Set<String>>(emptySet())

    val state: StateFlow<FocusScreenState> = combine(
        focusRepository.observeLimitStatuses(),
        focusRepository.schedules,
        focusRepository.observeActiveScheduleLabel()
    ) { limits, schedules, activeLabel ->
        FocusScreenState(
            limits = limits,
            scheduleLabels = schedules.map { it.label },
            activeScheduleLabel = activeLabel
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = FocusScreenState()
    )

    /** Removes a limit entirely, so the package becomes unrestricted again. */
    fun clearLimit(packageName: String) {
        viewModelScope.launch { focusRepository.clearLimit(packageName) }
    }

    fun setLimit(packageName: String, minutes: Int) {
        viewModelScope.launch { focusRepository.setLimit(packageName, minutes) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusScreen(
    onBackClick: () -> Unit,
    onAddLimitClick: () -> Unit,
    viewModel: FocusViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var pendingRemoval by remember { mutableStateOf<LimitStatus?>(null) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Focus") },
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (state.activeScheduleLabel != null) {
                item {
                    Text(
                        text = "Focus mode is on: ${state.activeScheduleLabel}",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(
                            horizontal = Dimens.APP_HORIZONTAL_SPACING,
                            vertical = 16.dp
                        )
                    )
                }
            }

            item {
                Text(
                    text = "Daily limits",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(
                        horizontal = Dimens.APP_HORIZONTAL_SPACING,
                        vertical = 12.dp
                    )
                )
            }

            if (state.limits.isEmpty()) {
                item {
                    Text(
                        text = "No limits yet. Add one to cap how long an app can be used each day.",
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(
                            horizontal = Dimens.APP_HORIZONTAL_SPACING,
                            vertical = 8.dp
                        )
                    )
                }
            }

            items(items = state.limits, key = { it.packageName }) { status ->
                LimitRow(
                    status = status,
                    onClick = { pendingRemoval = status }
                )
            }

            item {
                Text(
                    text = "Add a limit",
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onAddLimitClick)
                        .padding(
                            horizontal = Dimens.APP_HORIZONTAL_SPACING,
                            vertical = 16.dp
                        )
                )
            }
        }
    }

    pendingRemoval?.let { status ->
        AlertDialog(
            onDismissRequest = { pendingRemoval = null },
            text = { Text("Remove the limit for this app?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearLimit(status.packageName)
                        pendingRemoval = null
                    }
                ) { Text("Remove") }
            },
            dismissButton = {
                TextButton(onClick = { pendingRemoval = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun LimitRow(status: LimitStatus, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = Dimens.APP_HORIZONTAL_SPACING, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = status.packageName,
                fontSize = 17.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = if (status.isExhausted) "Used up" else "${status.remainingMinutes} min left",
                fontSize = 14.sp,
                color = if (status.isExhausted) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        LinearProgressIndicator(
            progress = { status.usedFraction },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "${status.usedMinutes} of ${status.limitMinutes} min used today",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
