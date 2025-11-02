package com.android.converter.ui.navigation.transitions

import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.ui.unit.IntOffset

object SlideTransitions {
    private val animationSpec = tween<IntOffset>(300)

    val enter = slideInHorizontally(animationSpec, initialOffsetX = { it })
    val exit = slideOutHorizontally(animationSpec, targetOffsetX = { -it })
    val popEnter = slideInHorizontally(animationSpec, initialOffsetX = { -it })
    val popExit = slideOutHorizontally(animationSpec, targetOffsetX = { it })
}
