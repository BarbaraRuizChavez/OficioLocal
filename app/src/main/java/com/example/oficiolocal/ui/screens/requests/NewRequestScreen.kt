package com.example.oficiolocal.ui.screens.requests

import androidx.compose.foundation.clickable
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.oficiolocal.R
import com.example.oficiolocal.domain.Urgency

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewRequestScreen(
    onDone: () -> Unit,
    viewModel: NewRequestViewModel = viewModel()
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.request_service),
                style = MaterialTheme.typography.headlineSmall
            )

            // Caja de texto con validación
            OutlinedTextField(
                value = viewModel.description,
                onValueChange = viewModel::onDescription,
                label = { Text(stringResource(R.string.description)) },
                isError = viewModel.errorRes == R.string.field_required,
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )

            // Radios: urgencia
            Text(
                text = stringResource(R.string.urgency),
                style = MaterialTheme.typography.titleSmall
            )
            Urgency.entries.forEach { option ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { viewModel.onUrgency(option) }
                ) {
                    RadioButton(
                        selected = viewModel.urgency == option,
                        onClick = { viewModel.onUrgency(option) }
                    )
                    Text(urgencyLabel(option))
                }
            }

            // Check: requiere materiales
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { viewModel.onNeedsMaterials(!viewModel.needsMaterials) }
            ) {
                Checkbox(
                    checked = viewModel.needsMaterials,
                    onCheckedChange = viewModel::onNeedsMaterials
                )
                Text(stringResource(R.string.needs_materials))
            }

            // DatePicker y TimePicker
            OutlinedButton(onClick = { showDatePicker = true }) {
                Text(
                    viewModel.dateMillis?.let { formatRequestDate(it) }
                        ?: stringResource(R.string.pick_date)
                )
            }
            OutlinedButton(onClick = { showTimePicker = true }) {
                val hour = viewModel.hour
                Text(
                    if (hour != null) "%02d:%02d".format(hour, viewModel.minute ?: 0)
                    else stringResource(R.string.pick_time)
                )
            }

            viewModel.errorRes
                ?.takeIf { it != R.string.field_required }
                ?.let { error ->
                    Text(
                        text = stringResource(error),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

            Button(
                onClick = { viewModel.submit(onDone) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.send))
            }
            TextButton(onClick = onDone, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.req_discard))
            }
        }
    }

    if (showDatePicker) {
        val dateState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.onDate(dateState.selectedDateMillis)
                    showDatePicker = false
                }) {
                    Text(stringResource(R.string.ok))
                }
            }
        ) {
            DatePicker(state = dateState)
        }
    }

    if (showTimePicker) {
        val timeState = rememberTimePickerState(is24Hour = true)
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.onTime(timeState.hour, timeState.minute)
                    showTimePicker = false
                }) {
                    Text(stringResource(R.string.ok))
                }
            },
            text = { TimePicker(state = timeState) }
        )
    }
}
