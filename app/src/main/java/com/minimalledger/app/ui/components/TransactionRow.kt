package com.minimalledger.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowCircleDown
import androidx.compose.material.icons.rounded.ArrowCircleUp
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.minimalledger.app.data.model.Transaction
import com.minimalledger.app.data.model.TransactionType
import com.minimalledger.app.ui.theme.ExpenseRed
import com.minimalledger.app.ui.theme.IncomeGreen
import com.minimalledger.app.utils.formatCurrency
import com.minimalledger.app.utils.formatTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionRow(
    transaction: Transaction,
    assetName: String?,
    onDelete: (Transaction) -> Unit,
    onClick: (Transaction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value != SwipeToDismissBoxValue.Settled) {
                onDelete(transaction)
                true
            } else {
                true
            }
        },
    )

    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier,
        backgroundContent = {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .background(ExpenseRed, RoundedCornerShape(24.dp))
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Delete,
                    contentDescription = "\u5220\u9664",
                    tint = Color.White,
                )
            }
        },
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick(transaction) },
            shape = RoundedCornerShape(24.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    val typeColor = if (transaction.type == TransactionType.INCOME) IncomeGreen else ExpenseRed
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(typeColor.copy(alpha = 0.14f), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = if (transaction.type == TransactionType.INCOME) {
                                Icons.Rounded.ArrowCircleUp
                            } else {
                                Icons.Rounded.ArrowCircleDown
                            },
                            contentDescription = null,
                            tint = typeColor,
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = transaction.note.ifBlank {
                                if (transaction.type == TransactionType.INCOME) {
                                    "\u6536\u5165"
                                } else {
                                    "\u652F\u51FA"
                                }
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = listOfNotNull(
                                formatTime(transaction.timestamp),
                                assetName?.let { "\u8D44\u4EA7\uFF1A$it" },
                            ).joinToString("  "),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        )
                    }
                }
                Text(
                    text = formatCurrency(transaction.amountInCents),
                    style = MaterialTheme.typography.titleMedium,
                    color = if (transaction.type == TransactionType.INCOME) IncomeGreen else ExpenseRed,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}
