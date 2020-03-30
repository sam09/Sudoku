package com.sunagakure.sudoku


import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View.SYSTEM_UI_FLAG_FULLSCREEN

class ShowSudoku : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val difficultyVal = intent.getIntExtra(DIFFICULTY, 0)
        displaySudoku(difficultyVal)
    }

    fun displaySudoku(difficultyVal: Int) {
        Log.i("SUDOKU", "Generating puzzle of $difficultyVal difficulty")
        val myCanvas = SudokuCanvas(this)
        myCanvas.systemUiVisibility = SYSTEM_UI_FLAG_FULLSCREEN
        myCanvas.contentDescription = getString(R.string.description)

        setContentView(myCanvas)
    }
}
