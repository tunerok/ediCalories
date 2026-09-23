package com.example.edicalories.ui.today

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.edicalories.R
import com.example.edicalories.domain.BodyWeight
import com.example.edicalories.domain.ChartPeriod
import kotlin.math.abs
import kotlin.math.roundToInt

private const val MAX_DOT_COUNT: Int = 90
private const val CHART_HEIGHT_DP: Int = 180
private const val Y_LABEL_WIDTH_DP: Int = 48

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen(
    state: ProgressUiState,
    onBack: () -> Unit,
    onPeriodChange: (ChartPeriod) -> Unit,
) {
    BackHandler(onBack = onBack)
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.progress_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Medium,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cancel),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            PeriodSelector(
                selected = state.period,
                onPeriodChange = onPeriodChange,
            )
            ProgressChartCard(
                title = stringResource(R.string.chart_calories),
                emptyText = stringResource(R.string.chart_empty_calories),
                points = state.caloriePoints,
                fromEpochDay = state.fromEpochDay,
                toEpochDay = state.toEpochDay,
                hasData = state.hasCalorieRecords,
                yMin = 0f,
                yMax = calorieYMax(state),
                guideValue = state.dailyGoal.toFloat(),
                formatY = { value -> value.roundToInt().toString() },
            )
            ProgressChartCard(
                title = stringResource(R.string.chart_weight),
                emptyText = stringResource(R.string.chart_empty_weight),
                points = state.weightPoints,
                fromEpochDay = state.fromEpochDay,
                toEpochDay = state.toEpochDay,
                hasData = state.hasWeightRecords,
                yMin = weightYMin(state.weightPoints),
                yMax = weightYMax(state.weightPoints),
                guideValue = null,
                formatY = { value ->
                    val tenths = (value * BodyWeight.TENTHS_PER_KG).roundToInt()
                    BodyWeight.formatKg(tenths)
                },
            )
        }
    }
}

@Composable
private fun PeriodSelector(
    selected: ChartPeriod,
    onPeriodChange: (ChartPeriod) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ChartPeriod.entries.forEach { period ->
            FilterChip(
                selected = selected == period,
                onClick = { onPeriodChange(period) },
                label = { Text(periodLabel(period)) },
            )
        }
    }
}

@Composable
private fun periodLabel(period: ChartPeriod): String {
    return stringResource(
        when (period) {
            ChartPeriod.Days7 -> R.string.period_7
            ChartPeriod.Days30 -> R.string.period_30
            ChartPeriod.Days90 -> R.string.period_90
            ChartPeriod.All -> R.string.period_all
        },
    )
}

@Composable
private fun ProgressChartCard(
    title: String,
    emptyText: String,
    points: List<ChartPoint>,
    fromEpochDay: Long,
    toEpochDay: Long,
    hasData: Boolean,
    yMin: Float,
    yMax: Float,
    guideValue: Float?,
    formatY: (Float) -> String,
) {
    val axisColor = MaterialTheme.colorScheme.onSurfaceVariant
    val lineColor = MaterialTheme.colorScheme.primary
    val guideColor = MaterialTheme.colorScheme.secondary
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            if (!hasData) {
                Text(
                    text = emptyText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(
                        modifier = Modifier
                            .width(Y_LABEL_WIDTH_DP.dp)
                            .height(CHART_HEIGHT_DP.dp),
                        verticalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = formatY(yMax),
                            style = MaterialTheme.typography.labelSmall,
                            color = axisColor,
                        )
                        Text(
                            text = formatY(yMin),
                            style = MaterialTheme.typography.labelSmall,
                            color = axisColor,
                        )
                    }
                    DailyLineChart(
                        points = points,
                        fromEpochDay = fromEpochDay,
                        toEpochDay = toEpochDay,
                        yMin = yMin,
                        yMax = yMax,
                        guideValue = guideValue,
                        lineColor = lineColor,
                        axisColor = axisColor.copy(alpha = 0.4f),
                        guideColor = guideColor,
                        modifier = Modifier
                            .weight(1f)
                            .height(CHART_HEIGHT_DP.dp),
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = Y_LABEL_WIDTH_DP.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    val midEpochDay = fromEpochDay + ((toEpochDay - fromEpochDay) / 2L)
                    Text(
                        text = formatEpochDayShort(fromEpochDay),
                        style = MaterialTheme.typography.labelSmall,
                        color = axisColor,
                        textAlign = TextAlign.Start,
                    )
                    Text(
                        text = formatEpochDayShort(midEpochDay),
                        style = MaterialTheme.typography.labelSmall,
                        color = axisColor,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = formatEpochDayShort(toEpochDay),
                        style = MaterialTheme.typography.labelSmall,
                        color = axisColor,
                        textAlign = TextAlign.End,
                    )
                }
                if (guideValue != null) {
                    Text(
                        text = stringResource(R.string.chart_goal) + ": ${formatY(guideValue)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = guideColor,
                    )
                }
            }
        }
    }
}

@Composable
private fun DailyLineChart(
    points: List<ChartPoint>,
    fromEpochDay: Long,
    toEpochDay: Long,
    yMin: Float,
    yMax: Float,
    guideValue: Float?,
    lineColor: Color,
    axisColor: Color,
    guideColor: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        if (width <= 0f || height <= 0f) {
            return@Canvas
        }
        val spanX = (toEpochDay - fromEpochDay).toFloat().coerceAtLeast(1f)
        val spanY = (yMax - yMin).coerceAtLeast(0.1f)
        fun xFor(epochDay: Long): Float {
            return ((epochDay - fromEpochDay).toFloat() / spanX) * width
        }
        fun yFor(value: Float): Float {
            return height - (((value - yMin) / spanY) * height)
        }
        drawLine(
            color = axisColor,
            start = Offset(0f, height),
            end = Offset(width, height),
            strokeWidth = 2f,
        )
        drawLine(
            color = axisColor,
            start = Offset(0f, 0f),
            end = Offset(0f, height),
            strokeWidth = 2f,
        )
        if (guideValue != null) {
            val guideY = yFor(guideValue)
            var dashX = 0f
            val dash = 12f
            val gap = 8f
            while (dashX < width) {
                val endX = (dashX + dash).coerceAtMost(width)
                drawLine(
                    color = guideColor,
                    start = Offset(dashX, guideY),
                    end = Offset(endX, guideY),
                    strokeWidth = 3f,
                )
                dashX = endX + gap
            }
        }
        if (points.isEmpty()) {
            return@Canvas
        }
        val path = Path()
        points.forEachIndexed { index, point ->
            val x = xFor(point.epochDay)
            val y = yFor(point.value)
            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }
        if (points.size == 1) {
            val x = xFor(points[0].epochDay)
            val y = yFor(points[0].value)
            drawCircle(color = lineColor, radius = 6f, center = Offset(x, y))
        } else {
            drawPath(
                path = path,
                color = lineColor,
                style = Stroke(width = 5f, cap = StrokeCap.Round),
            )
            if (points.size <= MAX_DOT_COUNT) {
                points.forEach { point ->
                    drawCircle(
                        color = lineColor,
                        radius = 4f,
                        center = Offset(xFor(point.epochDay), yFor(point.value)),
                    )
                }
            }
        }
    }
}

private fun calorieYMax(state: ProgressUiState): Float {
    var maxValue = state.dailyGoal.toFloat()
    for (point in state.caloriePoints) {
        if (point.value > maxValue) {
            maxValue = point.value
        }
    }
    if (maxValue <= 0f) {
        return 1f
    }
    return maxValue
}

private fun weightYMin(points: List<ChartPoint>): Float {
    if (points.isEmpty()) {
        return 0f
    }
    var minValue = points[0].value
    var maxValue = points[0].value
    for (point in points) {
        if (point.value < minValue) {
            minValue = point.value
        }
        if (point.value > maxValue) {
            maxValue = point.value
        }
    }
    return paddedLow(minValue, maxValue)
}

private fun weightYMax(points: List<ChartPoint>): Float {
    if (points.isEmpty()) {
        return 1f
    }
    var minValue = points[0].value
    var maxValue = points[0].value
    for (point in points) {
        if (point.value < minValue) {
            minValue = point.value
        }
        if (point.value > maxValue) {
            maxValue = point.value
        }
    }
    return paddedHigh(minValue, maxValue)
}

private fun paddedLow(minValue: Float, maxValue: Float): Float {
    val pad = yPad(minValue, maxValue)
    return (minValue - pad).coerceAtLeast(0f)
}

private fun paddedHigh(minValue: Float, maxValue: Float): Float {
    return maxValue + yPad(minValue, maxValue)
}

private fun yPad(minValue: Float, maxValue: Float): Float {
    val span = abs(maxValue - minValue)
    if (span <= 0.01f) {
        return 1f
    }
    return span * 0.08f
}
