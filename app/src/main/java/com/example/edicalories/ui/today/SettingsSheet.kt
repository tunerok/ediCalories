package com.example.edicalories.ui.today

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.edicalories.R
import com.example.edicalories.domain.AppThemeMode
import com.example.edicalories.domain.MealSchedule
import com.example.edicalories.domain.MinutesOfDay

private enum class MealWindowField {
    Breakfast,
    Lunch,
    Dinner,
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSheet(
    currentGoal: Int,
    currentSchedule: MealSchedule,
    currentLanguage: AppLanguage,
    currentTheme: AppThemeMode,
    onDismiss: () -> Unit,
    onSave: (goalRaw: String, schedule: MealSchedule) -> Unit,
    onLanguageChange: (AppLanguage) -> Unit,
    onThemeChange: (AppThemeMode) -> Unit,
    onExportJson: () -> Unit,
    onExportCsv: () -> Unit,
    onImport: () -> Unit,
    onClearJournal: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var goalRaw by remember { mutableStateOf(currentGoal.toString()) }
    var groupingEnabled by remember { mutableStateOf(currentSchedule.groupingEnabled) }
    var breakfastStart by remember { mutableIntStateOf(currentSchedule.breakfastStart) }
    var lunchStart by remember { mutableIntStateOf(currentSchedule.lunchStart) }
    var dinnerStart by remember { mutableIntStateOf(currentSchedule.dinnerStart) }
    var windowField by remember { mutableStateOf<MealWindowField?>(null) }
    var showClearConfirm by remember { mutableStateOf(false) }
    var showExportFormat by remember { mutableStateOf(false) }
    var showImportConfirm by remember { mutableStateOf(false) }
    var languageExpanded by remember { mutableStateOf(false) }
    var themeExpanded by remember { mutableStateOf(false) }
    val languageLabel = when (currentLanguage) {
        AppLanguage.System -> stringResource(R.string.language_system)
        AppLanguage.English -> "English"
        AppLanguage.Russian -> "Русский"
    }
    val themeLabel = when (currentTheme) {
        AppThemeMode.System -> stringResource(R.string.theme_system)
        AppThemeMode.Light -> stringResource(R.string.theme_light)
        AppThemeMode.Dark -> stringResource(R.string.theme_dark)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 28.dp),
        ) {
            Text(
                text = stringResource(R.string.settings),
                style = MaterialTheme.typography.titleLarge,
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = goalRaw,
                onValueChange = { incoming ->
                    goalRaw = incoming.filter { char -> char.isDigit() }.take(5)
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.daily_goal_hint)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = stringResource(R.string.meal_list_mode_title),
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Column(modifier = Modifier.selectableGroup()) {
                MealListModeRow(
                    label = stringResource(R.string.meal_list_mode_flat),
                    selected = !groupingEnabled,
                    onClick = { groupingEnabled = false },
                )
                MealListModeRow(
                    label = stringResource(R.string.meal_list_mode_grouped),
                    selected = groupingEnabled,
                    onClick = { groupingEnabled = true },
                )
            }
            if (groupingEnabled) {
                Spacer(modifier = Modifier.height(12.dp))
                MealWindowRow(
                    label = stringResource(R.string.breakfast_start),
                    minutesOfDay = breakfastStart,
                    onClick = { windowField = MealWindowField.Breakfast },
                )
                MealWindowRow(
                    label = stringResource(R.string.lunch_start),
                    minutesOfDay = lunchStart,
                    onClick = { windowField = MealWindowField.Lunch },
                )
                MealWindowRow(
                    label = stringResource(R.string.dinner_start),
                    minutesOfDay = dinnerStart,
                    onClick = { windowField = MealWindowField.Dinner },
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            SettingsDropdown(
                label = stringResource(R.string.language_title),
                selected = languageLabel,
                expanded = languageExpanded,
                onExpandedChange = { expanded ->
                    languageExpanded = expanded
                    if (expanded) {
                        themeExpanded = false
                    }
                },
                options = listOf(
                    SettingsOption(stringResource(R.string.language_system)) {
                        onLanguageChange(AppLanguage.System)
                    },
                    SettingsOption("English") {
                        onLanguageChange(AppLanguage.English)
                    },
                    SettingsOption("Русский") {
                        onLanguageChange(AppLanguage.Russian)
                    },
                ),
            )
            Spacer(modifier = Modifier.height(12.dp))
            SettingsDropdown(
                label = stringResource(R.string.theme_title),
                selected = themeLabel,
                expanded = themeExpanded,
                onExpandedChange = { expanded ->
                    themeExpanded = expanded
                    if (expanded) {
                        languageExpanded = false
                    }
                },
                options = listOf(
                    SettingsOption(stringResource(R.string.theme_system)) {
                        onThemeChange(AppThemeMode.System)
                    },
                    SettingsOption(stringResource(R.string.theme_light)) {
                        onThemeChange(AppThemeMode.Light)
                    },
                    SettingsOption(stringResource(R.string.theme_dark)) {
                        onThemeChange(AppThemeMode.Dark)
                    },
                ),
            )
            Spacer(modifier = Modifier.height(20.dp))
            OutlinedButton(
                onClick = { showExportFormat = true },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.export_data))
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = { showImportConfirm = true },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.import_data))
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = { showClearConfirm = true },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.clear_data))
            }
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(stringResource(R.string.cancel))
                }
                Button(
                    onClick = {
                        onSave(
                            goalRaw,
                            MealSchedule(
                                groupingEnabled = groupingEnabled,
                                breakfastStart = breakfastStart,
                                lunchStart = lunchStart,
                                dinnerStart = dinnerStart,
                            ),
                        )
                    },
                    modifier = Modifier.weight(1f),
                ) {
                    Text(stringResource(R.string.save))
                }
            }
        }
    }

    if (showExportFormat) {
        ExportFormatDialog(
            onExportJson = {
                showExportFormat = false
                onExportJson()
            },
            onExportCsv = {
                showExportFormat = false
                onExportCsv()
            },
            onDismiss = { showExportFormat = false },
        )
    }

    if (showImportConfirm) {
        ImportJournalDialog(
            onConfirm = {
                showImportConfirm = false
                onImport()
            },
            onDismiss = { showImportConfirm = false },
        )
    }

    if (showClearConfirm) {
        ClearJournalDialog(
            onConfirm = {
                showClearConfirm = false
                onClearJournal()
            },
            onDismiss = { showClearConfirm = false },
        )
    }

    val editingField = windowField
    if (editingField != null) {
        val currentMinutes = when (editingField) {
            MealWindowField.Breakfast -> breakfastStart
            MealWindowField.Lunch -> lunchStart
            MealWindowField.Dinner -> dinnerStart
        }
        MealWindowPickerDialog(
            minutesOfDay = currentMinutes,
            onDismiss = { windowField = null },
            onConfirm = { minutes ->
                when (editingField) {
                    MealWindowField.Breakfast -> breakfastStart = minutes
                    MealWindowField.Lunch -> lunchStart = minutes
                    MealWindowField.Dinner -> dinnerStart = minutes
                }
                windowField = null
            },
        )
    }
}

private class SettingsOption(
    val label: String,
    val onSelect: () -> Unit,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsDropdown(
    label: String,
    selected: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    options: List<SettingsOption>,
) {
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = onExpandedChange,
        modifier = Modifier.fillMaxWidth(),
    ) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) },
        ) {
            for (option in options) {
                DropdownMenuItem(
                    text = {
                        Text(
                            text = option.label,
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    },
                    onClick = {
                        onExpandedChange(false)
                        option.onSelect()
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )
            }
        }
    }
}

@Composable
private fun MealListModeRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton,
            )
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = selected,
            onClick = null,
        )
        Text(
            text = label,
            modifier = Modifier.padding(start = 8.dp),
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
private fun MealWindowRow(
    label: String,
    minutesOfDay: Int,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
        )
        Text(
            text = MinutesOfDay.format(minutesOfDay),
            style = MaterialTheme.typography.titleMedium,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MealWindowPickerDialog(
    minutesOfDay: Int,
    onDismiss: () -> Unit,
    onConfirm: (minutesOfDay: Int) -> Unit,
) {
    val safe = minutesOfDay.coerceIn(MinutesOfDay.MIN_INCLUSIVE, MinutesOfDay.MAX_INCLUSIVE)
    val pickerState = rememberTimePickerState(
        initialHour = safe / MinutesOfDay.MINUTES_PER_HOUR,
        initialMinute = safe % MinutesOfDay.MINUTES_PER_HOUR,
        is24Hour = true,
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val minutes = (pickerState.hour * MinutesOfDay.MINUTES_PER_HOUR) + pickerState.minute
                    onConfirm(minutes)
                },
            ) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
        title = { Text(stringResource(R.string.pick_meal_window)) },
        text = { TimePicker(state = pickerState) },
    )
}
