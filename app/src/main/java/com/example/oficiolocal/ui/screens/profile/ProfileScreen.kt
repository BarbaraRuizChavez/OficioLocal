package com.example.oficiolocal.ui.screens.profile

import android.app.LocaleManager
import android.content.Context
import android.os.Build
import android.os.LocaleList
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.oficiolocal.R

private fun setLanguage(context: Context, tag: String) {
    if (Build.VERSION.SDK_INT >= 33) {
        context.getSystemService(LocaleManager::class.java).applicationLocales =
            LocaleList.forLanguageTags(tag)
    }
}

@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    val darkMode by viewModel.darkMode.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    val offline by viewModel.offline.collectAsState()
    val context = LocalContext.current
    val isEnglish = LocalConfiguration.current.locales[0].language == "en"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stringResource(R.string.profile_title),
            style = MaterialTheme.typography.headlineSmall
        )

        viewModel.user?.let { user ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(user.name, style = MaterialTheme.typography.titleMedium)
                    Text(user.email, style = MaterialTheme.typography.bodyMedium)
                    Text(
                        text = stringResource(
                            if (user.isProvider) R.string.role_provider else R.string.role_client
                        ),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        Text(
            text = stringResource(R.string.language),
            style = MaterialTheme.typography.titleSmall
        )
        listOf("es" to R.string.lang_es, "en" to R.string.lang_en).forEach { (tag, label) ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { setLanguage(context, tag) }
            ) {
                RadioButton(
                    selected = (tag == "en") == isEnglish,
                    onClick = { setLanguage(context, tag) }
                )
                Text(stringResource(label))
            }
        }
        if (Build.VERSION.SDK_INT < 33) {
            Text(
                text = stringResource(R.string.lang_hint),
                style = MaterialTheme.typography.bodySmall
            )
        }

        SettingRow(R.string.dark_theme, darkMode, viewModel::onDarkMode)
        SettingRow(R.string.notifications, notifications, viewModel::onNotifications)
        SettingRow(R.string.simulate_offline, offline, viewModel::onOffline)

        Text(
            text = stringResource(R.string.sync_status) + ": " +
                stringResource(if (offline) R.string.sync_offline else R.string.sync_ok)
        )

        Button(
            onClick = {
                viewModel.logout()
                onLogout()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.logout))
        }
    }
}

@Composable
private fun SettingRow(
    @StringRes label: Int,
    checked: Boolean,
    onChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(stringResource(label))
        Switch(checked = checked, onCheckedChange = onChange)
    }
}
