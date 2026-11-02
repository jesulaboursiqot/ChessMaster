package com.chessmaster.app

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.widget.*
import com.github.bhlangonijr.chesslib.Board
import com.github.bhlangonijr.chesslib.Square

class MainActivity : Activity() {

    private var chess = Board()
    private var selected = ""
    private lateinit var boardView: GridLayout
    private lateinit var info: TextView
    private lateinit var cells: Array<TextView>

    private val light = Color.rgb(240, 217, 181)
    private val dark = Color.rgb(181, 136, 99)
    private val green = Color.rgb(120, 180, 90)

    private val symbols = mapOf(
        "WHITE_KING" to "♔",
        "WHITE_QUEEN" to "♕",
        "WHITE_ROOK" to "♖",
        "WHITE_BISHOP" to "♗",
        "WHITE_KNIGHT" to "♘",
        "WHITE_PAWN" to "♙",
        "BLACK_KING" to "♚",
        "BLACK_QUEEN" to "♛",
        "BLACK_ROOK" to "♜",
        "BLACK_BISHOP" to "♝",
        "BLACK_KNIGHT" to "♞",
        "BLACK_PAWN" to "♟",
        "NONE" to ""
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(12, 20, 12, 20)
            setBackgroundColor(Color.rgb(25, 32, 45))
        }

        val title = TextView(this).apply {
            text = "♟ CHESSMASTER"
            textSize = 29f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
        }
        root.addView(title)

        info = TextView(this).apply {
            text = "Au tour des blancs"
            textSize = 17f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            setPadding(0, 16, 0, 16)
        }
        root.addView(info)

        boardView = GridLayout(this).apply {
            columnCount = 8
            rowCount = 8
        }

        val size = (resources.displayMetrics.widthPixels -
            (24 * resources.displayMetrics.density).toInt()) / 8

        cells = Array(64) { i ->
            TextView(this).apply {
                textSize = 29f
                gravity = Gravity.CENTER
                setTextColor(Color.BLACK)
                setOnClickListener { tap(i) }
            }
        }

        for (i in 0 until 64) {
            boardView.addView(
                cells[i],
                GridLayout.LayoutParams().apply {
                    width = size
                    height = size
                }
            )
        }

        root.addView(boardView)

        val reset = Button(this).apply {
            text = "Nouvelle partie"
            setOnClickListener {
                chess = Board()
                selected = ""
                updateBoard()
                info.text = "Nouvelle partie ! Aux blancs."
            }
        }

        root.addView(reset)
        setContentView(root)
        updateBoard()
    }

    private fun tap(index: Int) {
        val file = 'a' + (index % 8)
        val rank = 8 - (index / 8)
        val destination = "$file$rank"

        if (selected.isEmpty()) {
            val square = Square.valueOf(destination.uppercase())
            if (chess.getPiece(square).toString() == "NONE") {
                info.text = "Sélectionne une pièce."
                return
            }

            selected = destination
            info.text = "Pièce sélectionnée : $destination"
            updateBoard()
            return
        }

        val legalMove = chess.legalMoves().firstOrNull {
            it.toString().take(4) == selected + destination
        }

        if (legalMove != null) {
            chess.doMove(legalMove)
            selected = ""

            info.text = when {
                chess.isMated() -> "ÉCHEC ET MAT ! Partie terminée."
                chess.isDraw() -> "Match nul !"
                else -> "Coup accepté ! À l'adversaire."
            }
        } else {
            val square = Square.valueOf(destination.uppercase())
            if (chess.getPiece(square).toString() != "NONE") {
                selected = destination
                info.text = "Nouvelle pièce sélectionnée."
            } else {
                selected = ""
                info.text = "Coup interdit ! Réessaie."
            }
        }

        updateBoard()
    }

    private fun updateBoard() {
        for (i in 0 until 64) {
            val file = 'a' + (i % 8)
            val rank = 8 - (i / 8)
            val name = "$file$rank"
            val square = Square.valueOf(name.uppercase())
            val piece = chess.getPiece(square).toString()

            cells[i].text = symbols[piece] ?: ""
            cells[i].setBackgroundColor(
                if (name == selected) green
                else if ((i / 8 + i % 8) % 2 == 0) light
                else dark
            )
        }
    }
}
