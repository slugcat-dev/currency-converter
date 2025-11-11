package com.android.converter.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.android.converter.data.model.Currency
import com.android.converter.ui.AppState
import com.android.converter.ui.AppViewModel

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun CurrencySelectionScreen(
    type: String,
    viewModel: AppViewModel,
    uiState: AppState,
    navController: NavController
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredCurrencies = uiState.currencies.filter { currency ->
        currency.name.contains(searchQuery, ignoreCase = true) ||
        currency.code.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "Currencies") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                label = { Text(text = "Search") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null
                    )
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                singleLine = true
            )

            @OptIn(ExperimentalFoundationApi::class)
            LazyColumn {
                val groupedCurrencies = filteredCurrencies.groupBy { it.category }

                groupedCurrencies.forEach { (category, currencies) ->
                    item {
                        Text(
                            text = category,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp, 8.dp)
                                .background(MaterialTheme.colorScheme.surface),
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                    items(currencies) { currency ->
                        CurrencyRow(currency) {
                            viewModel.setCurrency(type, currency)
                            navController.popBackStack()
                        }
                    }
                    item {
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun CurrencyRow(currency: Currency, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(16.dp, 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = currency.icon,
                style = MaterialTheme.typography.headlineLarge
            )
            Column(modifier = Modifier.padding(start = 16.dp)) {
                Text(
                    text = currency.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = currency.code,
                    modifier = Modifier.alpha(.75f),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
