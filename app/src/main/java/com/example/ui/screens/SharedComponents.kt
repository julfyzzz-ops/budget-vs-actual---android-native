package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.ui.theme.GlassTheme
import com.example.ui.theme.GlassTheme.glassyCard
import com.example.ui.viewmodels.MainViewModel

@Composable
fun CircleIcon(icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(color),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
    }
}

@Composable
fun EditActionButtons(
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleVisibility: (() -> Unit)? = null,
    isHidden: Boolean = false
) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        if (onToggleVisibility != null) {
            IconButton(onClick = onToggleVisibility, modifier = Modifier.size(32.dp)) {
                Icon(if (isHidden) Icons.Filled.VisibilityOff else Icons.Filled.Visibility, contentDescription = "Видимість", modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Filled.Edit, contentDescription = "Редагувати", modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Filled.Delete, contentDescription = "Видалити", modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
fun HeaderMonthYearSelector(viewModel: MainViewModel) {
    val month by viewModel.currentMonth.collectAsState()
    val year by viewModel.currentYear.collectAsState()
    val isEditMode by viewModel.isEditMode.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    
    val monthNames = listOf("Січень", "Лютий", "Березень", "Квітень", "Травень", "Червень", "Липень", "Серпень", "Вересень", "Жовтень", "Листопад", "Грудень")
    
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.previousMonth() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Попередній місяць")
            }
            Text(
                text = "${monthNames[month]} $year",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            IconButton(onClick = { viewModel.nextMonth() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Наступний місяць")
            }
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val surfaceModifier = if (themeMode == 3) {
                Modifier
                    .padding(top = 8.dp)
                    .glassyCard(RoundedCornerShape(8.dp))
            } else {
                Modifier.padding(top = 8.dp)
            }
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (themeMode == 3) Color.Transparent else MaterialTheme.colorScheme.surfaceVariant,
                modifier = surfaceModifier
            ) {
                Row {
                    IconButton(onClick = { /* TODO Filter */ }) {
                        Icon(Icons.Filled.FilterList, contentDescription = "Фільтр", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = { viewModel.toggleEditMode() }) {
                        Icon(
                            if (isEditMode) Icons.Filled.LockOpen else Icons.Filled.Lock,
                            contentDescription = "Режим редагування",
                            tint = if (isEditMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
