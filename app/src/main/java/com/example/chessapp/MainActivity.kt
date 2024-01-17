package com.example.chessapp

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.graphics.component1
import androidx.core.graphics.component2
import com.github.bhlangonijr.chesslib.Board
import com.github.bhlangonijr.chesslib.Piece
import com.github.bhlangonijr.chesslib.Rank
import com.github.bhlangonijr.chesslib.Side
import com.github.bhlangonijr.chesslib.Square
import com.github.bhlangonijr.chesslib.move.Move

class MainActivity : AppCompatActivity() {

    private var gameMode: Int = 0
    private lateinit var chessboardGameView: ChessBoardGameView
    private lateinit var board: Board
    private var selectedSquare: Square? = null
    private var movingPiece: Piece? = null
    private var isFlipped: Boolean = false
    private var turnIn: ImageView? = null
    private lateinit var whiteTimer: CountDownTimer
    private lateinit var blackTimer: CountDownTimer
    private lateinit var whiteTimerTV: TextView
    private lateinit var blackTimerTV: TextView
    private var incrementTime: Long = 0
    private var time: Long = 0
    private var TRW: Long = 0
    private var TRB: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        gameMode = intent.getIntExtra("GAME_MODE", 0)
        time = intent.getLongExtra("TIME", 0)
        TRW = time
        TRB = time
        incrementTime = intent.getLongExtra("INCREMENT", 0)

        chessboardGameView = findViewById(R.id.chessboardView)
        board = Board()

        if (gameMode == 2) setRandomPieces(board)

        chessboardGameView.setBoardPosition(board)

        whiteTimerTV = findViewById(R.id.whiteTimerTV)
        blackTimerTV = findViewById(R.id.blackTimerTV)


        chessboardGameView.setOnSquareClickListener { file, rank ->
            handleUserMove(file, rank)
        }

        val resetButton = findViewById<Button>(R.id.resetButton)
        resetButton.setOnClickListener {
            resetGame()
        }
        val flipButton = findViewById<Button>(R.id.flipButton)
        flipButton.setOnClickListener {
            isFlipped = !isFlipped
            chessboardGameView.flipBoard()
        }
        val undoButton = findViewById<Button>(R.id.undoButton)
        undoButton.setOnClickListener {
            if (board.history.size > 1) {
                board.undoMove()
                chessboardGameView.setBoardPosition(board)
            }
        }
        val returnToM = findViewById<Button>(R.id.returnToMenu)
        returnToM.setOnClickListener {
            startActivity(Intent(this, GameMenu::class.java))
            finish()
        }
        turnIn = findViewById<ImageView>(R.id.turnIndicator)
        val layoutParams = turnIn!!.layoutParams
        layoutParams.width = 200
        layoutParams.height = 200
        turnIn!!.layoutParams = layoutParams

        createWhiteTimer()
        createBlackTimer()
        whiteTimerTV.text = String.format("%02d:%02d", (time / 1000) / 60, (time / 1000) % 60)
        blackTimerTV.text = String.format("%02d:%02d", (time / 1000) / 60, (time / 1000) % 60)
    }

    private fun setRandomPieces(board: Board) {
        val randomVal = (21..61).random()
        var remainingValueW = randomVal
        var remainingValueB = randomVal

        while (remainingValueW > 3) {
            for (file in 'A'..'H') {
                val square = Square.valueOf((file + "1").uppercase())
                if (board.getPiece(square) != Piece.WHITE_KING) {
                    board.unsetPiece(board.getPiece(square), square)
                    if (remainingValueW > 3) {
                        val ranPiece = if (remainingValueW < 10) getRandomPiece((0..10 - remainingValueW).random(), 1)
                        else getRandomPiece((0..10).random(), 1)
                        board.setPiece(ranPiece, square)
                        val pieceValue = getPieceValue(ranPiece)
                        remainingValueW -= pieceValue
                    }
                }
            }
            if (remainingValueW > 3) remainingValueW = randomVal
        }
        while (remainingValueB > 3) {
            for (file in 'A'..'H') {
                val square = Square.valueOf((file + "8").uppercase())
                if (board.getPiece(square) != Piece.BLACK_KING) {
                    board.unsetPiece(board.getPiece(square), square)
                    if (remainingValueB > 3) {
                        val ranPiece = if (remainingValueB < 10) getRandomPiece((0..10 - remainingValueB).random(), 8)
                        else getRandomPiece((0..10).random(), 8)
                        board.setPiece(ranPiece, square)
                        val pieceValue = getPieceValue(ranPiece)
                        remainingValueB -= pieceValue
                    }
                }
            }
            if (remainingValueB > 3) remainingValueB = randomVal
        }
    }

    private fun getRandomPiece(value: Int, rank: Int): Piece {
        return when {
            value >= 9 -> if (rank == 8) Piece.BLACK_QUEEN else Piece.WHITE_QUEEN
            value >= 6 -> if (rank == 8) Piece.BLACK_ROOK else Piece.WHITE_ROOK
            value >= 3 -> if (rank == 8) Piece.BLACK_KNIGHT else Piece.WHITE_KNIGHT
            value >= 0 -> if (rank == 8) Piece.BLACK_BISHOP else Piece.WHITE_BISHOP
            else -> Piece.NONE
        }
    }

    private fun getPieceValue(piece: Piece): Int {
        return when (piece) {
            Piece.WHITE_PAWN, Piece.BLACK_PAWN -> 1
            Piece.WHITE_KNIGHT, Piece.BLACK_KNIGHT -> 3
            Piece.WHITE_BISHOP, Piece.BLACK_BISHOP -> 3
            Piece.WHITE_ROOK, Piece.BLACK_ROOK -> 5
            Piece.WHITE_QUEEN, Piece.BLACK_QUEEN -> 9
            else -> 0
        }
    }

    private fun showPromotionDialog(move: Move) {
        val promotionOptions = arrayOf("Queen", "Rook", "Bishop", "Knight")

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Choose promotion piece")
        builder.setItems(promotionOptions) { dialog, which ->
            val promotionPiece = when (which) {
                0 -> if (movingPiece == Piece.WHITE_PAWN) Piece.WHITE_QUEEN else Piece.BLACK_QUEEN
                1 -> if (movingPiece == Piece.WHITE_PAWN) Piece.WHITE_ROOK else Piece.BLACK_ROOK
                2 -> if (movingPiece == Piece.WHITE_PAWN) Piece.WHITE_BISHOP else Piece.BLACK_BISHOP
                3 -> if (movingPiece == Piece.WHITE_PAWN) Piece.WHITE_KNIGHT else Piece.BLACK_KNIGHT
                else -> if (movingPiece == Piece.WHITE_PAWN) Piece.WHITE_QUEEN else Piece.BLACK_QUEEN
            }
            val newMove = Move(move.from, move.to, promotionPiece)

            if (board.legalMoves().contains(newMove)) {
                board.doMove(newMove)
                chessboardGameView.setBoardPosition(board)
            }
            selectedSquare = null
            chessboardGameView.setLegalMoves(emptyList())
            chessboardGameView.setSelectedSquare(selectedSquare)
            dialog.dismiss()
        }
        builder.show()
    }

    private fun handleUserMove(file: Char, rank: Int) {
        val currentSquare = Square.valueOf("$file$rank".uppercase())

        if (selectedSquare == null) {
            val piece = board.getPiece(currentSquare)
            if (piece != Piece.NONE && piece != null) {
                selectedSquare = currentSquare
                val legalMoves = board.legalMoves()
                    .filter { it.from == selectedSquare }
                    .map { it.to }
                chessboardGameView.setLegalMoves(legalMoves)
            }
        } else {
            val move = Move(selectedSquare!!, currentSquare)
            movingPiece = board.getPiece(selectedSquare!!)
            if (movingPiece == Piece.WHITE_PAWN && move.to.rank == Rank.RANK_8 ||
                movingPiece == Piece.BLACK_PAWN && move.to.rank == Rank.RANK_1) {
                showPromotionDialog(move)
            }

            if (board.legalMoves().contains(move)) {
                board.doMove(move)
                chessboardGameView.setBoardPosition(board)
                if (board.history.size > 2) {
                    if (board.sideToMove == Side.WHITE) {
                        TRB += incrementTime
                        whiteTimer.cancel()
                        createWhiteTimer()
                        whiteTimer.start()
                        blackTimer.cancel()
                    } else {
                        TRW += incrementTime
                        blackTimer.cancel()
                        createBlackTimer()
                        blackTimer.start()
                        whiteTimer.cancel()
                    }
                }
            }
            selectedSquare = null
            chessboardGameView.setLegalMoves(emptyList())
        }
        chessboardGameView.setSelectedSquare(selectedSquare)
        chessboardGameView.setSelectedPiece(movingPiece)

        if (board.sideToMove == Side.WHITE) {
            val drawable = resources.getDrawable(R.drawable.king_light, null)
            drawable.setBounds(0,0, 200, 200)
            turnIn!!.setImageDrawable(drawable)
        }
        else {
            val drawable = resources.getDrawable(R.drawable.king_dark, null)
            drawable.setBounds(20,20, 200, 200)
            turnIn!!.setImageDrawable(drawable)
        }

        if (board.isMated) {
            val winner = if (board.sideToMove == Side.WHITE) "Black" else "White"
            showVictoryDialog(winner)
        }

        if (board.isDraw) {
            val drawer = if (board.sideToMove == Side.WHITE) "White" else "Black"
            showDrawDialog("$drawer has no legal moves")
        }

        if (board.isInsufficientMaterial) {
            showDrawDialog("Insufficient Material")
        }

        if (board.isRepetition) {
            showDrawDialog("Repetition")
        }
    }

    private fun createWhiteTimer() {
        whiteTimer =  object : CountDownTimer(TRW, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                TRW = millisUntilFinished
                val seconds = millisUntilFinished / 1000
                val minutes = seconds / 60
                val remainingSeconds = seconds % 60
                whiteTimerTV.text = String.format("%02d:%02d", minutes, remainingSeconds)
            }

            override fun onFinish() {
                showVictoryTimeUpDialog("Black", "White")
            }
        }
    }

    private fun createBlackTimer() {
        blackTimer =  object : CountDownTimer(TRB, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                TRB = millisUntilFinished
                val seconds = millisUntilFinished / 1000
                val minutes = seconds / 60
                val remainingSeconds = seconds % 60
                blackTimerTV.text = String.format("%02d:%02d", minutes, remainingSeconds)
            }

            override fun onFinish() {
                showVictoryTimeUpDialog("White", "Black")
            }
        }
    }

    private fun showVictoryDialog(winner: String) {
        val message = "$winner wins! Checkmate!"
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Victory!")
        builder.setMessage(message)
        builder.setPositiveButton("Reset Game") { _, _ ->
            resetGame()
        }
        builder.show()
    }

    private fun showDrawDialog(draw: String) {
        val message = "Stalemate! $draw!"
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Draw!")
        builder.setMessage(message)
        builder.setPositiveButton("Reset Game") { _, _ ->
            resetGame()
        }
        builder.show()
    }

    private fun showVictoryTimeUpDialog(winner: String, loser: String) {
        val message = "$winner wins! $loser ran out of time!"
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Victory!")
        builder.setMessage(message)
        builder.setPositiveButton("Reset Game") { _, _ ->
            resetGame()
        }
        builder.show()
    }

    private fun resetGame() {
        whiteTimer.cancel()
        blackTimer.cancel()
        TRW = time
        TRB = time
        createWhiteTimer()
        createBlackTimer()
        whiteTimerTV.text = String.format("%02d:%02d", (time / 1000) / 60, (time / 1000) % 60)
        blackTimerTV.text = String.format("%02d:%02d", (time / 1000) / 60, (time / 1000) % 60)
        board = Board()
        if (gameMode == 2) setRandomPieces(board)
        chessboardGameView.setBoardPosition(board)
        selectedSquare = null
        chessboardGameView.setLegalMoves(emptyList())
        chessboardGameView.setSelectedSquare(selectedSquare)
    }
}