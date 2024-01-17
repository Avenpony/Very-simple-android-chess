package com.example.chessapp

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class GameMenu : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.game_menu)

        val nor = findViewById<Button>(R.id.singleplayer)
        val ran = findViewById<Button>(R.id.ranMode)
        val quit = findViewById<Button>(R.id.quitButton)
        val imageView = findViewById<ImageView>(R.id.imageView)
        imageView.setImageResource(R.drawable.chesspieces)

        nor.setOnClickListener {
            showTimeInputDialog(1)
        }
        ran.setOnClickListener {
            showTimeInputDialog(2)
        }
        quit.setOnClickListener {
            finishAffinity()
        }
    }

    private fun showTimeInputDialog(gameMode: Int) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.time_dialog, null)
        val etMinutes = dialogView.findViewById<EditText>(R.id.etMinutes)
        val etSeconds = dialogView.findViewById<EditText>(R.id.etSeconds)
        val etIncrement = dialogView.findViewById<EditText>(R.id.etIncrement)

        AlertDialog.Builder(this)
            .setTitle("Enter Time and Increment")
            .setView(dialogView)
            .setPositiveButton("OK") { _, _ ->
                val minutes = etMinutes.text.toString().toLongOrNull() ?: 0
                val seconds = etSeconds.text.toString().toLongOrNull() ?: 0
                val increment = etIncrement.text.toString().toLongOrNull() ?: 0

                val time = ((minutes * 60) + seconds) * 1000

                startMainActivity(gameMode, time, increment * 1000)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun startMainActivity(gameMode: Int, time: Long, increment: Long) {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("GAME_MODE", gameMode)
        intent.putExtra("TIME", time)
        intent.putExtra("INCREMENT", increment)
        startActivity(intent)
        finish()
    }
}