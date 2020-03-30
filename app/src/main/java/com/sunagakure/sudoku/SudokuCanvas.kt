package com.sunagakure.sudoku

import android.content.Context
import android.graphics.*
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.core.content.res.ResourcesCompat

import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley

class SudokuCanvas(context: Context) : View(context) {
    private lateinit var extraCanvas: Canvas
    private lateinit var extraBitmap: Bitmap
    private var length = 100
    private val topMargin = 100;
    private val leftMargin = 100
    private val backgroundColor = ResourcesCompat.getColor(resources, R.color.backgroundColor, null)
    private val drawColor = ResourcesCompat.getColor(resources, R.color.drawColor, null)
    private val boldColor = ResourcesCompat.getColor(resources, R.color.colorPrimary, null)
    private val STROKEWIDTH = 10f
    private val keyboardTextWidth = 25f
    private var paint = Paint().apply {
        color = drawColor
        // Smooths out edges of what is drawn without affecting shape.
        isAntiAlias = true
        // Dithering affects how colors with higher-precision than the device are down-sampled.
        isDither = true
        style = Paint.Style.STROKE // default: FILL
        strokeJoin = Paint.Join.ROUND // default: MITER
        strokeCap = Paint.Cap.ROUND // default: BUTT
    }
    private val outlinePaint: Paint = Paint().apply {
        color = Color.RED
        isAntiAlias = true
        // Dithering affects how colors with higher-precision than the device are down-sampled.
        isDither = true
        style = Paint.Style.STROKE // default: FILL
        strokeJoin = Paint.Join.ROUND // default: MITER
        strokeCap = Paint.Cap.ROUND // default: BUTT
        strokeWidth = STROKEWIDTH // default: Hairline-width (really thin)
    }
    private val boldPaint = Paint().apply {
        color = boldColor
        isAntiAlias = true
        // Dithering affects how colors with higher-precision than the device are down-sampled.
        isDither = true
        style = Paint.Style.STROKE // default: FILL
        strokeJoin = Paint.Join.ROUND // default: MITER
        strokeCap = Paint.Cap.ROUND // default: BUTT
        strokeWidth = STROKEWIDTH // default: Hairline-width (really thin)
    }
    private val textSize = 50f
    private val textPaint = Paint().apply {
        color = Color.BLUE
        style = Paint.Style.FILL
        textSize = 50f
    }
    private val buttonPaint = Paint().apply{
        style = Paint.Style.FILL
        color = Color.WHITE
    }
    private var puzzle: Array<IntArray>? = null
    private var solution: Array<IntArray>? = null
    private var currI = -1
    private var startedRequest = false
    private var currJ = -1
    private var keyboardOffset = 40
    private var keyboardKeyLength = 50

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (::extraBitmap.isInitialized)    extraBitmap.recycle()
        extraBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        extraCanvas = Canvas(extraBitmap)
        extraCanvas.drawColor(backgroundColor)
        if (!startedRequest) {
            getPuzzle()
            startedRequest = true
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (canvas != null && this.solution != null) {
            canvas.drawBitmap(extraBitmap, 0f, 0f, paint)
            drawRectangles(9, 9, canvas, paint, length, leftMargin, topMargin)
            drawRectangles(3, 3, canvas, boldPaint, length*3, leftMargin, topMargin)
            drawNumbers(9, 9, canvas, textPaint, outlinePaint, length, leftMargin, topMargin)
            drawKeyboard(canvas, buttonPaint, textPaint)
        } else if (canvas != null) {
            Log.i("INFO", "Canvas is ready, response is not")
            canvas.drawBitmap(extraBitmap, 0f, 0f, paint)
        }
    }

    private fun drawKeyboard(canvas: Canvas, buttonPaint: Paint, textPaint: Paint) {
        var leftX = leftMargin
        var topX = topMargin* 2 + length * 9
        var length = this.keyboardKeyLength
        var leftOffset = this.keyboardOffset
        var textSize = this.keyboardTextWidth
        var i = 0
        while (i <= 9) {
            val r = Rect(leftX, topX, leftX+length, topX+length)
            canvas.drawRect(r, buttonPaint)
            leftX += length + leftOffset
            i++
        }
        leftX = leftMargin
        i = 0
        while (i <= 9) {
            val text = if (i == 0) {
                ""
            } else {
                i.toString()
            }
            canvas.drawText(text, (leftX + length/2.0 - textSize/2).toFloat(),
                (topX + length/2.0 + 3*textSize/4).toFloat(), textPaint)
            i++
            leftX += length + leftOffset
        }
    }
    private fun drawNumbers(rows: Int, columns: Int, canvas: Canvas, paint: Paint, outlinePaint: Paint,
                            length: Int, leftOffset: Int, topOffset: Int) {
        var topX = topOffset;
        var leftX = leftOffset;
        var counter = 0

        while(counter < rows * columns) {
            var i = counter/9
            var j = counter%9
            if (this.solution!![i][j] != -1) {
                var text = this.solution!![i][j].toString()
                if (this.puzzle!![i][j] == -1)
                    paint.color = Color.BLUE
                else
                    paint.color = Color.LTGRAY
                canvas.drawText(
                    text,
                    (leftX + length / 2.0 - this.textSize/2).toFloat(), (topX + length / 2.0 + this.textSize/2).toFloat(), paint
                )
            }
            if (i == currI && j == currJ) {
                var r = Rect(leftX, topX, leftX + length, topX + length)
                canvas.drawRect(r, outlinePaint)
            }
            counter++
            leftX += length
            if (counter % rows == 0) {
                topX += length
                leftX = leftOffset
            }
        }
        paint.color = Color.BLUE
    }

    private fun parseString(string: String) {
        var i = 0;
        this.puzzle = Array(9) { IntArray(9) }
        this.solution = Array(9) { IntArray(9) }
        while (i < string.length) {
            if (string[i] == '.') {
                this.puzzle!![i / 9][i % 9] = -1
                this.solution!![i / 9][i % 9] = -1
            } else {
                this.puzzle!![i / 9][i % 9] = Character.getNumericValue(string[i])
                this.solution!![i / 9][i % 9] = Character.getNumericValue(string[i])
            }
            i++
        }
        this.invalidate()
    }

    private fun getPuzzle() {
        val queue = Volley.newRequestQueue(this.context)
        val url = "https://agarithm.com/sudoku/new"
        val stringRequest = StringRequest(Request.Method.GET, url,
            Response.Listener<String> { response ->
                Log.i("INFO", "Response found $response")
                parseString(response)
            },
            Response.ErrorListener { error: VolleyError? ->
                Log.e("ERROR", "Error occurred while making request: $error")
            }
        )
        queue.add(stringRequest)
    }

    private fun drawRectangles(rows: Int, columns: Int, canvas: Canvas, paint: Paint,
                       incr: Int, leftOffset: Int, topOffset: Int) {
        var topX = topOffset
        var leftX = leftOffset
        var counter = 0
        while(counter < rows*columns) {
            val r = Rect(leftX, topX, leftX+incr, topX+incr)
            canvas.drawRect(r, paint)
            counter++
            leftX += incr
            if (counter % rows == 0) {
                topX += incr
                leftX = leftOffset
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        super.onTouchEvent(event)
        if (event!= null) {
            var action = event.action
            if(action == MotionEvent.ACTION_DOWN) {
                var j = ((event.x-leftMargin)/this.length).toInt()
                var i = ((event.y-topMargin)/this.length).toInt()
                if (i < 9 && j < 9) {
                    Log.i("TouchEvent", "Puzzle at $i , $j is ${puzzle?.get(i)?.get(j)}")
                    if (puzzle?.get(i)?.get(j) == -1) {
                        currI = i
                        currJ = j
                    } else {
                        currJ = -1
                        currI = -1
                    }
                } else {
                    var indexY = this.length*9 + this.topMargin*2
                    Log.i("Keyboard", "Index Y is $indexY")
                    if (event.y >= indexY && event.y <= indexY + this.keyboardKeyLength) {
                        var counter = 0
                        while((leftMargin + (this.keyboardOffset + this.keyboardKeyLength) * counter) < event.x)
                            counter++
                        Log.i("Keyboard", "Counter $counter")
                        if (counter in 1..10) {
                            var indexX = leftMargin + (this.keyboardOffset + this.keyboardKeyLength) * (counter - 1)
                            if (event.x <= indexX + this.keyboardOffset) {
                                if (counter == 1)
                                    removeNumber()
                                else
                                    addNumber(counter - 1)
                            }
                        }
                    }
                }
                this.invalidate()
            }
        }
        return  true
    }

    private fun addNumber(i: Int) {
        Log.i("INFO", "Adding $i")
        if (currI != -1 && currJ != -1) {
            this.solution?.get(currI)?.set(currJ, i)
            this.invalidate()
        } else {
            Log.i("AddNumber", "Unable to add number as no box is selected")
        }
    }
    private fun removeNumber() {
        Log.i("Remove", "Removing number")
        if (currJ == -1 || currI == -1) {
            Log.i("Remove","Removing number failed as no box is selected")
        } else if (this.solution?.get(currI)?.get(currJ) == -1) {
            Log.i("Remove","Removing number failed as selected box is a constant in the problem")
        } else {
            this.solution?.get(currI)?.set(currJ, -1)
            Log.i("Remove", "Removed number at ${solution?.get(currI)?.get(currJ)}")
            this.invalidate()
        }
    }
}