package com.vegam.budgetcalculator.presentation.category

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.vegam.budgetcalculator.domain.model.Category
import com.vegam.budgetcalculator.domain.model.SubCategory

private val categoryIcons = listOf("🏠", "🛒", "🥬", "⛽", "💧", "⚡", "👤", "🏥", "💳", "🔑", "🍽️", "✈️", "🛍️", "🎓", "🎬", "🏦", "🛡️", "📦")
private val categoryColors = listOf(
    0xFFF44336.toInt(), 0xFFE91E63.toInt(), 0xFF9C27B0.toInt(),
    0xFF3F51B5.toInt(), 0xFF2196F3.toInt(), 0xFF009688.toInt(),
    0xFF4CAF50.toInt(), 0xFFFF9800.toInt(), 0xFF795548.toInt()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageCategoriesScreen(
    onBackPressed: () -> Unit,
    viewModel: ManageCategoriesViewModel = hiltViewModel()
) {
    val categories by viewModel.categories.collectAsState()
    val subCategories by viewModel.subCategories.collectAsState()
    var editingCategory by remember { mutableStateOf<Category?>(null) }
    var showCategoryDialog by remember { mutableStateOf(false) }
    var subCategoryTarget by remember { mutableStateOf<Category?>(null) }
    var editingSubCategory by remember { mutableStateOf<SubCategory?>(null) }
    var expandedIds by remember { mutableStateOf(emptySet<String>()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Categories") },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                editingCategory = null
                showCategoryDialog = true
            }) {
                Icon(Icons.Default.Add, contentDescription = "Create category")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Text(
                    "Create unlimited categories and optional subcategories. Tap a category to expand it.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            items(categories, key = { it.id }) { category ->
                val expanded = category.id in expandedIds
                Card(colors = CardDefaults.cardColors(containerColor = Color(category.color).copy(alpha = 0.10f))) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable {
                                expandedIds = if (expanded) expandedIds - category.id else expandedIds + category.id
                            }.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier.size(42.dp).background(Color(category.color), CircleShape),
                                contentAlignment = Alignment.Center
                            ) { Text(category.icon) }
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(category.name, fontWeight = FontWeight.SemiBold)
                                Text(
                                    if (category.isDefault) "Default category" else "Custom category",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            IconButton(onClick = {
                                editingCategory = category
                                showCategoryDialog = true
                            }) { Icon(Icons.Default.Edit, contentDescription = "Edit ${category.name}") }
                            Icon(if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, null)
                        }
                        if (expanded) {
                            val children = subCategories[category.id].orEmpty()
                            children.forEach { subCategory ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(start = 66.dp, end = 12.dp, bottom = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(subCategory.icon, modifier = Modifier.width(28.dp))
                                    Text(subCategory.name, modifier = Modifier.weight(1f))
                                    IconButton(onClick = {
                                        subCategoryTarget = category
                                        editingSubCategory = subCategory
                                    }) { Icon(Icons.Default.Edit, contentDescription = "Edit ${subCategory.name}") }
                                }
                            }
                            TextButton(
                                onClick = {
                                    subCategoryTarget = category
                                    editingSubCategory = null
                                },
                                modifier = Modifier.padding(start = 54.dp, bottom = 6.dp)
                            ) {
                                Icon(Icons.Default.Add, null)
                                Text("Add subcategory")
                            }
                        }
                    }
                }
            }
            item { Spacer(Modifier.size(80.dp)) }
        }
    }

    if (showCategoryDialog) {
        CategoryEditorDialog(
            existing = editingCategory,
            onDismiss = { showCategoryDialog = false },
            onSave = { name, icon, color ->
                viewModel.saveCategory(editingCategory, name, icon, color)
                showCategoryDialog = false
            }
        )
    }

    subCategoryTarget?.let { category ->
        SubCategoryEditorDialog(
            categoryName = category.name,
            existing = editingSubCategory,
            onDismiss = {
                subCategoryTarget = null
                editingSubCategory = null
            },
            onSave = { name, icon ->
                viewModel.saveSubCategory(category.id, editingSubCategory, name, icon)
                subCategoryTarget = null
                editingSubCategory = null
                expandedIds = expandedIds + category.id
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CategoryEditorDialog(
    existing: Category?,
    onDismiss: () -> Unit,
    onSave: (String, String, Int) -> Unit
) {
    var name by remember(existing) { mutableStateOf(existing?.name.orEmpty()) }
    var icon by remember(existing) { mutableStateOf(existing?.icon ?: categoryIcons.first()) }
    var color by remember(existing) { mutableStateOf(existing?.color ?: categoryColors.first()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "Create category" else "Edit category") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, singleLine = true)
                Text("Icon", fontWeight = FontWeight.SemiBold)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    categoryIcons.forEach { candidate ->
                        OutlinedButton(
                            onClick = { icon = candidate },
                            border = if (icon == candidate) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(8.dp)
                        ) { Text(candidate) }
                    }
                }
                Text("Color", fontWeight = FontWeight.SemiBold)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    categoryColors.forEach { candidate ->
                        Box(
                            Modifier.size(if (color == candidate) 36.dp else 30.dp)
                                .background(Color(candidate), CircleShape)
                                .clickable { color = candidate }
                        )
                    }
                }
            }
        },
        confirmButton = { Button(onClick = { onSave(name, icon, color) }, enabled = name.isNotBlank()) { Text("Save") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SubCategoryEditorDialog(
    categoryName: String,
    existing: SubCategory?,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var name by remember(existing) { mutableStateOf(existing?.name.orEmpty()) }
    var icon by remember(existing) { mutableStateOf(existing?.icon ?: "•") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "Add to $categoryName" else "Edit subcategory") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, singleLine = true)
                Text("Icon", fontWeight = FontWeight.SemiBold)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("•", "🛒", "🥬", "🧹", "🥛", "💊", "🩺", "🏥", "🧪", "🚌", "🚆", "✈️", "🚕").forEach { candidate ->
                        OutlinedButton(onClick = { icon = candidate }, border = if (icon == candidate) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null) { Text(candidate) }
                    }
                }
            }
        },
        confirmButton = { Button(onClick = { onSave(name, icon) }, enabled = name.isNotBlank()) { Text("Save") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
