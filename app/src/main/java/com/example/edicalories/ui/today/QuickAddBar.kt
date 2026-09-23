package com.example.edicalories.ui.today

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.edicalories.R
import com.example.edicalories.domain.BodyWeight

private val QUICK_ADD_VALUES: List<Int> = listOf(50, 100, 250, 500)
private val PANEL_WIDTH = 300.dp
private const val SCRIM_ALPHA: Float = 0.45f
private const val CUSTOM_CALORIES_MAX_DIGITS: Int = 5

@Composable
fun QuickAddOverlay(
    visible: Boolean,
    selectedEpochDay: Long,
    existingWeightTenths: Int?,
    onDismiss: () -> Unit,
    onQuickAdd: (Int) -> Unit,
    onSave: (caloriesRaw: String, weightRaw: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    BackHandler(enabled = visible, onBack = onDismiss)

    var caloriesRaw by remember { mutableStateOf("") }
    var weightRaw by remember { mutableStateOf("") }
    var caloriesFocused by remember { mutableStateOf(false) }
    var weightFocused by remember { mutableStateOf(false) }
    val caloriesSaveRequester = remember { BringIntoViewRequester() }
    val weightScrollState = rememberScrollState()
    val imeBottom = WindowInsets.ime.getBottom(LocalDensity.current)
    LaunchedEffect(caloriesFocused, imeBottom, visible) {
        if (!visible || !caloriesFocused) {
            return@LaunchedEffect
        }
        snapshotFlow { weightScrollState.maxValue }.collect {
            caloriesSaveRequester.bringIntoView()
        }
    }
    LaunchedEffect(weightFocused, imeBottom, visible) {
        if (!visible || !weightFocused) {
            return@LaunchedEffect
        }
        snapshotFlow { weightScrollState.maxValue }.collect { maxValue ->
            weightScrollState.scrollTo(maxValue)
        }
    }
    LaunchedEffect(visible) {
        if (visible) {
            caloriesRaw = ""
            weightRaw = existingWeightTenths?.let { tenths ->
                BodyWeight.formatKg(tenths)
            }.orEmpty()
        }
    }
    LaunchedEffect(visible, existingWeightTenths) {
        if (visible && existingWeightTenths != null) {
            weightRaw = BodyWeight.formatKg(existingWeightTenths)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = SCRIM_ALPHA))
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = onDismiss,
                    ),
            )
        }

        AnimatedVisibility(
            visible = visible,
            modifier = Modifier.align(Alignment.CenterEnd),
            enter = slideInHorizontally { fullWidth -> fullWidth },
            exit = slideOutHorizontally { fullWidth -> fullWidth },
        ) {
            Surface(
                modifier = Modifier
                    .width(PANEL_WIDTH)
                    .fillMaxHeight()
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = {},
                    ),
                tonalElevation = 6.dp,
                shadowElevation = 8.dp,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding()
                        .imePadding()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(R.string.add_menu_title),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f),
                        )
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = stringResource(R.string.close_add_menu),
                            )
                        }
                    }
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(weightScrollState),
                        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Bottom),
                    ) {
                        QUICK_ADD_VALUES.forEach { value ->
                            FilledTonalButton(
                                onClick = { onQuickAdd(value) },
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text(text = value.toString())
                            }
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        Text(
                            text = stringResource(R.string.add_custom),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        OutlinedTextField(
                            value = caloriesRaw,
                            onValueChange = { incoming ->
                                caloriesRaw = incoming.filter { char -> char.isDigit() }
                                    .take(CUSTOM_CALORIES_MAX_DIGITS)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .bringIntoViewRequester(caloriesSaveRequester)
                                .onFocusChanged { focusState ->
                                    caloriesFocused = focusState.isFocused
                                },
                            label = { Text(stringResource(R.string.calories_label)) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done,
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = { onSave(caloriesRaw, weightRaw) },
                            ),
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        Text(
                            text = stringResource(
                                R.string.weight_for_date,
                                formatEpochDay(selectedEpochDay),
                            ),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        if (existingWeightTenths != null) {
                            Text(
                                text = stringResource(
                                    R.string.current_weight,
                                    BodyWeight.formatKg(existingWeightTenths),
                                ),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        OutlinedTextField(
                            value = weightRaw,
                            onValueChange = { incoming ->
                                weightRaw = BodyWeight.sanitizeInput(incoming)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .onFocusChanged { focusState ->
                                    weightFocused = focusState.isFocused
                                },
                            label = { Text(stringResource(R.string.weight_label)) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Decimal,
                                imeAction = ImeAction.Done,
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = { onSave(caloriesRaw, weightRaw) },
                            ),
                        )
                    }
                    Button(
                        onClick = { onSave(caloriesRaw, weightRaw) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                    ) {
                        Text(text = stringResource(R.string.save))
                    }
                }
            }
        }
    }
}
