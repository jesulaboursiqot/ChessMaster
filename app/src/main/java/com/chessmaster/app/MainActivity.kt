package com.chessmaster.app

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.widget.Button
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {

    private val light = Color.rgb(240, 217, 181)
    private val dark = Color.rgb(181, 136, 99)
    private val background = Color.rgb(25, 32, 45)
    private val selectedColor = Color.rgb(120, 180, 90)

    private val initialPieces = arrayOf(
        "♜","♞","♝","♛","♚","♝","♞","♜",
        "♟","♟","♟","♟","♟","♟","♟","♟",
        "","","","","","","","",
        "","","","","","","","",
        "","","","","","","","",
        "","","","","","","","",
        "♙","♙","♙","♙","♙","♙","♙","♙",
        "♖","♘","♗","♕","♔","♗","♘","♖"
    )

    private var pieces = initialPieces.copyOf()
    private var selected = -1

    private lateinit var board: GridLayout
    private lateinit var info: TextView
    private lateinit var squares: Array<TextView>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showGame()
    }

    private fun showGame() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(12, 20, 12, 20)
            setBackgroundColor(Color.rgb(25, 32, 45))
        }

        val title = TextView(this).apply {
            text = "♟ CHESSMASTER"
            textSize = 30f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }

        root.addView(
            title,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        val subtitle = TextView(this).apply {
            text = "Apprenez. Réfléchissez. Gagnez."
            textSize = 15f
            setTextColor(Color.LTGRAY)
            gravity = Gravity.CENTER
            setPadding(0, 8, 0, 20)
        }
        root.addView(subtitle)

        board = GridLayout(this).apply {
            columnCount = 8
            rowCount = 8
        }

        val size = (resources.displayMetrics.widthPixels -
            (24 * resources.displayMetrics.density).toInt()) / 8

        squares = Array(64) { i ->
            TextView(this).apply {
                textSize = 29f
                gravity = Gravity.CENTER
                setTextColor(Color.BLACK)
                setOnClickListener { tapSquare(i) }
            }
        }

        for (i in 0 until 64) {
            val params = GridLayout.LayoutParams().apply {
                width = size
                height = size
                setMargins(0, 0, 0, 0)
            }
            board.addView(squares[i], params)
        }

        root.addView(board)
        drawBoard()

        info = TextView(this).apply {
            text = "Choisis une pièce pour commencer."
            textSize = 16f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(0, 20, 0, 16)
        }
        root.addView(info)

        val reset = Button(this).apply {
            text = "Nouvelle partie"
            textSize = 16f
            setOnClickListener {
                pieces = initialPieces.copyOf()
                selected = -1
                info.text = "Nouvelle partie ! Choisis une pièce."
                drawBoard()
            }
        }

        root.addView(
            reset,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        setContentView(root)
    }

    private fun tapSquare(position: Int) {
        if (selected == -1) {
            if (pieces[position].isNotEmpty()) {
                selected = position
                info.text = "Pièce sélectionnée. Choisis sa destination."
                drawBoard()
            } else {
                info.text = "Choisis d'abord une case avec une pièce."
            }
            return
        }

        if (position == selected) {
            selected = -1
            info.text = "Sélection annulée."
            drawBoard()
            return
        }

        if (pieces[position].isNotEmpty() &&
            sameSide(pieces[selected], pieces[position])) {
            selected = position
            info.text = "Nouvelle pièce sélectionnée."
            drawBoard()
            return
        }

        pieces[position] = pieces[selected]
        pieces[selected] = ""
        selected = -1

        info.text = "Pièce déplacée ! Choisis un autre coup."
        drawBoard()
    }

    private fun sameSide(first: String, second: String): Boolean {
        val whitePieces = "♙♖♘♗♕♔"
        val blackPieces = "♟♜♞♝♛♚"

        return (first in whitePieces && second in whitePieces) ||
               (first in blackPieces && second in blackPieces)
    }

    private fun drawBoard() {
        for (i in 0 until 64) {
            squares[i].text = pieces[i]

            val normalColor =
                if ((i / 8 + i % 8) % 2 == 0) light else dark

            squares[i].setBackgroundColor(
                if (i == selected) selectedColor else normalColor
            )
        }
    }
}
