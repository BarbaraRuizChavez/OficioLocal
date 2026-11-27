package com.example.oficiolocal.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil3.compose.AsyncImage
import com.example.oficiolocal.R

/**
 * Fila de fotos de trabajos. Al tocar una se ve grande.
 * Si se pasa [onRemove], cada foto muestra una X para quitarla (modo edición del prestador).
 */
@Composable
fun WorkGallery(
    photos: List<String>,
    modifier: Modifier = Modifier,
    onRemove: ((String) -> Unit)? = null
) {
    var selected by remember { mutableStateOf<String?>(null) }

    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(photos, key = { it }) { url ->
            Box {
                AsyncImage(
                    model = url,
                    contentDescription = stringResource(R.string.work_photo_description),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(130.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0x22888888))
                        .clickable { selected = url }
                )
                if (onRemove != null) {
                    IconButton(
                        onClick = { onRemove(url) },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(0xAA000000))
                    ) {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = stringResource(R.string.photo_remove),
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }

    selected?.let { url ->
        Dialog(onDismissRequest = { selected = null }) {
            AsyncImage(
                model = url,
                contentDescription = stringResource(R.string.work_photo_description),
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 520.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { selected = null }
            )
        }
    }
}
