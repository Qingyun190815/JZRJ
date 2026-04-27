package com.minimalledger.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.minimalledger.app.data.model.TransactionType
import com.minimalledger.app.ui.theme.ExpenseRed
import com.minimalledger.app.ui.theme.IncomeGreen
import com.minimalledger.app.ui.theme.LedgerBorder

@Composable
fun TransactionTypeToggle(
    selectedType: TransactionType,
    onSelected: (TransactionType) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(LedgerBorder)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        TypeToggleButton(
            text = "\u652F\u51FA",
            selected = selectedType == TransactionType.EXPENSE,
            selectedColor = ExpenseRed,
            modifier = Modifier.weight(1f),
        ) { onSelected(TransactionType.EXPENSE) }
        TypeToggleButton(
            text = "\u6536\u5165",
            selected = selectedType == TransactionType.INCOME,
            selectedColor = IncomeGreen,
            modifier = Modifier.weight(1f),
        ) { onSelected(TransactionType.INCOME) }
    }
}

@Composable
private fun TypeToggleButton(
    text: String,
    selected: Boolean,
    selectedColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    androidx.compose.material3.Surface(
        onClick = onClick,
        modifier = modifier,
        color = if (selected) selectedColor else MaterialTheme.colorScheme.surface,
        contentColor = if (selected) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurface,
        shape = RoundedCornerShape(20.dp),
    ) {
        Text(
            text = text,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
        )
    }
}
