package com.awaran.stream

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

class MainActivity : AppCompatActivity() {
    private var player: ExoPlayer? = null
    private lateinit var playerView: PlayerView
    private lateinit var hint: View
    private val speeds = floatArrayOf(0.5f, 1f, 1.5f, 2f)
    private var speedIdx = 1

    private val picker = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { play(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        playerView = findViewById(R.id.player)
        hint = findViewById(R.id.hint)
        findViewById<View>(R.id.btnOpen).setOnClickListener { picker.launch(arrayOf("video/*", "audio/*")) }
        findViewById<View>(R.id.btnUrl).setOnClickListener { askUrl() }
        val speedBtn = findViewById<Button>(R.id.btnSpeed)
        speedBtn.setOnClickListener {
            speedIdx = (speedIdx + 1) % speeds.size
            player?.setPlaybackSpeed(speeds[speedIdx])
            speedBtn.text = "${speeds[speedIdx]}x"
        }
        intent?.data?.let { play(it) }
    }

    private fun play(uri: Uri) {
        player?.release()
        val p = ExoPlayer.Builder(this).build()
        player = p
        playerView.player = p
        p.setMediaItem(MediaItem.fromUri(uri))
        p.setPlaybackSpeed(speeds[speedIdx])
        p.prepare(); p.play()
        hint.visibility = View.GONE
    }

    private fun askUrl() {
        val input = EditText(this).apply { hint = "https://…/video.mp4 or .m3u8" }
        AlertDialog.Builder(this).setTitle("Open network stream").setView(input)
            .setPositiveButton("Play") { _, _ ->
                input.text.toString().trim().takeIf { it.isNotEmpty() }?.let { play(Uri.parse(it)) }
            }.setNegativeButton("Cancel", null).show()
    }

    override fun onStop() { super.onStop(); player?.pause() }
    override fun onDestroy() { super.onDestroy(); player?.release(); player = null }
}
