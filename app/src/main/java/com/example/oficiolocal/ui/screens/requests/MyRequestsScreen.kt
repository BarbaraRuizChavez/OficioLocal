package com.example.oficiolocal.ui.screens.requests

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import com.example.oficiolocal.domain.RequestStatus
import com.example.oficiolocal.domain.ServiceRequest
import com.example.oficiolocal.domain.JobType
import com.example.oficiolocal.domain.Urgency
import com.example.oficiolocal.ui.components.AppDatePickerDialog
import com.example.oficiolocal.ui.components.AppTimePickerDialog
import com.example.oficiolocal.ui.components.formatTime
import java.text.DateFormat
import java.util.Date
import java.util.TimeZone

/** El DatePicker entrega la fecha en UTC; se formatea en UTC para que no salga un día antes. */
internal fun formatRequestDate(millis: Long): String {
    val format = DateFormat.getDateInstance(DateFormat.MEDIUM)
    format.timeZone = TimeZone.getTimeZone("UTC")
    return format.format(Date(millis))
}

@Composable
private fun statusLabel(status: RequestStatus): String = stringResource(
    when (status) {
        RequestStatus.PENDING -> R.string.req_status_pending
        RequestStatus.ACCEPTED -> R.string.req_status_accepted
        RequestStatus.COMPLETED -> R.string.req_status_completed
        RequestStatus.CANCELLED -> R.string.req_status_cancelled
    }
)

@Composable
internal fun urgencyLabel(urgency: Urgency): String = stringResource(
    when (urgency) {
        Urgency.LOW -> R.string.urgency_low
        Urgency.MEDIUM -> R.string.urgency_medium
        Urgency.HIGH -> R.string.urgency_high
    }
)

@Composable
fun MyRequestsScreen(
    onNewRequest: () -> Unit,
    viewModel: MyRequestsViewModel = viewModel()
) {
    val requests by viewModel.requests.collectAsState()
    val selected by viewModel.selected.collectAsState()
    val filters: List<RequestStatus?> = listOf(null) + RequestStatus.entries
    var requestToEdit by remember { mutableStateOf<ServiceRequest?>(null) }
    var requestToDelete by remember { mutableStateOf<ServiceRequest?>(null) }
    var editDescription by remember { mutableStateOf("") }
    var editLocation by remember { mutableStateOf("") }
    var editJobType by remember { mutableStateOf(JobType.PLUMBING) }
    var editNeedsMaterials by remember { mutableStateOf(false) }
    var editUrgency by remember { mutableStateOf(Urgency.MEDIUM) }
    var editDateMillis by remember { mutableStateOf<Long?>(null) }
    var editHour by remember { mutableStateOf<Int?>(null) }
    var editMinute by remember { mutableStateOf<Int?>(null) }
    var showEditDatePicker by remember { mutableStateOf(false) }
    var showEditTimePicker by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(
            modifier = Modifier
                .widthIn(max = 720.dp)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.req_title),
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(Modifier.height(12.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(filters) { status ->
                    FilterChip(
                        selected = selected == status,
                        onClick = { viewModel.onFilterSelected(status) },
                        label = {
                            Text(
                                if (status == null) stringResource(R.string.all)
                                else statusLabel(status)
                            )
                        }
                    )
                }
            }
            Spacer(Modifier.height(12.dp))

            if (requests.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.req_empty))
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(requests, key = { it.id }) { request ->
                        RequestCard(
                            request = request,
                            onEdit = {
                                requestToEdit = request
                                editDescription = request.description
                                editLocation = request.location
                                editJobType = request.jobType ?: JobType.PLUMBING
                                editNeedsMaterials = request.needsMaterials
                                editUrgency = request.urgency
                                editDateMillis = request.dateMillis
                                editHour = request.hour
                                editMinute = request.minute
                            },
                            onCancel = { viewModel.onCancel(request.id) },
                            onDelete = { requestToDelete = request }
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Button(onClick = onNewRequest, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.request_service))
            }
        }
    }

    requestToEdit?.let { request ->
        AlertDialog(
            onDismissRequest = { requestToEdit = null },
            title = { Text(stringResource(R.string.req_edit_title)) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = editDescription,
                        onValueChange = { editDescription = it },
                        label = { Text(stringResource(R.string.req_description)) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                    OutlinedTextField(
                        value = editLocation,
                        onValueChange = { editLocation = it },
                        label = { Text(stringResource(R.string.req_location)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Text(stringResource(R.string.form_job_type))
                    JobType.entries.forEach { jobType ->
                        FilterChip(
                            selected = editJobType == jobType,
                            onClick = { editJobType = jobType },
                            label = { Text(stringResource(jobType.label)) }
                        )
                    }
                    Text(stringResource(R.string.urgency))
                    Urgency.entries.forEach { urgency ->
                        FilterChip(
                            selected = editUrgency == urgency,
                            onClick = { editUrgency = urgency },
                            label = { Text(urgencyLabel(urgency)) }
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = editNeedsMaterials,
                            onCheckedChange = { editNeedsMaterials = it }
                        )
                        Text(stringResource(R.string.needs_materials))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { showEditDatePicker = true }) {
                            Text(
                                editDateMillis?.let { formatRequestDate(it) }
                                    ?: stringResource(R.string.form_pick_date)
                            )
                        }
                        OutlinedButton(onClick = { showEditTimePicker = true }) {
                            Text(
                                if (editHour != null && editMinute != null) {
                                    formatTime(editHour!!, editMinute!!)
                                } else {
                                    stringResource(R.string.form_pick_time)
                                }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.onUpdate(
                            request.copy(
                                description = editDescription.trim(),
                                location = editLocation.trim(),
                                jobType = editJobType,
                                needsMaterials = editNeedsMaterials,
                                urgency = editUrgency,
                                dateMillis = editDateMillis,
                                hour = editHour,
                                minute = editMinute
                            )
                        )
                        requestToEdit = null
                    },
                    enabled = editDescription.isNotBlank() &&
                        editLocation.isNotBlank() &&
                        editDateMillis != null &&
                        editHour != null &&
                        editMinute != null
                ) {
                    Text(stringResource(R.string.req_save))
                }
            },
            dismissButton = {
                TextButton(onClick = { requestToEdit = null }) {
                    Text(stringResource(R.string.req_discard))
                }
            }
        )
    }

    requestToDelete?.let { request ->
        AlertDialog(
            onDismissRequest = { requestToDelete = null },
            title = { Text(stringResource(R.string.req_delete_title)) },
            text = { Text(stringResource(R.string.req_delete_confirmation)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.onDelete(request.id)
                        requestToDelete = null
                    }
                ) {
                    Text(stringResource(R.string.req_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { requestToDelete = null }) {
                    Text(stringResource(R.string.req_discard))
                }
            }
        )
    }

    if (showEditDatePicker) {
        AppDatePickerDialog(
            onConfirm = {
                editDateMillis = it
                showEditDatePicker = false
            },
            onDismiss = { showEditDatePicker = false },
            initialDateMillis = editDateMillis
        )
    }
    if (showEditTimePicker) {
        AppTimePickerDialog(
            onConfirm = { hour, minute ->
                editHour = hour
                editMinute = minute
                showEditTimePicker = false
            },
            onDismiss = { showEditTimePicker = false },
            initialHour = editHour ?: 9,
            initialMinute = editMinute ?: 0
        )
    }
}

@Composable
private fun RequestCard(
    request: ServiceRequest,
    onEdit: () -> Unit,
    onCancel: () -> Unit,
    onDelete: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = request.providerName.ifBlank { stringResource(R.string.req_unassigned) },
                    style = MaterialTheme.typography.titleMedium
                )
                AssistChip(onClick = {}, label = { Text(statusLabel(request.status)) })
            }

            Text(request.description)

            Text(
                text = stringResource(R.string.urgency) + ": " + urgencyLabel(request.urgency),
                style = MaterialTheme.typography.bodySmall
            )
            if (request.needsMaterials) {
                Text(
                    text = stringResource(R.string.needs_materials),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            val date = request.dateMillis?.let { formatRequestDate(it) }
            val time = request.hour?.let { "%02d:%02d".format(it, request.minute ?: 0) }
            val whenText = listOfNotNull(date, time).joinToString(" · ")
            if (whenText.isNotEmpty()) {
                Text(whenText, style = MaterialTheme.typography.bodySmall)
            }

            if (request.status == RequestStatus.PENDING) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onEdit) {
                        Text(stringResource(R.string.req_edit))
                    }
                    TextButton(onClick = onCancel) {
                        Text(stringResource(R.string.req_cancel))
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDelete) {
                    Text(stringResource(R.string.req_delete))
                }
            }
        }
    }
}
