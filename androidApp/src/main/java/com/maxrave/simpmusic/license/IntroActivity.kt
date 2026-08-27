package com.maxrave.simpmusic.license

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import com.maxrave.simpmusic.MainActivity
import com.maxrave.simpmusic.R

class IntroActivity : AppCompatActivity() {
    private var routed = false

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

        root.addView(
            ImageView(this).apply {
                setImageResource(R.mipmap.ic_launcher)
                scaleType = ImageView.ScaleType.FIT_CENTER
            },
            FrameLayout.LayoutParams(dp(144), dp(144), Gravity.CENTER).apply {
                bottomMargin = dp(30)
            },
        )

        root.addView(
            ProgressBar(this).apply {
                isIndeterminate = true
            },
            FrameLayout.LayoutParams(dp(36), dp(36), Gravity.CENTER).apply {
                topMargin = dp(180)
            },
        )

        setContentView(root)
        validateSavedKey()
    }

    private fun validateSavedKey() {
        val key = LicenseManager.savedKey(this)

        if (key.isNullOrBlank()) {
            launchLicense()
            return
        }

        LicenseManager.validate(this, key) { result ->
            runOnUiThread {
                if (routed || isFinishing || isDestroyed) return@runOnUiThread

                if (result.valid) {
                    LicenseManager.saveKey(this, key)
                    LicenseManager.markSessionValidated()
                    launchMain()
                } else {
                    if (result.status != "network_error") {
                        LicenseManager.clearKey(this)
                    }
                    launchLicense(result)
                }
            }
        }
    }

    private fun launchLicense(result: LicenseManager.Result? = null) {
        if (routed) return
        routed = true
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
        if (routed) return
        routed = true

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

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    companion object {
        private const val EXTRA_FORWARD_INTENT = "pg_music_intro_forward_intent"

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
