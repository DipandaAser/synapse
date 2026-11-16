package com.aserdipanda.synapse.feature.onboarding.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.aserdipanda.synapse.feature.onboarding.R

/**
 * The frame every onboarding screen shares: an optional back arrow, scrolling content, and the
 * actions at the bottom. Content draws behind the system bars and is padded to stay clear of them.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun OnboardingScaffold(
    onBack: (() -> Unit)?,
    actions: @Composable ColumnScope.() -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    Scaffold(
        topBar = {
            if (onBack != null) {
                TopAppBar(
                    title = {},
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                                contentDescription = stringResource(R.string.onboarding_back),
                            )
                        }
                    },
                )
            }
        },
        contentWindowInsets = WindowInsets.safeDrawing,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(
                    start = 24.dp,
                    top = if (onBack == null) 28.dp else 8.dp,
                    end = 24.dp,
                    bottom = 16.dp,
                ),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            content()
            // Keeps the actions at the bottom when the content is shorter than the screen.
            Spacer(Modifier.weight(1f))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp), content = actions)
        }
    }
}

@Composable
internal fun ScreenTitle(
    title: String,
    body: String,
    titleStyle: TextStyle = MaterialTheme.typography.headlineSmall,
    bodyStyle: TextStyle = MaterialTheme.typography.bodyMedium,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = title, style = titleStyle, modifier = Modifier.semantics { heading() })
        Text(text = body, style = bodyStyle, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/** An icon on a tinted shape: 40 dp circles in lists, 56 dp squircles above a screen title. */
@Composable
internal fun IconBadge(
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    size: Dp = 40.dp,
    iconSize: Dp = 20.dp,
    shape: Shape = CircleShape,
) {
    Surface(
        modifier = Modifier.size(size),
        shape = shape,
        color = containerColor,
        contentColor = contentColor,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(iconSize))
        }
    }
}

@Composable
internal fun ErrorHeaderBadge(icon: ImageVector) {
    IconBadge(
        icon = icon,
        containerColor = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
        size = 56.dp,
        iconSize = 24.dp,
        shape = RoundedCornerShape(16.dp),
    )
}

/**
 * One row in an outlined card. TalkBack reads the whole row as one item, so the text must say
 * everything an icon shows.
 */
@Composable
internal fun CardRow(
    alignTop: Boolean = false,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val verticalPadding = if (alignTop) 12.dp else 8.dp
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .semantics(mergeDescendants = true) {}
            .padding(start = 16.dp, top = verticalPadding, end = 24.dp, bottom = verticalPadding),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = if (alignTop) Alignment.Top else Alignment.CenterVertically,
    ) {
        leading?.invoke()
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
            content = content,
        )
        trailing?.invoke()
    }
}

@Composable
internal fun InfoNote(text: String) {
    Row(
        modifier = Modifier.padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            imageVector = Icons.Outlined.Info,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** The screen's main action: full width and 56 dp tall. */
@Composable
internal fun LargeButton(text: String, onClick: () -> Unit, icon: ImageVector? = null) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        contentPadding = if (icon == null) {
            ButtonDefaults.ContentPadding
        } else {
            ButtonDefaults.ButtonWithIconContentPadding
        },
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(ButtonDefaults.IconSize),
            )
            Spacer(Modifier.width(ButtonDefaults.IconSpacing))
        }
        Text(text = text, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
internal fun NotNowButton(onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
    ) {
        Text(text = stringResource(R.string.onboarding_not_now))
    }
}
