package com.example.oficiolocal.ui.screens.providerprofile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.oficiolocal.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderProfileScreen(
    onBackClick: () -> Unit,
    widthSizeClass: WindowWidthSizeClass = WindowWidthSizeClass.Compact,
    viewModel: ProviderProfileViewModel = viewModel()
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.pp_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = if (widthSizeClass == WindowWidthSizeClass.Expanded) 1120.dp else 640.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (widthSizeClass == WindowWidthSizeClass.Expanded) {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CategoryField(viewModel)
                            LocationField(viewModel)
                            PriceField(viewModel)
                            ScheduleField(viewModel)
                        }
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            PhoneField(viewModel)
                            DescriptionField(viewModel)
                            ServicesField(viewModel)
                        }
                    }
                } else {
                    CategoryField(viewModel)
                    LocationField(viewModel)
                    PriceField(viewModel)
                    ScheduleField(viewModel)
                    PhoneField(viewModel)
                    DescriptionField(viewModel)
                    ServicesField(viewModel)
                }

                Button(
                    onClick = viewModel::save,
                    enabled = !viewModel.isSaving && viewModel.canSave,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(if (viewModel.isSaving) R.string.pp_saving else R.string.pp_save))
                }

                viewModel.saveState?.let { synced ->
                    Text(
                        text = stringResource(if (synced) R.string.pp_saved_cloud else R.string.pp_saved_local),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryField(viewModel: ProviderProfileViewModel) {
    OutlinedTextField(
        value = viewModel.category,
        onValueChange = { viewModel.category = it },
        modifier = Modifier.fillMaxWidth(),
        label = { Text(stringResource(R.string.pp_category)) },
        singleLine = true
    )
}

@Composable
private fun LocationField(viewModel: ProviderProfileViewModel) {
    OutlinedTextField(
        value = viewModel.location,
        onValueChange = { viewModel.location = it },
        modifier = Modifier.fillMaxWidth(),
        label = { Text(stringResource(R.string.pp_location)) },
        singleLine = true
    )
}

@Composable
private fun PriceField(viewModel: ProviderProfileViewModel) {
    OutlinedTextField(
        value = viewModel.priceRange,
        onValueChange = { viewModel.priceRange = it },
        modifier = Modifier.fillMaxWidth(),
        label = { Text(stringResource(R.string.pp_price)) },
        singleLine = true
    )
}

@Composable
private fun ScheduleField(viewModel: ProviderProfileViewModel) {
    OutlinedTextField(
        value = viewModel.schedule,
        onValueChange = { viewModel.schedule = it },
        modifier = Modifier.fillMaxWidth(),
        label = { Text(stringResource(R.string.pp_schedule)) },
        singleLine = true
    )
}

@Composable
private fun PhoneField(viewModel: ProviderProfileViewModel) {
    OutlinedTextField(
        value = viewModel.phone,
        onValueChange = { viewModel.phone = it },
        modifier = Modifier.fillMaxWidth(),
        label = { Text(stringResource(R.string.pp_phone)) },
        singleLine = true
    )
}

@Composable
private fun DescriptionField(viewModel: ProviderProfileViewModel) {
    OutlinedTextField(
        value = viewModel.description,
        onValueChange = { viewModel.description = it },
        modifier = Modifier.fillMaxWidth(),
        label = { Text(stringResource(R.string.pp_description)) },
        minLines = 3
    )
}

@Composable
private fun ServicesField(viewModel: ProviderProfileViewModel) {
    OutlinedTextField(
        value = viewModel.services,
        onValueChange = { viewModel.services = it },
        modifier = Modifier.fillMaxWidth(),
        label = { Text(stringResource(R.string.pp_services)) },
        supportingText = { Text(stringResource(R.string.pp_services_hint)) },
        minLines = 2
    )
}
