package de.routineheld.app.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import de.routineheld.app.util.IconCategory
import de.routineheld.app.util.IconInfo
import de.routineheld.app.util.IconRegistry

@Composable
fun IconPicker(
    selectedIconRef: String?,
    onIconSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<IconCategory?>(null) }

    val filteredIcons = remember(searchQuery, selectedCategory) {
        when {
            searchQuery.isNotBlank() -> IconRegistry.search(searchQuery)
            selectedCategory != null -> IconRegistry.getByCategory(selectedCategory!!)
            else -> IconRegistry.allIcons
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // Search Field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Icon suchen...") },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Suchen")
            },
            trailingIcon = {
                if (searchQuery.isNotBlank()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Löschen")
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            singleLine = true
        )

        // Category Filter Chips
        if (searchQuery.isBlank()) {
            LazyRow(
                modifier = Modifier.padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedCategory == null,
                        onClick = { selectedCategory = null },
                        label = { Text("Alle") }
                    )
                }
                items(IconCategory.entries.toTypedArray()) { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        label = { Text(category.labelDE) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Icon Grid or Empty State
        if (filteredIcons.isEmpty()) {
            EmptyState(
                icon = Icons.Outlined.SearchOff,
                title = "Keine Icons gefunden",
                subtitle = "Versuche es mit einem anderen Suchbegriff",
                modifier = Modifier.fillMaxSize()
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredIcons, key = { it.ref }) { iconInfo ->
                    IconItem(
                        iconInfo = iconInfo,
                        isSelected = iconInfo.ref == selectedIconRef,
                        onClick = { onIconSelected(iconInfo.ref) }
                    )
                }
            }
        }
    }
}

@Composable
private fun IconItem(
    iconInfo: IconInfo,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val drawableRes = remember(iconInfo.ref) {
        IconRegistry.getDrawableRes(context, iconInfo.ref)
    }

    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = MaterialTheme.shapes.small
            )
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (drawableRes != 0) {
            Icon(
                painter = painterResource(id = drawableRes),
                contentDescription = iconInfo.nameDE,
                modifier = Modifier.size(40.dp),
                tint = Color.Unspecified  // Preserve original icon colors
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = iconInfo.nameDE,
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
