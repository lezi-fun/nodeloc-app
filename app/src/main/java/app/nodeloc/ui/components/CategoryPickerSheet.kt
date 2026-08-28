package app.nodeloc.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.nodeloc.data.model.CategoryDto
import app.nodeloc.ui.theme.LocalNodelocColors

/**
 * 对齐官网创建话题时的节点选择器:自带搜索框按名称实时过滤,不用像下拉列表那样一直往下滑找。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryPickerSheet(categories: List<CategoryDto>, onDismiss: () -> Unit, onPick: (CategoryDto) -> Unit) {
    val nc = LocalNodelocColors.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var query by remember { mutableStateOf("") }
    val filtered = remember(categories, query) {
        if (query.isBlank()) categories else categories.filter { it.name.contains(query, ignoreCase = true) }
    }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState, containerColor = nc.background) {
        Column(Modifier.fillMaxSize().heightIn(min = 400.dp)) {
            Text(
                "选择节点",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                color = nc.onBackground,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("搜索节点", color = nc.onSurfaceVariant) },
                singleLine = true,
                leadingIcon = { Icon(Icons.Filled.Search, null, tint = nc.onSurfaceVariant) },
                shape = RoundedCornerShape(999.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = nc.outlineVariant,
                    focusedBorderColor = nc.primary,
                ),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
            )
            HorizontalDivider(color = nc.outlineVariant, modifier = Modifier.padding(top = 8.dp))
            if (filtered.isEmpty()) {
                androidx.compose.foundation.layout.Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("没有找到匹配的节点", color = nc.onSurfaceVariant)
                }
            } else {
                LazyColumn(Modifier.fillMaxSize()) {
                    items(filtered, key = { it.id }) { cat ->
                        Surface(onClick = { onPick(cat) }, color = nc.background, modifier = Modifier.fillMaxWidth()) {
                            androidx.compose.foundation.layout.Row(
                                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                CategoryDot(cat, size = 18.dp)
                                androidx.compose.foundation.layout.Spacer(Modifier.width(10.dp))
                                Text(cat.name, style = MaterialTheme.typography.bodyMedium, color = nc.onBackground)
                            }
                        }
                    }
                }
            }
        }
    }
}
