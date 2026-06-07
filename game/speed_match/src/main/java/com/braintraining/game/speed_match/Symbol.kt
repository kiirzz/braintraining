package com.braintraining.game.speed_match

internal enum class Shape { CIRCLE, SQUARE, TRIANGLE, STAR, DIAMOND }

internal enum class SymbolColor { RED, BLUE, GREEN, YELLOW, PURPLE }

internal data class Symbol(
    val shape: Shape,
    val color: SymbolColor,
)