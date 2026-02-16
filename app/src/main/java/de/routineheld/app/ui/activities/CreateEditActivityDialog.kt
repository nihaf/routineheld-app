package de.routineheld.app.ui.activities

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import de.routineheld.app.ui.components.IconPicker
import de.routineheld.app.ui.theme.ActivityColorNames
import de.routineheld.app.ui.theme.ActivityColors
import de.routineheld.app.util.IconRegistry

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEditActivityDialog(
    name: String,
    iconRef: String?,
    color: Int?,
    isValid: Boolean,
    isEditMode: Boolean,
    onNameChange: (String) -> Unit,
    onIconChange: (String) -> Unit,
    onColorChange: (Int?) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    var showIconPicker by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        if (showIconPicker) {
            // Icon Picker Mode
            Column(modifier = Modifier.fillMaxSize()) {
                // Header with back button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { showIconPicker = false }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zurück")
                    }
                    Text(
                        text = "Icon wählen",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                // Icon picker fills remaining space
                IconPicker(
                    selectedIconRef = iconRef,
                    onIconSelected = { newIconRef ->
                        onIconChange(newIconRef)
                        showIconPicker = false  // Auto-close after selection
                    }
                )
            }
        } else {
            // Form Mode
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Title
                Text(
                    text = if (isEditMode) "Aktivität bearbeiten" else "Neue Aktivität",
                    style = MaterialTheme.typography.headlineSmall
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Name Field
                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChange,
                    label = { Text("Name") },
                    supportingText = { Text("${name.length}/30") },
                    isError = name.isBlank(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Icon Selector
                Text(
                    text = "Icon",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (iconRef != null) {
                        val context = LocalContext.current
                        val iconDrawable = remember(iconRef) {
                            IconRegistry.getDrawableRes(context, iconRef)
                        }
                        val iconInfo = remember(iconRef) {
                            IconRegistry.getByRef(iconRef)
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (iconDrawable != 0) {
                                Icon(
                                    painter = painterResource(id = iconDrawable),
                                    contentDescription = iconInfo?.nameDE,
                                    modifier = Modifier.size(48.dp),
                                    tint = Color.Unspecified
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = iconInfo?.nameDE ?: iconRef,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    } else {
                        Text(
                            text = "Kein Icon gewählt",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }

                    OutlinedButton(onClick = { showIconPicker = true }) {
                        Text(if (iconRef != null) "Ändern" else "Wählen")
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Color Picker
                Text(
                    text = "Farbe (optional)",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                ColorPicker(
                    selectedColor = color,
                    onColorSelected = onColorChange
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Abbrechen")
                    }
                    Button(
                        onClick = onSave,
                        enabled = isValid
                    ) {
                        Text("Speichern")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ColorPicker(
    selectedColor: Int?,
    onColorSelected: (Int?) -> Unit
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // "No color" option
        ColorCircle(
            color = null,
            label = "Keine",
            isSelected = selectedColor == null,
            onClick = { onColorSelected(null) }
        )

        // Activity colors from theme
        ActivityColors.forEachIndexed { index, color ->
            ColorCircle(
                color = color.toArgb(),
                label = ActivityColorNames[index],
                isSelected = selectedColor == color.toArgb(),
                onClick = { onColorSelected(color.toArgb()) }
            )
        }
    }
}

@Composable
private fun ColorCircle(
    color: Int?,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .border(
                    width = if (isSelected) 3.dp else 1.dp,
                    color = if (isSelected)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.outline,
                    shape = CircleShape
                )
                .padding(4.dp)
                .background(
                    color = if (color != null) Color(color) else Color.Transparent,
                    shape = CircleShape
                )
        ) {
            if (color == null) {
                Icon(
                    Icons.Outlined.Block,
                    contentDescription = "Keine Farbe",
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(24.dp),
                    tint = MaterialTheme.colorScheme.outline
                )
            }
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
