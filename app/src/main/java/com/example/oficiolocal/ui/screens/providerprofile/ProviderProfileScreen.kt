package com.example.oficiolocal.ui.screens.providerprofile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.ui.text.font.FontWeight
import com.example.oficiolocal.ui.components.ProviderAvatar
import com.example.oficiolocal.ui.components.WorkGallery
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
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
    viewModel: ProviderProfileViewModel = viewModel()
) {
    val avatarPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri -> viewModel.onAvatarPicked(uri) }
    val workPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(maxItems = 5)
    ) { uris -> viewModel.onWorkPhotosPicked(uris) }

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
                    .widthIn(max = 640.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.pp_photo_title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ProviderAvatar(
                        category = viewModel.category,
                        photoUrl = viewModel.photoUrl,
                        size = 88.dp
                    )
                    Spacer(Modifier.width(16.dp))
                    OutlinedButton(
                        onClick = {
                            avatarPicker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        enabled = !viewModel.photoBusy && viewModel.canSave
                    ) {
                        Text(stringResource(R.string.pp_photo_change))
                    }
                    if (viewModel.photoBusy) {
                        Spacer(Modifier.width(12.dp))
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 3.dp)
                    }
                }

                Text(
                    text = stringResource(R.string.pp_work_title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                if (viewModel.gallery.isNotEmpty()) {
                    WorkGallery(photos = viewModel.gallery, onRemove = viewModel::removeWorkPhoto)
                }
                Text(
                    text = stringResource(R.string.pp_work_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedButton(
                    onClick = {
                        workPicker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    enabled = !viewModel.photoBusy && viewModel.canSave && viewModel.canAddWorkPhotos,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.pp_work_add))
                }
                viewModel.photoMessage?.let { message ->
                    Text(
                        text = stringResource(message),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                OutlinedTextField(
                    value = viewModel.category,
                    onValueChange = { viewModel.category = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.pp_category)) },
                    singleLine = true
                )
                OutlinedTextField(
                    value = viewModel.location,
                    onValueChange = { viewModel.location = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.pp_location)) },
                    singleLine = true
                )
                OutlinedTextField(
                    value = viewModel.priceRange,
                    onValueChange = { viewModel.priceRange = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.pp_price)) },
                    singleLine = true
                )
                OutlinedTextField(
                    value = viewModel.schedule,
                    onValueChange = { viewModel.schedule = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.pp_schedule)) },
                    singleLine = true
                )
                OutlinedTextField(
                    value = viewModel.phone,
                    onValueChange = { viewModel.phone = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.pp_phone)) },
                    singleLine = true
                )
                OutlinedTextField(
                    value = viewModel.description,
                    onValueChange = { viewModel.description = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.pp_description)) },
                    minLines = 3
                )
                OutlinedTextField(
                    value = viewModel.services,
                    onValueChange = { viewModel.services = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.pp_services)) },
                    supportingText = { Text(stringResource(R.string.pp_services_hint)) },
                    minLines = 2
                )

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
