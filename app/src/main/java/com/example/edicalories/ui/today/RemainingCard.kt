package com.example.edicalories.ui.today

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.edicalories.R
import com.example.edicalories.domain.BodyWeight
import com.example.edicalories.domain.CalorieBalance
import com.example.edicalories.ui.theme.ExactYellow
import com.example.edicalories.ui.theme.ExactYellowLight
import kotlin.math.abs

@Composable
fun RemainingCard(
    state: DaySnapshot,
    modifier: Modifier = Modifier,
) {
    val nearGoal = CalorieBalance.isNearGoal(state.dailyGoal, state.consumed)
    val accent = when {
        nearGoal -> if (isSystemInDarkTheme()) ExactYellowLight else ExactYellow
        state.isOver -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.primary
    }
    val statusLabel = when {
        nearGoal -> stringResource(R.string.remaining_status_exact)
        state.isOver -> stringResource(R.string.remaining_status_over)
        else -> stringResource(R.string.remaining_status_left)
    }
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Box(
                modifier = Modifier.size(132.dp),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(
                    progress = { state.progress },
                    modifier = Modifier.size(132.dp),
                    color = accent,
                    strokeWidth = 10.dp,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    strokeCap = ProgressIndicatorDefaults.CircularDeterminateStrokeCap,
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = statusLabel,
                        style = MaterialTheme.typography.labelMedium,
                        color = accent,
                    )
                    Text(
                        text = abs(state.remaining).toString(),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = accent,
                    )
                    Text(
                        text = stringResource(R.string.kcal),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(
                        R.string.consumed_of_goal,
                        state.consumed,
                        state.dailyGoal,
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = stringResource(R.string.daily_goal_label),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                val priorWeightTenths = state.priorWeightTenths
                if (priorWeightTenths != null) {
                    Text(
                        text = stringResource(
                            R.string.card_weight,
                            BodyWeight.formatKg(priorWeightTenths),
                        ),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}
