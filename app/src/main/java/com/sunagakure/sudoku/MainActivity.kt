package com.sunagakure.sudoku

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
const val DIFFICULTY = "DIFFICULTY"
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
    fun clickListener(it: View) {
        Log.i("ButtonClick", "Button clicked ${it.id}")
        val intent = Intent(this, ShowSudoku::class.java)
        startActivity(intent)
    }
}

