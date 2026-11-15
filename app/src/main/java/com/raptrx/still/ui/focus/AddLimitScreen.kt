package com.raptrx.still.ui.focus

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.raptrx.still.data.AppInfoDao
import com.raptrx.still.data.FocusRepository
import com.raptrx.still.ui.entities.AppInfo
import com.raptrx.still.ui.theme.Dimens
import com.raptrx.still.utils.AppUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddLimitViewModel @Inject constructor(
    private val appInfoDao: AppInfoDao,
    private val appUtils: AppUtils,
    private val focusRepository: FocusRepository
) : ViewModel() {
    /** Only launchable apps can carry a limit; shortcuts are excluded. */
    val apps: StateFlow<List<AppInfo>> = appInfoDao.getAllAppsFlow()
        .map { entities ->
            appUtils.mapToAppInfo(entities)
                .filter { !it.isShortcut }
                .sortedBy { it.name.lowercase() }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun save(packageName: String, minutes: Int, onSaved: () -> Unit) {
        viewModelScope.launch {
            focusRepository.setLimit(packageName, minutes)
            onSaved()
        }
    }
}

/** The minute budgets offered when creating a limit. */
private val LIMIT_CHOICES = listOf(15, 30, 45, 60, 90, 120, 180, 240)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddLimitScreen(
    onBackClick: () -> Unit,
    onSaved: () -> Unit,
    viewModel: AddLimitViewModel = hiltViewModel()
) {
    val apps by viewModel.apps.collectAsStateWithLifecycle()
    var selected by remember { mutableStateOf<AppInfo?>(null) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(selected?.name ?: "Choose an app") },
                navigationIcon = {
                    IconButton(
                        onClick = { if (selected == null) onBackClick() else selected = null }
                    ) {
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
        if (selected == null) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                items(items = apps, key = { it.id }) { app ->
                    Text(
                        text = app.name,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selected = app }
                            .padding(
                                horizontal = Dimens.APP_HORIZONTAL_SPACING,
                                vertical = 14.dp
                            )
                    )
                }
            }
        } else {
            val app = selected ?: return@Scaffold
            Column(modifier = Modifier.padding(paddingValues)) {
                Text(
                    text = "Daily limit",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(
                        horizontal = Dimens.APP_HORIZONTAL_SPACING,
                        vertical = 12.dp
                    )
                )
                LIMIT_CHOICES.forEach { minutes ->
                    Text(
                        text = formatMinutes(minutes.toLong()),
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.save(app.packageName, minutes, onSaved)
                            }
                            .padding(
                                horizontal = Dimens.APP_HORIZONTAL_SPACING,
                                vertical = 14.dp
                            )
                    )
                }
            }
        }
    }
}
