package com.raptrx.still.ui.settings

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.raptrx.still.R
import com.raptrx.still.ui.theme.Dimens
import com.raptrx.still.utils.openHomeSettings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    onFavouriteAppsClick: () -> Unit,
    onHiddenAppsClick: () -> Unit,
    onCustomisationClick: () -> Unit,
    onFocusClick: () -> Unit,
    onBackupClick: () -> Unit,
    onSupporterClick: () -> Unit,
    onAboutAppClick: () -> Unit
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        stringResource(R.string.settings)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back),
                            contentDescription = "Back"
                        )
                    }
                },
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            SettingsItem(
                name = stringResource(R.string.favourite_apps),
                onClick = onFavouriteAppsClick
            )
            SettingsItem(name = stringResource(R.string.hidden_apps), onClick = onHiddenAppsClick)
            SettingsItem(
                name = stringResource(R.string.focus),
                onClick = onFocusClick
            )
            SettingsItem(
                name = stringResource(R.string.customisation),
                onClick = onCustomisationClick
            )
            SettingsItem(
                name = stringResource(R.string.backup_and_restore),
                onClick = onBackupClick
            )
            SettingsItem(
                name = stringResource(R.string.set_default_launcher),
                onClick = context::openHomeSettings
            )
            SettingsItem(
                name = stringResource(R.string.about_app),
                onClick = onAboutAppClick
            )
            SettingsItem(
                name = stringResource(R.string.become_a_supporter),
                onClick = onSupporterClick
            )

        }
    }
}

@Composable
private fun SettingsItem(name: String, onClick: () -> Unit) {
    Text(
        text = name,
        color = MaterialTheme.colorScheme.onSurface,
        fontSize = 20.sp,
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick
            )
            .padding(horizontal = Dimens.APP_HORIZONTAL_SPACING, vertical = 16.dp),
    )
}
