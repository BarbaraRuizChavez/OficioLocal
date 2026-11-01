package com.example.oficiolocal.ui.screens.request

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.oficiolocal.R
import com.example.oficiolocal.domain.JobType
import com.example.oficiolocal.domain.Urgency
import com.example.oficiolocal.ui.components.AppDatePickerDialog
import com.example.oficiolocal.ui.components.AppTimePickerDialog
import com.example.oficiolocal.ui.components.formatDate
import com.example.oficiolocal.ui.components.formatTime

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun NewRequestScreen(
    onBackClick: () -> Unit,
    viewModel: NewRequestViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val sentMessage = state.sentId?.let { stringResource(R.string.form_sent, it) }
    LaunchedEffect(state.sentId) {
        if (sentMessage != null) {
            snackbarHostState.showSnackbar(sentMessage)
            viewModel.onSentShown()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.form_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = state.providerName
                    ?.let { stringResource(R.string.form_for_provider, it) }
                    ?: stringResource(R.string.form_any_provider),
                style = MaterialTheme.typography.titleMedium
            )

            // Tipo de trabajo: radios
            Text(stringResource(R.string.form_job_type), style = MaterialTheme.typography.labelLarge)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                JobType.entries.forEach { job ->
                    RadioOption(
                        label = stringResource(job.label),
                        selected = state.jobType == job,
                        onClick = { viewModel.onJobTypeChange(job) }
                    )
                }
            }

            // Descripción
            OutlinedTextField(
                value = state.description,
                onValueChange = viewModel::onDescriptionChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.form_description)) },
                minLines = 3,
                isError = state.descriptionError,
                supportingText = {
                    if (state.descriptionError) Text(stringResource(R.string.err_description))
                }
            )

            // Ubicación
            OutlinedTextField(
                value = state.location,
                onValueChange = viewModel::onLocationChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.form_location)) },
                singleLine = true,
                isError = state.locationError,
                supportingText = {
                    if (state.locationError) Text(stringResource(R.string.err_location))
                }
            )

            // Urgencia: radios
            Text(stringResource(R.string.urgency), style = MaterialTheme.typography.labelLarge)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Urgency.entries.forEach { urgency ->
                    RadioOption(
                        label = stringResource(urgency.label),
                        selected = state.urgency == urgency,
                        onClick = { viewModel.onUrgencyChange(urgency) }
                    )
                }
            }

            // Requiere materiales: check
            Row(
                modifier = Modifier.toggleable(
                    value = state.needsMaterials,
                    role = Role.Checkbox,
                    onValueChange = viewModel::onNeedsMaterialsChange
                ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(checked = state.needsMaterials, onCheckedChange = null)
                Text(
                    text = stringResource(R.string.needs_materials),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            // Fecha y hora
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { showDatePicker = true }) {
                    Text(
                        state.dateMillis?.let { formatDate(it) }
                            ?: stringResource(R.string.form_pick_date)
                    )
                }
                OutlinedButton(onClick = { showTimePicker = true }) {
                    val hour = state.hour
                    val minute = state.minute
                    Text(
                        if (hour != null && minute != null) formatTime(hour, minute)
                        else stringResource(R.string.form_pick_time)
                    )
                }
            }
            if (state.dateTimeError) {
                Text(
                    text = stringResource(R.string.err_datetime),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Button(onClick = viewModel::submit, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.form_send))
            }
        }
    }

    if (showDatePicker) {
        AppDatePickerDialog(
            onConfirm = {
                viewModel.onDateSelected(it)
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }
    if (showTimePicker) {
        AppTimePickerDialog(
            onConfirm = { hour, minute ->
                viewModel.onTimeSelected(hour, minute)
                showTimePicker = false
            },
            onDismiss = { showTimePicker = false }
        )
    }
}

@Composable
private fun RadioOption(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier.selectable(selected = selected, role = Role.RadioButton, onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = null)
        Text(label, modifier = Modifier.padding(start = 6.dp))
    }
}