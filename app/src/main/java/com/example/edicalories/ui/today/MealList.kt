package com.example.edicalories.ui.today

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.edicalories.R
import com.example.edicalories.data.Meal
import com.example.edicalories.domain.MealGroup
import com.example.edicalories.domain.MealSchedule
import com.example.edicalories.domain.MealSlot
import com.example.edicalories.domain.MinutesOfDay

@Composable
fun MealList(
    meals: List<Meal>,
    schedule: MealSchedule,
    onMealClick: (Meal) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
) {
    if (meals.isEmpty()) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(contentPadding)
                .padding(horizontal = 24.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.empty_day),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        return
    }

    val groups = remember(meals, schedule) {
        if (schedule.groupingEnabled) {
            MealSchedule.groupMeals(meals, schedule)
        } else {
            emptyList()
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (!schedule.groupingEnabled) {
            mealRows(meals = meals, onMealClick = onMealClick)
        } else {
            groupedMealRows(
                meals = meals,
                groups = groups,
                onMealClick = onMealClick,
            )
        }
    }
}

private fun LazyListScope.mealRows(
    meals: List<Meal>,
    onMealClick: (Meal) -> Unit,
) {
    itemsIndexed(meals, key = { _, meal -> meal.id }) { index, meal ->
        MealRow(
            index = index + 1,
            meal = meal,
            onClick = { onMealClick(meal) },
        )
    }
}

private fun LazyListScope.groupedMealRows(
    meals: List<Meal>,
    groups: List<MealGroup>,
    onMealClick: (Meal) -> Unit,
) {
    val indexById = HashMap<Long, Int>(meals.size)
    meals.forEachIndexed { index, meal ->
        indexById[meal.id] = index + 1
    }
    groups.forEach { group ->
        val headerKey = group.slot?.name ?: "untimed"
        item(key = "header-$headerKey") {
            MealGroupHeader(
                slot = group.slot,
                calories = group.calories,
            )
        }
        items(group.meals, key = { meal -> meal.id }) { meal ->
            MealRow(
                index = indexById[meal.id] ?: 0,
                meal = meal,
                onClick = { onMealClick(meal) },
            )
        }
    }
}

@Composable
private fun MealGroupHeader(
    slot: MealSlot?,
    calories: Int,
) {
    val title = when (slot) {
        MealSlot.Breakfast -> stringResource(R.string.meal_slot_breakfast)
        MealSlot.Lunch -> stringResource(R.string.meal_slot_lunch)
        MealSlot.Dinner -> stringResource(R.string.meal_slot_dinner)
        null -> stringResource(R.string.meal_slot_untimed)
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = "$calories ${stringResource(R.string.kcal)}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun MealRow(
    index: Int,
    meal: Meal,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val minutesOfDay = meal.minutesOfDay
            val timeLabel = if (minutesOfDay != null) {
                MinutesOfDay.format(minutesOfDay)
            } else {
                index.toString()
            }
            Text(
                text = timeLabel,
                modifier = Modifier.widthIn(min = 48.dp),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = meal.calories.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = stringResource(R.string.kcal),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
