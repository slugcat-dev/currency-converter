package com.android.converter.ui.screens

import android.icu.text.DecimalFormatSymbols
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.automirrored.outlined.Backspace
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.android.converter.data.model.Currency
import com.android.converter.ui.AppState
import com.android.converter.ui.AppViewModel

@Composable
fun CurrencyConverterScreen(
    viewModel: AppViewModel,
    uiState: AppState,
    navController: NavController
) {
    if (uiState.ready.not())
        return

    val fromCurrency = requireNotNull(uiState.fromCurrency)
    val toCurrency = requireNotNull(uiState.toCurrency)
    val fromAmount = uiState.fromAmount
    val toAmount = uiState.toAmount

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CurrencyRow(
                    currency = fromCurrency,
                    amount = fromAmount
                ) { navController.navigate("selection/from") }
                FilledIconButton(
                    modifier = Modifier.rotate(90f),
                    onClick = { viewModel.swapCurrencies() },
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.CompareArrows,
                        contentDescription = "Swap currencies"
                    )
                }
                CurrencyRow(
                    currency = toCurrency,
                    amount = toAmount
                ) { navController.navigate("selection/to") }
            }
            Keypad(uiState) { key -> viewModel.onKeyPress(key) }
        }
    }
}

@Composable
fun CurrencyRow(
    currency: Currency,
    amount: String,
    onClick: () -> Unit
) {
    Surface(
        onClick,
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = currency.icon,
                    style = MaterialTheme.typography.headlineLarge
                )
                Text(
                    text = currency.code,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                BasicText(
                    text  = amount,
                    style = TextStyle(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold
                    ),
                    maxLines = 1,
                    autoSize = TextAutoSize.StepBased(maxFontSize = 48.sp)
                )
            }
        }
    }
}

@Composable
fun Keypad(uiState: AppState, onKeyPress: (String) -> Unit) {
    val keys = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf(".", "0", "<")
    )

    val decimalSeparator = remember(uiState.locale) {
        DecimalFormatSymbols.getInstance(uiState.locale).decimalSeparatorString
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        keys.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { key ->
                    KeypadButton(
                        key,
                        decimalSeparator,
                        modifier = Modifier.weight(1f)
                    ) { onKeyPress(it) }
                }
            }
        }
    }
}

@Composable
fun KeypadButton(
    key: String,
    decimalSeparator: String,
    modifier: Modifier,
    onKeyPress: (String) -> Unit
) {
    val haptic = LocalHapticFeedback.current

    Surface(
        modifier = modifier
            .aspectRatio(2f)
            .pointerInput(key) {
                detectTapGestures(
                    onTap = {
                        onKeyPress(key)
                        haptic.performHapticFeedback(HapticFeedbackType.KeyboardTap)
                    },
                    onLongPress = {
                        if (key == "<") {
                            onKeyPress("C")
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                    }
                )
            },
        shape = RoundedCornerShape(100),
        color = MaterialTheme.colorScheme.surfaceContainerHighest
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            when (key) {
                "<" -> {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.Backspace,
                        contentDescription = "Backspace",
                        modifier = Modifier.size(28.dp)
                    )
                }
                "." -> {
                    Text(
                        text = decimalSeparator,
                        style = MaterialTheme.typography.headlineLarge
                    )
                }
                else -> {
                    Text(
                        text = key,
                        style = MaterialTheme.typography.headlineLarge
                    )
                }
            }
        }
    }
}
