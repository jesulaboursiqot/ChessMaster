package com.chessmaster.app

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.widget.*
import com.github.bhlangonijr.chesslib.Board
import com.github.bhlangonijr.chesslib.Square
import com.github.bhlangonijr.chesslib.move.Move

class MainActivity : Activity() {

    private var chess = Board()
    private var selected = ""
    private var difficulty = 2
    private var thinking = false

    private lateinit var info: TextView
    private lateinit var cells: Array<TextView>

    private val light = Color.rgb(240, 217, 181)
    private val dark = Color.rgb(181, 136, 99)
    private val green = Color.rgb(120, 180, 90)

    private val symbols = mapOf(
        "WHITE_KING" to "♔", "WHITE_QUEEN" to "♕",
        "WHITE_ROOK" to "♖", "WHITE_BISHOP" to "♗",
        "WHITE_KNIGHT" to "♘", "WHITE_PAWN" to "♙",
        "BLACK_KING" to "♚", "BLACK_QUEEN" to "♛",
        "BLACK_ROOK" to "♜", "BLACK_BISHOP" to "♝",
        "BLACK_KNIGHT" to "♞", "BLACK_PAWN" to "♟",
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
            text = "♟ CHESSMASTER V4"
            textSize = 27f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
        }
        root.addView(title)

        val level = Spinner(this)
        val options = listOf("Débutant", "Intermédiaire", "Difficile")
        level.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            options
        )
        level.setSelection(1)
        level.onItemSelectedListener =
            object : android.widget.AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
                override fun onItemSelected(
                    parent: android.widget.AdapterView<*>?,
                    view: android.view.View?,
                    position: Int,
                    id: Long
                ) {
                    difficulty = position + 1
                }
            }
        root.addView(level)

        info = TextView(this).apply {
            text = "Tu joues les Blancs. À toi !"
            textSize = 16f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            setPadding(0, 15, 0, 15)
        }
        root.addView(info)

        val grid = GridLayout(this).apply {
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
            grid.addView(
                cells[i],
                GridLayout.LayoutParams().apply {
                    width = size
                    height = size
                }
            )
        }

        root.addView(grid)

        val reset = Button(this).apply {
            text = "Nouvelle partie"
            setOnClickListener {
                chess = Board()
                selected = ""
                thinking = false
                info.text = "Nouvelle partie ! Tu joues les Blancs."
                updateBoard()
            }
        }
        root.addView(reset)

        setContentView(root)
        updateBoard()
    }

    private fun tap(index: Int) {
        if (thinking || chess.sideToMove.toString() != "WHITE") return

        val file = 'a' + index % 8
        val rank = 8 - index / 8
        val destination = "$file$rank"

        if (selected.isEmpty()) {
            val square = Square.valueOf(destination.uppercase())
            val piece = chess.getPiece(square).toString()

            if (piece.startsWith("WHITE_")) {
                selected = destination
                info.text = "Pièce sélectionnée : $destination"
                updateBoard()
            } else {
                info.text = "Sélectionne une pièce blanche."
            }
            return
        }

        val move = chess.legalMoves().firstOrNull {
            it.toString().take(4) == selected + destination
        }

        if (move == null) {
            selected = ""
            info.text = "Coup interdit. Réessaie."
            updateBoard()
            return
        }

        chess.doMove(move)
        selected = ""
        updateBoard()

        if (finishIfNeeded()) return

        thinking = true
        info.text = "L'ordinateur réfléchit..."
        updateBoard()

        Thread {
            val reply = chooseMove(chess, difficulty)

            runOnUiThread {
                if (reply != null && chess.sideToMove.toString() == "BLACK") {
                    chess.doMove(reply)
                }
                thinking = false
                updateBoard()
                finishIfNeeded()
            }
        }.start()
    }

    private fun chooseMove(position: Board, depth: Int): Move? {
        val moves = position.legalMoves().shuffled()
        var bestMove: Move? = null
        var bestScore = Int.MIN_VALUE

        for (move in moves) {
            val next = position.clone()
            next.doMove(move)
            val score = -search(next, depth - 1)
            if (score > bestScore) {
                bestScore = score
                bestMove = move
            }
        }
        return bestMove
    }

    private fun search(board: Board, depth: Int): Int {
        if (board.isMated()) return -100000
        if (board.isDraw() || board.isStaleMate()) return 0
        if (depth <= 0) return evaluate(board)

        var best = Int.MIN_VALUE
        for (move in board.legalMoves()) {
            val next = board.clone()
            next.doMove(move)
            val score = -search(next, depth - 1)
            if (score > best) best = score
        }
        return best
    }

    private fun evaluate(board: Board): Int {
        var whiteScore = 0

        val values = mapOf(
            "PAWN" to 100,
            "KNIGHT" to 320,
            "BISHOP" to 330,
            "ROOK" to 500,
            "QUEEN" to 900,
            "KING" to 20000
        )

        for (square in Square.values()) {
            if (square == Square.NONE) continue
            val piece = board.getPiece(square).toString()
            val value = values.entries.firstOrNull {
                piece.endsWith(it.key)
            }?.value ?: 0

            if (piece.startsWith("WHITE_")) whiteScore += value
            if (piece.startsWith("BLACK_")) whiteScore -= value
        }

        return if (board.sideToMove.toString() == "WHITE")
            whiteScore else -whiteScore
    }

    private fun finishIfNeeded(): Boolean {
        if (chess.isMated()) {
            info.text = if (chess.sideToMove.toString() == "WHITE")
                "Échec et mat ! L'ordinateur gagne."
            else "Échec et mat ! Tu as gagné !"
            return true
        }

        if (chess.isDraw() || chess.isStaleMate()) {
            info.text = "Match nul !"
            return true
        }

        info.text = if (chess.sideToMove.toString() == "WHITE")
            "À toi de jouer !"
        else "Au tour de l'ordinateur."

        return false
    }

    private fun updateBoard() {
        for (i in 0 until 64) {
            val file = 'a' + i % 8
            val rank = 8 - i / 8
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
