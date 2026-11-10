package com.aserdipanda.core.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.aserdipanda.synapse.core.ui.R

/**
 * The Synapse mark: the launcher icon's glyph on brand teal. It keeps the brand colors in
 * light and dark themes, and with dynamic color.
 */
@Composable
fun SynapseLogo(
    modifier: Modifier = Modifier,
    size: Dp = 72.dp,
    contentDescription: String? = null,
) {
    Image(
        painter = painterResource(R.drawable.synapse_logo),
        contentDescription = contentDescription,
        modifier = modifier
            .size(size)
            // 20 dp corners at 72 dp, scaled for other sizes.
            .clip(RoundedCornerShape(size * (20f / 72f))),
    )
}
