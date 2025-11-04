package com.android.converter.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalConfiguration
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.android.converter.ui.AppViewModel
import com.android.converter.ui.navigation.transitions.SlideTransitions
import com.android.converter.ui.screens.CurrencyConverterScreen
import com.android.converter.ui.screens.CurrencySelectionScreen

@Composable
fun AppNavHost(viewModel: AppViewModel) {
    // Let the view model access the app locale
    val configuration = LocalConfiguration.current
    val locale = configuration.locales[0]

    LaunchedEffect(locale) {
        viewModel.setLocale(locale)
    }

    // Create the nav controller
    val navController = rememberNavController()
    val uiState by viewModel.uiState.collectAsState()

    NavHost(
        navController,
        startDestination = "converter",
        enterTransition = { SlideTransitions.enter },
        exitTransition = { SlideTransitions.exit },
        popEnterTransition = { SlideTransitions.popEnter },
        popExitTransition = { SlideTransitions.popExit }
    ) {
        composable("converter") {
            CurrencyConverterScreen(
                viewModel,
                uiState,
                navController
            )
        }
        composable("selection/{type}") { entry ->
            val type = entry.arguments!!.getString("type")!!

            CurrencySelectionScreen(
                type,
                viewModel,
                uiState,
                navController
            )
        }
    }
}
