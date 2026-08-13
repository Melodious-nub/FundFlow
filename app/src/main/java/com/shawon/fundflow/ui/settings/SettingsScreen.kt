package com.shawon.fundflow.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateToAbout: () -> Unit,
    onNavigateToCycleManagement: () -> Unit,
    onNavigateToBackup: () -> Unit,
    onNavigateToUpdate: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val currency by viewModel.currencyCode.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()

    var showCurrencySheet by remember { mutableStateOf(false) }
    var showThemeSheet by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Preferences",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))

            SettingItem(
                title = "Currency",
                subtitle = "Active: $currency",
                icon = Icons.Default.Payments,
                onClick = { showCurrencySheet = true }
            )

            Spacer(modifier = Modifier.height(12.dp))

            SettingItem(
                title = "Manage Cycles",
                subtitle = "View, edit or delete budget cycles",
                icon = Icons.Default.History,
                onClick = onNavigateToCycleManagement
            )

            Spacer(modifier = Modifier.height(12.dp))

            SettingItem(
                title = "Theme",
                subtitle = "Mode: ${themeMode.lowercase().replaceFirstChar { it.uppercase() }.replace("System", "System Default")}",
                icon = Icons.Default.Palette,
                onClick = { showThemeSheet = true }
            )

            Spacer(modifier = Modifier.height(12.dp))

            SettingItem(
                title = "Backup & Restore",
                subtitle = "Local backup of your data",
                icon = Icons.Default.Storage,
                onClick = onNavigateToBackup
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Application",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))

            SettingItem(
                title = "App Updates",
                subtitle = "Check for the latest version",
                icon = Icons.Default.Update,
                onClick = onNavigateToUpdate
            )

            Spacer(modifier = Modifier.height(12.dp))

            SettingItem(
                title = "About FundFlow",
                subtitle = "App details & Developer info",
                icon = Icons.Default.Info,
                onClick = onNavigateToAbout
            )
            
            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    if (showCurrencySheet) {
        ModalBottomSheet(
            onDismissRequest = { showCurrencySheet = false },
            sheetState = sheetState
        ) {
            CurrencySelectionContent(
                selectedCurrency = currency,
                onCurrencySelected = { code ->
                    scope.launch {
                        viewModel.updateCurrency(code)
                        sheetState.hide()
                    }.invokeOnCompletion {
                        if (!sheetState.isVisible) {
                            showCurrencySheet = false
                        }
                    }
                }
            )
        }
    }

    if (showThemeSheet) {
        ModalBottomSheet(
            onDismissRequest = { showThemeSheet = false },
            sheetState = sheetState
        ) {
            ThemeSelectionContent(
                selectedTheme = themeMode,
                onThemeSelected = { mode ->
                    scope.launch {
                        viewModel.updateTheme(mode)
                        sheetState.hide()
                    }.invokeOnCompletion {
                        if (!sheetState.isVisible) {
                            showThemeSheet = false
                        }
                    }
                }
            )
        }
    }
}

@Composable
private fun SettingItem(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    androidx.compose.material3.Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ThemeSelectionContent(
    selectedTheme: String,
    onThemeSelected: (String) -> Unit
) {
    val themes = listOf(
        "SYSTEM" to "System Default",
        "LIGHT" to "Light Mode",
        "DARK" to "Dark Mode"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp)
    ) {
        Text(
            text = "Select Theme",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp)
        )
        HorizontalDivider()
        themes.forEach { (mode, name) ->
            ListItem(
                headlineContent = { Text(name) },
                leadingContent = {
                    RadioButton(
                        selected = selectedTheme == mode,
                        onClick = { onThemeSelected(mode) }
                    )
                },
                modifier = Modifier.clickable { onThemeSelected(mode) }
            )
        }
    }
}

@Composable
private fun CurrencySelectionContent(
    selectedCurrency: String,
    onCurrencySelected: (String) -> Unit
) {
    val currencies = listOf(
        "TK" to "Bangladeshi Taka (৳)",
        "USD" to "US Dollar ($)",
        "EUR" to "Euro (€)",
        "GBP" to "British Pound (£)",
        "INR" to "Indian Rupee (₹)"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp)
    ) {
        Text(
            text = "Select Currency",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp)
        )
        HorizontalDivider()
        currencies.forEach { (code, name) ->
            ListItem(
                headlineContent = { Text(name) },
                leadingContent = {
                    RadioButton(
                        selected = selectedCurrency == code,
                        onClick = { onCurrencySelected(code) }
                    )
                },
                modifier = Modifier.clickable { onCurrencySelected(code) }
            )
        }
    }
}
