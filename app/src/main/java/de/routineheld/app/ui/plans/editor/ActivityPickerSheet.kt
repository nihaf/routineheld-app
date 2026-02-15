package de.routineheld.app.ui.plans.editor

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import de.routineheld.app.data.local.entity.ActivityEntity
import de.routineheld.app.util.IconRegistry

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityPickerSheet(
    activities: List<ActivityEntity>,
    usedActivityIds: Set<Long>,
    onActivitySelected: (ActivityEntity) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Aktivität auswählen",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(24.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(activities, key = { it.id }) { activity ->
                    ActivityPickerItem(
                        activity = activity,
                        isUsed = activity.id in usedActivityIds,
                        onClick = { onActivitySelected(activity) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ActivityPickerItem(
    activity: ActivityEntity,
    isUsed: Boolean,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val iconRes = remember(activity.iconRef) {
        IconRegistry.getDrawableRes(context, activity.iconRef)
    }

    Card(
        modifier = Modifier
            .aspectRatio(0.9f)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isUsed)
                MaterialTheme.colorScheme.secondaryContainer
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (iconRes != 0) {
                    Icon(
                        painter = painterResource(id = iconRes),
                        contentDescription = activity.name,
                        modifier = Modifier.size(40.dp),
                        tint = Color.Unspecified
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = activity.name,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (isUsed) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "Bereits verwendet",
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
