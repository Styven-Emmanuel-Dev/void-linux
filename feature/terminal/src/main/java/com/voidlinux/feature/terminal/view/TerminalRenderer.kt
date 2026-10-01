package com.voidlinux.feature.terminal.view

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import com.voidlinux.feature.terminal.TerminalBuffer

/**
 * Rendu alternatif du TerminalBuffer sur un Canvas externe.
 * Utilisé pour les aperçus ou les rendus hors écran.
 */
class TerminalRenderer(
    private val buffer: TerminalBuffer,
    private val textSize: Float = 30f
) {

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.MONOSPACE
        this.textSize = this@TerminalRenderer.textSize
        color = Color.parseColor("#00FF88")
    }

    private val bgPaint = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.FILL
    }

    fun render(canvas: Canvas, cellWidth: Float, cellHeight: Float) {
        canvas.drawColor(Color.BLACK)

        val fm = textPaint.fontMetrics
        val baselineOffset = -fm.ascent

        for (r in 0 until buffer.rows) {
            for (c in 0 until buffer.cols) {
                val cell = buffer.getCell(r, c) ?: continue

                val x = c * cellWidth
                val y = r * cellHeight

                if (cell.bg != Color.BLACK) {
                    bgPaint.color = cell.bg
                    canvas.drawRect(x, y, x + cellWidth, y + cellHeight, bgPaint)
                }

                if (cell.char != ' ') {
                    textPaint.color = cell.fg
                    canvas.drawText(cell.char.toString(), x, y + baselineOffset, textPaint)
                }
            }
        }
    }
}