package com.sunagakure.sudoku

import android.content.Context
import android.graphics.*
import android.os.Handler
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.core.content.res.ResourcesCompat

class SudokuCanvas(context: Context, private var sudoku: Sudoku) : View(context) {
    private lateinit var extraCanvas: Canvas
    private lateinit var extraBitmap: Bitmap
    private var puzzleTileLength = 100
    private val topMargin = 100;
    private val leftMargin = 100
    private val backgroundColor = ResourcesCompat.getColor(resources, R.color.backgroundColor, null)
    private val drawColor = ResourcesCompat.getColor(resources, R.color.drawColor, null)
    private val boldColor = ResourcesCompat.getColor(resources, R.color.colorPrimary, null)
    private val boxBoldWidth = 10f
    private val keyboardTextWidth = 25f
    private var defaultPaint = Paint().apply {
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
        strokeWidth = boxBoldWidth // default: Hairline-width (really thin)
    }
    private val boldPaint = Paint().apply {
        color = boldColor
        isAntiAlias = true
        // Dithering affects how colors with higher-precision than the device are down-sampled.
        isDither = true
        style = Paint.Style.STROKE // default: FILL
        strokeJoin = Paint.Join.ROUND // default: MITER
        strokeCap = Paint.Cap.ROUND // default: BUTT
        strokeWidth = boxBoldWidth // default: Hairline-width (really thin)
    }
    private val textSize = 50f
    private val textPaint = Paint().apply {
        color = Color.BLUE
        style = Paint.Style.FILL
        textSize = 50f
    }
    private var constantPaint = Paint().apply {
        color = Color.LTGRAY
        style = Paint.Style.FILL // default: FILL
        textSize = 50f
    }
    private val messagePaint = Paint().apply {
        color = Color.RED
        style = Paint.Style.FILL
        textSize = 50f
    }
    private val buttonPaint = Paint().apply{
        style = Paint.Style.FILL
        color = Color.WHITE
    }
    private var currJ = -1
    private var currI = -1
    private var startedRequest = false
    private var keyboardOffset = 10
    private var keyboardKeyLength = 70

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (::extraBitmap.isInitialized)    extraBitmap.recycle()
        extraBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        extraCanvas = Canvas(extraBitmap)
        extraCanvas.drawColor(backgroundColor)
        if (!startedRequest) {
            sudoku.getPuzzle()
            startedRequest = true
        }
    }
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (canvas != null && this.sudoku.isReady()) {
            canvas.drawBitmap(extraBitmap, 0f, 0f, defaultPaint)
            drawRectangles(9, 9, canvas, defaultPaint, puzzleTileLength, leftMargin, topMargin)
            drawRectangles(3, 3, canvas, boldPaint, puzzleTileLength*3, leftMargin, topMargin)
            drawNumbers(9, 9, canvas, textPaint, constantPaint, outlinePaint, puzzleTileLength, leftMargin, topMargin,
                this.sudoku.puzzle, this.sudoku.solution)
            drawKeyboard(canvas, buttonPaint, textPaint)
        } else if (canvas != null) {
            canvas.drawBitmap(extraBitmap, 0f, 0f, defaultPaint)
            canvas.drawText("Loading", this.leftMargin.toFloat(), this.topMargin.toFloat(), this.textPaint)

            Handler().postDelayed(
                { this@SudokuCanvas.invalidate() }, 5000
            )
        }
    }

    private fun drawKeyboard(canvas: Canvas, buttonPaint: Paint, textPaint: Paint) {
        var leftX = leftMargin
        var topX = topMargin* 2 + this.puzzleTileLength * 9
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

        leftX = leftMargin;
        topX = topMargin * 3 + this.puzzleTileLength * 10

        val resetButton = Rect(leftX, topX, leftX + this.puzzleTileLength* 2 ,topX + this.puzzleTileLength)
        val resetText = "Reset"
        val checkText = "Check"
        val checkButton = Rect(leftX + this.leftMargin + this.puzzleTileLength * 2, topX,
            leftX + this.leftMargin + this.puzzleTileLength * 4 ,topX + this.puzzleTileLength)

        canvas.drawRect(resetButton, buttonPaint)
        canvas.drawText(resetText,
            (leftX + this.puzzleTileLength - 3*this.textSize/2.0).toFloat(),
            (topX + this.puzzleTileLength/2.0 + this.textSize/2.0).toFloat(), textPaint)
        canvas.drawRect(checkButton, buttonPaint)
        canvas.drawText(checkText,
            (leftX + this.leftMargin + this.puzzleTileLength * 2 + this.puzzleTileLength - 3*this.textSize/2.0).toFloat(),
            (topX + this.puzzleTileLength/2.0 + this.textSize/2.0).toFloat(), textPaint)
        if (this.sudoku.isValidated()) {
            this.sudoku.inValidate()
            var text = "Incorrect"
            if (this.sudoku.isSolved()) {
                text = "Correct!"
            }
            canvas.drawText(
                text,
                leftMargin.toFloat(),
                (topMargin * 4 + this.puzzleTileLength * 11).toFloat(),
                messagePaint
            )
        }
    }

    private fun drawNumbers(rows: Int, columns: Int, canvas: Canvas, defaultPaint: Paint, constantPaint: Paint, outlinePaint: Paint,
                            length: Int, leftOffset: Int, topOffset: Int, puzzle: Array<IntArray>?,
                            solution: Array<IntArray>?) {
        var topX = topOffset;
        var leftX = leftOffset;
        var counter = 0

        while(counter < rows * columns) {
            var i = counter/9
            var j = counter%9
            if (solution!![i][j] != -1) {
                var text = solution[i][j].toString()
                var paint = defaultPaint
                if (puzzle!![i][j] != -1)
                    paint = constantPaint

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
                var j = ((event.x-leftMargin)/this.puzzleTileLength).toInt()
                var i = ((event.y-topMargin)/this.puzzleTileLength).toInt()
                if (i < 9 && j < 9) {
                    if (this.sudoku.isValidPositionToFill(i, j)) {
                        currI = i
                        currJ = j
                    } else {
                        currJ = -1
                        currI = -1
                    }
                } else {
                    var indexY = this.puzzleTileLength*9 + this.topMargin*2
                    Log.i("Keyboard", "Event occurred at (${event.y} , ${event.x})")
                    if (event.y >= indexY && event.y <= indexY + this.keyboardKeyLength) {
                        var counter = 0
                        while((leftMargin + (this.keyboardOffset + this.keyboardKeyLength) * counter) < event.x)
                            counter++
                        Log.i("Keyboard", "Counter $counter")
                        if (counter in 1..10) {
                            var indexX = leftMargin + (this.keyboardOffset + this.keyboardKeyLength) * (counter - 1)
                            Log.i("Keyboard", "Index Y is $indexX and event Y is ${event.x}")
                            if (event.x <= indexX + this.keyboardKeyLength) {
                                if (counter == 1)
                                    this.sudoku.removeNumber(this.currI, this.currJ)
                                else
                                    this.sudoku.addNumber(this.currI, this.currJ, counter - 1)
                            }
                            Log.i("Keyboard", "Value at $i, $j is ${this.sudoku.solution?.get(currI)?.get(currJ)}")
                        }
                    } else {
                        indexY = this.puzzleTileLength*10 + this.topMargin*3
                        if (event.y >= indexY && event.y <= indexY + this.puzzleTileLength) {
                            var resetX = this.leftMargin
                            var checkX = resetX + this.leftMargin + this.puzzleTileLength * 2
                            if (event.x >= resetX && event.x <= resetX + this.puzzleTileLength * 2) {
                                this.sudoku.resetSolution();
                            } else if (event.x >= checkX && event.x <= checkX + this.puzzleTileLength * 2) {
                                this.sudoku.checkSolution();
                            }
                        }
                    }
                }
            }
        }
        this.invalidate()
        return  true
    }
}