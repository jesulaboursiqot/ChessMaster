package com.chessmaster.app

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.widget.*

class MainActivity : Activity() {

    private val light = Color.rgb(240, 217, 181)
    private val dark = Color.rgb(181, 136, 99)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.gravity = Gravity.CENTER
        root.setPadding(16, 24, 16, 24)
        root.setBackgroundColor(Color.rgb(25, 32, 45))

        val title = TextView(this)
        title.text = "♟ CHESSMASTER"
        title.textSize = 30f
        title.setTextColor(Color.WHITE)
        title.gravity = Gravity.CENTER

        root.addView(title)

        val subtitle = TextView(this)
        subtitle.text = "Apprenez. Réfléchissez. Gagnez."
        subtitle.textSize = 16f
        subtitle.setTextColor(Color.LTGRAY)
        subtitle.gravity = Gravity.CENTER

        root.addView(subtitle)

        val board = GridLayout(this)
        board.columnCount = 8
        board.rowCount = 8

        val pieces = arrayOf(
            "♜","♞","♝","♛","♚","♝","♞","♜",
            "♟","♟","♟","♟","♟","♟","♟","♟",
            "","","","","","","","",
            "","","","","","","","",
            "","","","","","","","",
            "","","","","","","","",
            "♙","♙","♙","♙","♙","♙","♙","♙",
            "♖","♘","♗","♕","♔","♗","♘","♖"
        )

        val size = (resources.displayMetrics.widthPixels - 48) / 8

        for (i in 0 until 64) {
            val cell = TextView(this)
            cell.text = pieces[i]
            cell.textSize = 27f
            cell.gravity = Gravity.CENTER
            cell.setBackgroundColor(
                if ((i / 8 + i % 8) % 2 == 0) light else dark
            )

            board.addView(
                cell,
                GridLayout.LayoutParams().apply {
                    width = size
                    height = size
                }
            )
        }

        root.addView(board)

        val info = TextView(this)
        info.text = "Bienvenue dans ChessMaster !"
        info.textSize = 18f
        info.setTextColor(Color.WHITE)
        info.gravity = Gravity.CENTER
        info.setPadding(0, 24, 0, 16)

        root.addView(info)

        setContentView(root)
    }
}
