package com.example.chessapp

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import com.github.bhlangonijr.chesslib.Board
import com.github.bhlangonijr.chesslib.Piece
import com.github.bhlangonijr.chesslib.Rank
import com.github.bhlangonijr.chesslib.Side
import com.github.bhlangonijr.chesslib.Square
import com.github.bhlangonijr.chesslib.move.Move

class ChessBoardGameView : View {

    private var onSquareClickListener: ((Char, Int) -> Unit)? = null
    private val paint = Paint()
    private var board: Board? = null
    private val legalMoves = mutableListOf<Square>()
    private var selectedSquare: Square? = null
    private var selectedPiece: Piece? = null
    private var isFlipped: Boolean = false

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    init {
        paint.color = Color.BLACK
        paint.textSize = 40f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        paint.color = Color.BLACK and (200 shl 24)

        if (isFlipped) canvas.rotate(180f, width / 2f, height / 2f)

        //Draw a black background
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat() - 50f, paint)
        canvas.translate(0f, 50f) //Move the view down by 50f

        drawChessboard(canvas)

        drawLegalMoves(canvas)

        drawSelectedSquare(canvas)

        drawPieces(canvas)
    }

    private fun drawChessboard(canvas: Canvas) {
        val darkSquareColor = Color.parseColor("#8B4513")
        val lightSquareColor = Color.parseColor("#FFDAB9")
        val squareSize = width / 8f

        for (rank in 1..8) {
            for (file in 'A'..'H') {
                val x = (file - 'A') * squareSize
                val y = (8 - rank) * squareSize

                val color = if ((rank + file.code) % 2 == 0) lightSquareColor else darkSquareColor

                paint.color = color
                canvas.drawRect(x, y, x + squareSize, y + squareSize, paint)

                if (rank == 1) {
                    paint.color = Color.BLACK
                    paint.textSize = 25f
                    canvas.drawText(file.toString().lowercase(), x + 0.05f * squareSize, y + 0.95f * squareSize, paint)
                }
                if (file == 'A') {
                    paint.color = Color.BLACK
                    paint.textSize = 25f
                    canvas.drawText(rank.toString(), x + 0.05f * squareSize, y + 0.28f * squareSize, paint)
                }
            }
        }
    }

    private fun drawPieces(canvas: Canvas) {
        val pieceSize = width / 8f

        for (file in 'A'..'H') {
            for (rank in 1..8) {
                val square = Square.valueOf("$file$rank".uppercase())
                val x = (file - 'A') * pieceSize
                val y = (8 - rank) * pieceSize

                val piece = board!!.getPiece(square)
                if (piece != Piece.NONE) {
                    val resourceId = if (isFlipped) getPiece180ResourceId(piece) else getPieceResourceId(piece)
                    val pieceDrawable = resources.getDrawable(resourceId, null)
                    pieceDrawable.setBounds(x.toInt(), y.toInt(), (x + pieceSize).toInt(), (y + pieceSize).toInt())
                    pieceDrawable.draw(canvas)
                }

                if (board!!.isKingAttacked && square == board!!.getKingSquare(board!!.sideToMove)) {
                    val kingX = (file - 'A') * pieceSize + pieceSize / 2
                    val kingY = (8 - rank) * pieceSize + pieceSize / 2

                    val gradient = RadialGradient(
                        kingX,
                        kingY,
                        (pieceSize / 2) + 5, // Sizes
                        Color.RED,
                        Color.TRANSPARENT,
                        Shader.TileMode.CLAMP
                    )

                    val circlePaint = Paint().apply {
                        isAntiAlias = true
                        shader = gradient
                    }

                    canvas.drawCircle(kingX, kingY, pieceSize / 2, circlePaint)
                }
            }
        }
    }

    fun flipBoard() {
        isFlipped = !isFlipped
        invalidate()
    }

    private fun isCaptureMove(from: Square, to: Square): Boolean {
        val move = Move(from, to)
        return board!!.getPiece(to) != Piece.NONE && board!!.isMoveLegal(move, true)
    }

    private fun isPromoteMove(to: Square): Boolean {
        val movingPiece = board!!.getPiece(selectedSquare!!)
        return (movingPiece == Piece.WHITE_PAWN && to.rank == Rank.RANK_8) ||
                (movingPiece == Piece.BLACK_PAWN && to.rank == Rank.RANK_1)
    }

    private fun drawSelectedSquare(canvas: Canvas) {
        if (selectedSquare != null) {
            val squareSize = width / 8f
            val x = (selectedSquare!!.file.ordinal) * squareSize
            val y = (7 - selectedSquare!!.rank.ordinal) * squareSize

            val highlightColor = Color.parseColor("#00FF00") and 0x00FFFFFF or (128 shl 24)
            paint.color = highlightColor

            canvas.drawRect(x, y, x + squareSize, y + squareSize, paint)
        }
    }

    fun setSelectedSquare(square: Square?) {
        selectedSquare = square
        invalidate()
    }

    fun setSelectedPiece(piece: Piece?) {
        selectedPiece = piece
        invalidate()
    }

    private fun drawLegalMoves(canvas: Canvas) {
        val highlightColor = Color.parseColor("#00FF00") and 0x00FFFFFF or (128 shl 24)
        val highlightColorCap = Color.RED and 0x00FFFFFF or (128 shl 24)
        val squareSize = width / 8f

        for (legalMoveSquare in legalMoves) {
            val x = (legalMoveSquare.file.ordinal) * squareSize
            val y = (7 - legalMoveSquare.rank.ordinal) * squareSize

            if(isCaptureMove(selectedSquare!!, legalMoveSquare) || isPromoteMove(legalMoveSquare)) {
                paint.color = highlightColorCap
                canvas.drawRect(x, y, x + squareSize, y + squareSize, paint)
                invalidate()
            } else {
                paint.color = highlightColor
                canvas.drawCircle(
                    x + squareSize / 2,
                    y + squareSize / 2,
                    squareSize / 6,
                    paint
                )
            }
        }
    }

    fun setLegalMoves(newLegalMoves: List<Square>) {
        legalMoves.clear()
        legalMoves.addAll(newLegalMoves)
        invalidate()
    }

    private fun getPieceResourceId(piece: Piece): Int {
        return when (piece) {
            Piece.WHITE_PAWN -> R.drawable.pawn_light
            Piece.WHITE_KNIGHT -> R.drawable.knight_light
            Piece.WHITE_BISHOP -> R.drawable.bishop_light
            Piece.WHITE_ROOK -> R.drawable.rook_light
            Piece.WHITE_QUEEN -> R.drawable.queen_light
            Piece.WHITE_KING -> R.drawable.king_light
            Piece.BLACK_PAWN -> R.drawable.pawn_dark
            Piece.BLACK_KNIGHT -> R.drawable.knight_dark
            Piece.BLACK_BISHOP -> R.drawable.bishop_dark
            Piece.BLACK_ROOK -> R.drawable.rook_dark
            Piece.BLACK_QUEEN -> R.drawable.queen_dark
            Piece.BLACK_KING -> R.drawable.king_dark
            else -> 0
        }
    }

    private fun getPiece180ResourceId(piece: Piece): Int {
        return when (piece) {
            Piece.WHITE_PAWN -> R.drawable.pawn_light_180
            Piece.WHITE_KNIGHT -> R.drawable.knight_light_180
            Piece.WHITE_BISHOP -> R.drawable.bishop_light_180
            Piece.WHITE_ROOK -> R.drawable.rook_light_180
            Piece.WHITE_QUEEN -> R.drawable.queen_light_180
            Piece.WHITE_KING -> R.drawable.king_light_180
            Piece.BLACK_PAWN -> R.drawable.pawn_dark_180
            Piece.BLACK_KNIGHT -> R.drawable.knight_dark_180
            Piece.BLACK_BISHOP -> R.drawable.bishop_dark_180
            Piece.BLACK_ROOK -> R.drawable.rook_dark_180
            Piece.BLACK_QUEEN -> R.drawable.queen_dark_180
            Piece.BLACK_KING -> R.drawable.king_dark_180
            else -> 0
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP) {
            val x: Float
            val y: Float
            if (isFlipped) {
                x = width - event.x
                y = height - event.y - 50f
            } else {
                x = event.x
                y = event.y - 50f
            }
            val file = ('A'.code + (x / (width / 8))).toInt().toChar()
            val rank = 8 - (y / (width / 8)).toInt()
            // Check if we're clicking on the chess board or not, prevent crashing
            if (file in 'A'..'H' && rank in 1..8) {
                onSquareClickListener?.invoke(file, rank)
            }
        }
        return true
    }

    fun setBoardPosition(newBoard: Board) {
        board = newBoard
        invalidate()
    }

    fun setOnSquareClickListener(listener: (Char, Int) -> Unit) {
        onSquareClickListener = listener
    }
}