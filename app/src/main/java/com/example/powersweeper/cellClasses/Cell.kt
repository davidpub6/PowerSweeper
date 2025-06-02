package com.example.powersweeper.cellClasses

// Board data representation
data class Cell(
    var isMine: Boolean = false,
    var isRevealed: Boolean = false,
    var adjacentMines: Int = 0,
    var isFlagged: Boolean = false,
    var special: SpecialType = SpecialType.NONE,
    var isSpecial: Boolean = false
)
