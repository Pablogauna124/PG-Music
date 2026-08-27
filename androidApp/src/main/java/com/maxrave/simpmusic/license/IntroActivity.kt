package com.maxrave.simpmusic.license

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import com.maxrave.simpmusic.MainActivity
import com.maxrave.simpmusic.R

class IntroActivity : AppCompatActivity() {
    private val handler = Handler(Looper.getMainLooper())
    private val videoTimeout = Runnable { onVideoFinished() }

    private lateinit var video: FullscreenVideoView
    private lateinit var progress: ProgressBar

    private var videoFinished = false
    private var validationFinished = false
    private var routed = false
    private var validationResult: LicenseManager.Result? = null
    private var candidateKey: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
        )
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        enterFullscreen()

        val root =
            FrameLayout(this).apply {
                setBackgroundColor(Color.BLACK)
            }

        video =
            FullscreenVideoView(this).apply {
                setBackgroundColor(Color.BLACK)
                setOnPreparedListener { player ->
                    player.isLooping = false
                    player.setVideoScalingMode(
                        MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING,
                    )
                    setBackgroundColor(Color.TRANSPARENT)
                    start()
                    handler.postDelayed(videoTimeout, VIDEO_TIMEOUT_MS)
                }
                setOnCompletionListener {
                    onVideoFinished()
                }
                setOnErrorListener { _, _, _ ->
                    onVideoFinished()
                    true
                }
            }

        progress =
            ProgressBar(this).apply {
                visibility = View.GONE
                isIndeterminate = true
            }

        root.addView(
            video,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT,
            ),
        )
        root.addView(
            progress,
            FrameLayout.LayoutParams(dp(42), dp(42), Gravity.CENTER),
        )
        setContentView(root)

        video.setVideoURI(
            Uri.parse("android.resource://" + packageName + "/" + R.raw.pg_music_intro),
        )
        video.requestFocus()

        validateSavedKey()
    }

    override fun onDestroy() {
        handler.removeCallbacks(videoTimeout)
        if (::video.isInitialized) {
            video.stopPlayback()
        }
        super.onDestroy()
    }

    private fun validateSavedKey() {
        candidateKey = LicenseManager.savedKey(this)
        val key = candidateKey

        if (key.isNullOrBlank()) {
            validationFinished = true
            maybeNavigate()
            return
        }

        LicenseManager.validate(this, key) { result ->
            runOnUiThread {
                if (isFinishing || isDestroyed) return@runOnUiThread

                validationResult = result
                validationFinished = true

                if (result.valid) {
                    LicenseManager.saveKey(this, key)
                    LicenseManager.markSessionValidated()
                } else if (result.status != "network_error") {
                    LicenseManager.clearKey(this)
                }

                if (videoFinished) {
                    progress.visibility = View.GONE
                }
                maybeNavigate()
            }
        }
    }

    private fun onVideoFinished() {
        if (videoFinished) return
        videoFinished = true
        handler.removeCallbacks(videoTimeout)

        if (!validationFinished) {
            progress.visibility = View.VISIBLE
        }
        maybeNavigate()
    }

    private fun maybeNavigate() {
        if (routed || !videoFinished || !validationFinished) return
        routed = true

        val result = validationResult
        when {
            candidateKey.isNullOrBlank() -> launchLicense()
            result?.valid == true -> launchMain()
            else -> launchLicense(result)
        }
    }

    private fun launchLicense(result: LicenseManager.Result? = null) {
        startActivity(
            LicenseActivity.createIntent(
                context = this,
                sourceIntent = forwardedIntent(),
                errorStatus = result?.status,
                errorMessage = result?.message,
            ),
        )
        finish()
    }

    private fun launchMain() {
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

    private fun enterFullscreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            window.insetsController?.let { controller ->
                controller.hide(
                    WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars(),
                )
                controller.systemBarsBehavior =
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        }
    }

    private class FullscreenVideoView(
        context: Context,
    ) : VideoView(context) {
        override fun onMeasure(
            widthMeasureSpec: Int,
            heightMeasureSpec: Int,
        ) {
            setMeasuredDimension(
                View.MeasureSpec.getSize(widthMeasureSpec),
                View.MeasureSpec.getSize(heightMeasureSpec),
            )
        }
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    companion object {
        private const val EXTRA_FORWARD_INTENT = "pg_music_intro_forward_intent"
        private const val VIDEO_TIMEOUT_MS = 8_000L

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
