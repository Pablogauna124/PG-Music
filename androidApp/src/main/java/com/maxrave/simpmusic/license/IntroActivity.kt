package com.maxrave.simpmusic.license

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import com.maxrave.simpmusic.MainActivity
import com.maxrave.simpmusic.R

class IntroActivity : AppCompatActivity() {
    private val handler = Handler(Looper.getMainLooper())
    private val timeout = Runnable { finishIntro() }
    private var completed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
        )
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val video =
            VideoView(this).apply {
                setBackgroundColor(Color.BLACK)
                setOnPreparedListener { player ->
                    player.isLooping = false
                    setBackgroundColor(Color.TRANSPARENT)
                    start()
                }
                setOnCompletionListener {
                    finishIntro()
                }
                setOnErrorListener { _, _, _ ->
                    finishIntro()
                    true
                }
            }

        setContentView(video)
        video.setVideoURI(
            Uri.parse("android.resource://" + packageName + "/" + R.raw.pg_music_intro),
        )
        video.requestFocus()
        handler.postDelayed(timeout, INTRO_TIMEOUT_MS)
    }

    override fun onDestroy() {
        handler.removeCallbacks(timeout)
        super.onDestroy()
    }

    private fun finishIntro() {
        if (completed || isFinishing) return
        completed = true
        handler.removeCallbacks(timeout)

        val destination =
            forwardedIntent()?.apply {
                setClass(this@IntroActivity, MainActivity::class.java)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            } ?: Intent(this, MainActivity::class.java)

        startActivity(destination)
        finish()
    }

    @Suppress("DEPRECATION")
    private fun forwardedIntent(): Intent? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(EXTRA_FORWARD_INTENT, Intent::class.java)
        } else {
            intent.getParcelableExtra(EXTRA_FORWARD_INTENT)
        }

    companion object {
        private const val EXTRA_FORWARD_INTENT = "pg_music_intro_forward_intent"
        private const val INTRO_TIMEOUT_MS = 6_500L

        fun createIntent(
            context: Context,
            sourceIntent: Intent?,
        ): Intent =
            Intent(context, IntroActivity::class.java).apply {
                sourceIntent?.let {
                    putExtra(EXTRA_FORWARD_INTENT, Intent(it))
                }
            }
    }
}
