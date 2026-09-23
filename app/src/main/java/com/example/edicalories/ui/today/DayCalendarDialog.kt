package com.example.edicalories.ui.today

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.edicalories.R
import com.example.edicalories.domain.MonthGrid
import com.example.edicalories.ui.theme.LeafGreen
import com.example.edicalories.ui.theme.LeafGreenLight
import com.example.edicalories.ui.theme.OverRed
import com.example.edicalories.ui.theme.OverRedLight
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayCalendarDialog(
    selectedEpochDay: Long,
    dailyGoal: Int,
    dayTotals: Map<Long, Int>,
    onVisibleRangeChange: (fromEpochDay: Long, toEpochDay: Long) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: (epochDay: Long) -> Unit,
) {
    val firstDayOfWeek = DayOfWeek.MONDAY
    var yearMonth by remember {
        mutableStateOf(YearMonth.from(LocalDate.ofEpochDay(selectedEpochDay)))
    }
    val firstEpochDay = MonthGrid.firstEpochDay(yearMonth, firstDayOfWeek)
    val lastEpochDay = MonthGrid.lastEpochDay(yearMonth, firstDayOfWeek)
    LaunchedEffect(firstEpochDay, lastEpochDay) {
        onVisibleRangeChange(firstEpochDay, lastEpochDay)
    }

    val maxDialogHeight = LocalConfiguration.current.screenHeightDp.dp * 0.9f
    val todayEpochDay = LocalDate.now().toEpochDay()
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .heightIn(max = maxDialogHeight),
            shape = DatePickerDefaults.shape,
            tonalElevation = 6.dp,
            color = MaterialTheme.colorScheme.surface,
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 16.dp),
            ) {
                CalendarHeader(
                    yearMonth = yearMonth,
                    onPreviousMonth = { yearMonth = yearMonth.minusMonths(1L) },
                    onNextMonth = { yearMonth = yearMonth.plusMonths(1L) },
                )
                WeekdayHeader(firstDayOfWeek = firstDayOfWeek)
                val weekCount = MonthGrid.CELL_COUNT / MonthGrid.DAYS_IN_WEEK
                for (weekIndex in 0 until weekCount) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        for (dayIndex in 0 until MonthGrid.DAYS_IN_WEEK) {
                            val offset = (weekIndex * MonthGrid.DAYS_IN_WEEK) + dayIndex
                            val epochDay = firstEpochDay + offset.toLong()
                            CalendarDayCell(
                                epochDay = epochDay,
                                yearMonth = yearMonth,
                                selectedEpochDay = selectedEpochDay,
                                todayEpochDay = todayEpochDay,
                                consumed = dayTotals[epochDay],
                                dailyGoal = dailyGoal,
                                onClick = { onConfirm(epochDay) },
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarHeader(
    yearMonth: YearMonth,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onPreviousMonth) {
            Icon(
                imageVector = Icons.Filled.ChevronLeft,
                contentDescription = stringResource(R.string.previous_month),
            )
        }
        Text(
            text = formatYearMonth(yearMonth),
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
        )
        IconButton(onClick = onNextMonth) {
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = stringResource(R.string.next_month),
            )
        }
    }
}

@Composable
private fun WeekdayHeader(firstDayOfWeek: DayOfWeek) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 4.dp),
    ) {
        for (offset in 0 until MonthGrid.DAYS_IN_WEEK) {
            val dayOfWeek = firstDayOfWeek.plus(offset.toLong())
            Text(
                text = formatWeekdayShort(dayOfWeek),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Clip,
            )
        }
    }
}

@Composable
private fun CalendarDayCell(
    epochDay: Long,
    yearMonth: YearMonth,
    selectedEpochDay: Long,
    todayEpochDay: Long,
    consumed: Int?,
    dailyGoal: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val date = LocalDate.ofEpochDay(epochDay)
    val inMonth = YearMonth.from(date) == yearMonth
    val selected = epochDay == selectedEpochDay
    val isToday = epochDay == todayEpochDay
    val shownCalories = if (consumed != null && consumed > 0) {
        consumed
    } else {
        null
    }
    val dayNumberColor = when {
        selected -> MaterialTheme.colorScheme.onPrimary
        inMonth -> MaterialTheme.colorScheme.onSurface
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val cellDescription = if (shownCalories != null) {
        stringResource(
            R.string.calendar_day_description,
            formatEpochDay(epochDay),
            shownCalories,
        )
    } else {
        formatEpochDay(epochDay)
    }

    Column(
        modifier = modifier
            .semantics(mergeDescendants = true) { contentDescription = cellDescription }
            .clickable(role = Role.Button, onClick = onClick)
            .padding(vertical = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(
                    if (selected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        Color.Transparent
                    },
                )
                .then(
                    if (isToday && !selected) {
                        Modifier.border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.primary,
                            shape = CircleShape,
                        )
                    } else {
                        Modifier
                    },
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = date.dayOfMonth.toString(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (selected || isToday) FontWeight.SemiBold else FontWeight.Normal,
                color = dayNumberColor,
            )
        }
        if (shownCalories != null) {
            Text(
                text = shownCalories.toString(),
                color = consumedColor(consumed = shownCalories, dailyGoal = dailyGoal),
                fontSize = 10.sp,
                lineHeight = 12.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Clip,
            )
        } else {
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun consumedColor(consumed: Int, dailyGoal: Int): Color {
    val overGoal = consumed > dailyGoal
    val darkTheme = isSystemInDarkTheme()
    return if (overGoal) {
        if (darkTheme) OverRedLight else OverRed
    } else {
        if (darkTheme) LeafGreenLight else LeafGreen
    }
}
