package com.sunagakure.sudoku


import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View.SYSTEM_UI_FLAG_FULLSCREEN

class ShowSudoku : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        displaySudoku()
    }

    private fun displaySudoku() {
        val sudoku = Sudoku(this)
        val myCanvas = SudokuCanvas(this, sudoku)
        myCanvas.systemUiVisibility = SYSTEM_UI_FLAG_FULLSCREEN
        myCanvas.contentDescription = getString(R.string.description)
        setContentView(myCanvas)
    }
}
