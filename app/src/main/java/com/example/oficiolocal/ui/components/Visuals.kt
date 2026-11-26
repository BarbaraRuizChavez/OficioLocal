package com.example.oficiolocal.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Carpenter
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.Plumbing
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.oficiolocal.R
import com.example.oficiolocal.domain.RequestStatus

/** Color e \u00edcono propios de cada oficio (se usan en tarjetas, avatares y encabezados). */
data class CategoryStyle(val color: Color, val icon: ImageVector)

fun categoryStyle(category: String): CategoryStyle {
    val c = category.trim().lowercase()
    return when {
        c.startsWith("plom") || c.startsWith("plumb") -> CategoryStyle(Color(0xFF1E88E5), Icons.Filled.Plumbing)
        c.startsWith("electr") -> CategoryStyle(Color(0xFFF59E0B), Icons.Filled.ElectricBolt)
        c.startsWith("carpint") || c.startsWith("carpent") -> CategoryStyle(Color(0xFF8D6E63), Icons.Filled.Carpenter)
        c.startsWith("t\u00e9c") || c.startsWith("tec") || c.startsWith("soporte") || c.startsWith("tech") ->
            CategoryStyle(Color(0xFF7E57C2), Icons.Filled.SupportAgent)
        else -> CategoryStyle(Color(0xFF2E7D6B), Icons.Filled.Handyman)
    }
}

/** Avatar redondo con degradado y el \u00edcono del oficio. */
@Composable
fun ProviderAvatar(category: String, modifier: Modifier = Modifier, size: Dp = 56.dp) {
    val style = categoryStyle(category)
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(Brush.linearGradient(listOf(style.color, style.color.copy(alpha = 0.65f)))),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = style.icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(size * 0.52f)
        )
    }
}

/** Avatar con la inicial del usuario. */
@Composable
fun UserAvatar(name: String, modifier: Modifier = Modifier, size: Dp = 56.dp) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = name.trim().take(1).uppercase(),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = (size.value * 0.42f).sp
        )
    }
}

/** Banner con degradado e ilustraci\u00f3n (imagen vectorial propia de la app). */
@Composable
fun HeroBanner(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    footer: (@Composable () -> Unit)? = null
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.75f),
                        MaterialTheme.colorScheme.tertiary
                    )
                )
            )
    ) {
        Image(
            painter = painterResource(R.drawable.illustration_tools),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 8.dp)
                .size(width = 150.dp, height = 120.dp)
        )
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(0.62f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = title,
                color = Color.White,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                color = Color.White.copy(alpha = 0.92f),
                style = MaterialTheme.typography.bodyMedium
            )
            if (footer != null) {
                Row { footer() }
            }
        }
    }
}

/** Etiqueta de color seg\u00fan el estado de la solicitud. */
@Composable
fun StatusChip(label: String, status: RequestStatus, modifier: Modifier = Modifier) {
    val (bg, fg) = when (status) {
        RequestStatus.PENDING -> Color(0xFFFFF3CD) to Color(0xFF8A6100)
        RequestStatus.ACCEPTED -> Color(0xFFDCEBFF) to Color(0xFF0B4F9C)
        RequestStatus.COMPLETED -> Color(0xFFDDF3E4) to Color(0xFF1B6B3A)
        RequestStatus.CANCELLED -> Color(0xFFFFE1E1) to Color(0xFF9B1C1C)
    }
    Surface(color = bg, shape = RoundedCornerShape(50), modifier = modifier) {
        Text(
            text = label,
            color = fg,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
        )
    }
}

/** Fila peque\u00f1a icono + etiqueta + valor, para pantallas de detalle. */
@Composable
fun InfoRow(icon: ImageVector, label: String, value: String, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp)
        )
        Box(Modifier.width(12.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(text = value, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
fun SectionSpacer() {
    Box(Modifier.height(4.dp))
}
