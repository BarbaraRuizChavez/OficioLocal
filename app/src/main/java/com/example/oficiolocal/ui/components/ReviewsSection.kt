package com.example.oficiolocal.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.oficiolocal.domain.Review

/**
 * Reseñas de un prestador: promedio, lista y formulario con estrellas.
 * Uso en el Detalle: ReviewsSection(providerId = provider.id)
 * Es una Column normal (no LazyColumn) para poder ir dentro de una pantalla con scroll.
 */
@Composable
fun ReviewsSection(
    providerId: String,
    modifier: Modifier = Modifier,
    viewModel: ReviewsViewModel = viewModel(key = "reviews_$providerId") {
        ReviewsViewModel(providerId)
    }
) {
    var reviewToDelete by remember { mutableStateOf<Review?>(null) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.reviews_title),
            style = MaterialTheme.typography.titleLarge
        )

        if (viewModel.reviews.isEmpty()) {
            Text(
                text = stringResource(R.string.reviews_empty),
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            val average = "%.1f".format(viewModel.reviews.map { it.rating }.average())
            Text(
                text = stringResource(R.string.reviews_avg, average),
                style = MaterialTheme.typography.bodyMedium
            )
            viewModel.reviews.forEach { review ->
                ReviewCard(
                    review = review,
                    canManage = viewModel.canManage(review),
                    onEdit = { viewModel.startEditing(review) },
                    onDelete = { reviewToDelete = review }
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            (1..5).forEach { star ->
                IconButton(onClick = { viewModel.onRating(star) }) {
                    Icon(
                        imageVector = if (star <= viewModel.rating) Icons.Filled.Star else Icons.Filled.StarBorder,
                        contentDescription = stringResource(R.string.star_label, star),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        OutlinedTextField(
            value = viewModel.comment,
            onValueChange = viewModel::onComment,
            label = { Text(stringResource(R.string.review_hint)) },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = viewModel::submit,
            enabled = viewModel.rating > 0
        ) {
            Text(
                stringResource(
                    if (viewModel.editingReviewId == null) R.string.review_send
                    else R.string.review_save_changes
                )
            )
        }

        if (viewModel.editingReviewId != null) {
            TextButton(onClick = viewModel::cancelEditing) {
                Text(stringResource(R.string.review_cancel_edit))
            }
        }
    }

    reviewToDelete?.let { review ->
        AlertDialog(
            onDismissRequest = { reviewToDelete = null },
            title = { Text(stringResource(R.string.review_delete_title)) },
            text = { Text(stringResource(R.string.review_delete_confirmation)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.delete(review)
                        reviewToDelete = null
                    }
                ) {
                    Text(stringResource(R.string.review_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { reviewToDelete = null }) {
                    Text(stringResource(R.string.review_cancel_edit))
                }
            }
        )
    }
}

@Composable
private fun ReviewCard(
    review: Review,
    canManage: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(review.userName, style = MaterialTheme.typography.titleMedium)
            Text(
                text = "★ ${review.rating}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp)
            )
            Text(
                text = review.comment,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                text = review.date,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp)
            )
            if (canManage) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onEdit) {
                        Text(stringResource(R.string.review_edit))
                    }
                    TextButton(onClick = onDelete) {
                        Text(stringResource(R.string.review_delete))
                    }
                }
            }
        }
    }
}
