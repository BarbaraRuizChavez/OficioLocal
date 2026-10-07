package com.example.oficiolocal.ui.screens.requests

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.oficiolocal.R
import com.example.oficiolocal.domain.RequestStatus
import com.example.oficiolocal.domain.ServiceRequest
import com.example.oficiolocal.domain.Urgency
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
                        RequestCard(request, onCancel = { viewModel.onCancel(request.id) })
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Button(onClick = onNewRequest, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.request_service))
            }
        }
    }
}

@Composable
private fun RequestCard(request: ServiceRequest, onCancel: () -> Unit) {
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
                TextButton(onClick = onCancel) {
                    Text(stringResource(R.string.req_cancel))
                }
            }
        }
    }
}
