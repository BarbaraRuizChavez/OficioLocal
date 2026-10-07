package com.example.oficiolocal.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
            viewModel.reviews.forEach { review -> ReviewCard(review) }
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
            Text(stringResource(R.string.review_send))
        }
    }
}

@Composable
private fun ReviewCard(review: Review) {
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
        }
    }
}
