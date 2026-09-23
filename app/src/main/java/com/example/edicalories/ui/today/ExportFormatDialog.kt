package com.example.edicalories.ui.today

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.edicalories.R

@Composable
fun ExportFormatDialog(
    onExportJson: () -> Unit,
    onExportCsv: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.export_data_title)) },
        text = {
            Column {
                TextButton(
                    onClick = onExportJson,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.export_format_native))
                }
                TextButton(
                    onClick = onExportCsv,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.export_format_csv))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
    )
}
