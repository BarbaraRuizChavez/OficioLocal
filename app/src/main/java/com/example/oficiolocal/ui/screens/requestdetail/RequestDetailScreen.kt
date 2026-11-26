package com.example.oficiolocal.ui.screens.requestdetail

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.oficiolocal.R
import com.example.oficiolocal.domain.RequestStatus
import com.example.oficiolocal.ui.screens.requests.formatRequestDate
import com.example.oficiolocal.ui.screens.requests.statusLabel
import com.example.oficiolocal.ui.screens.requests.urgencyLabel
import coil3.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestDetailScreen(
    requestId: String,
    onBackClick: () -> Unit,
    viewModel: RequestDetailViewModel = viewModel(
        key = requestId,
        factory = viewModelFactory {
            initializer { RequestDetailViewModel(requestId) }
        }
    )
) {
    val request by viewModel.request.collectAsState()
    val attachments by viewModel.attachments.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.rd_title)) },
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
        val item = request
        if (item == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(stringResource(R.string.rd_not_found))
            }
        } else {
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
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = item.providerName.ifBlank { stringResource(R.string.req_unassigned) },
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text("${stringResource(R.string.rd_status)}: ${statusLabel(item.status)}")
                    Text("${stringResource(R.string.rd_urgency)}: ${urgencyLabel(item.urgency)}")
                    item.jobType?.let { Text(stringResource(it.label)) }
                    Text("${stringResource(R.string.rd_description)}: ${item.description}")
                    Text("${stringResource(R.string.rd_location)}: ${item.location}")
                    item.dateMillis?.let { millis ->
                        val time = if (item.hour != null && item.minute != null) {
                            " %02d:%02d".format(item.hour, item.minute)
                        } else {
                            ""
                        }
                        Text("${stringResource(R.string.rd_date)}: ${formatRequestDate(millis)}$time")
                    }
                    Text(
                        "${stringResource(R.string.rd_materials)}: " +
                            stringResource(if (item.needsMaterials) R.string.rd_yes else R.string.rd_no)
                    )

                    if (attachments.isNotEmpty()) {
                        Text(stringResource(R.string.rd_attachments), style = MaterialTheme.typography.titleMedium)
                        attachments.forEach { attachment ->
                            AsyncImage(
                                model = attachment.remoteUrl ?: Uri.parse(attachment.uri),
                                contentDescription = stringResource(R.string.rd_photo_description),
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxWidth().height(220.dp).clip(RoundedCornerShape(12.dp))
                            )
                            Text(
                                stringResource(
                                    when (attachment.syncState) {
                                        "SYNCED" -> R.string.media_sync_synced
                                        "FAILED" -> R.string.media_sync_failed
                                        "PENDING" -> R.string.media_sync_pending
                                        else -> R.string.media_sync_local
                                    }
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                    if (viewModel.isProvider) {
                        if (item.status == RequestStatus.PENDING) {
                            Button(onClick = viewModel::accept, modifier = Modifier.fillMaxWidth()) {
                                Text(stringResource(R.string.rd_accept))
                            }
                            OutlinedButton(onClick = viewModel::reject, modifier = Modifier.fillMaxWidth()) {
                                Text(stringResource(R.string.rd_reject))
                            }
                        }
                        if (item.status == RequestStatus.ACCEPTED) {
                            Button(onClick = viewModel::complete, modifier = Modifier.fillMaxWidth()) {
                                Text(stringResource(R.string.rd_complete))
                            }
                        }
                    } else if (item.status == RequestStatus.PENDING || item.status == RequestStatus.ACCEPTED) {
                        OutlinedButton(onClick = viewModel::cancel, modifier = Modifier.fillMaxWidth()) {
                            Text(stringResource(R.string.req_cancel))
                        }
                    }
                }
            }
        }
    }
}
