package com.maxrave.simpmusic.license

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.Shader
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.maxrave.simpmusic.MainActivity
import com.maxrave.simpmusic.R
import kotlin.math.PI
import kotlin.math.sin

class LicenseActivity : AppCompatActivity() {
    private lateinit var keyInput: EditText
    private lateinit var activateButton: Button
    private lateinit var progress: ProgressBar
    private lateinit var status: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.BLACK
        window.navigationBarColor = Color.BLACK
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        buildUi()

        LicenseManager.savedKey(this)?.let { savedKey ->
            keyInput.setText(savedKey)
            validate(savedKey)
        }
    }

    private fun buildUi() {
        val root =
            FrameLayout(this).apply {
                setBackgroundColor(Color.rgb(3, 3, 5))
            }

        root.addView(
            ActivationBackgroundView(this),
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT,
            ),
        )

        val scroll =
            ScrollView(this).apply {
                isFillViewport = true
            }

        val screen =
            LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                setPadding(dp(22), dp(32), dp(22), dp(32))
            }

        val card =
            LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER_HORIZONTAL
                setPadding(dp(26), dp(28), dp(26), dp(28))
                background =
                    roundedBackground(
                        color = Color.rgb(13, 13, 17),
                        radius = 24,
                        strokeColor = Color.rgb(148, 18, 30),
                        strokeWidth = 1,
                    )
                elevation = dp(18).toFloat()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    outlineAmbientShadowColor = Color.rgb(120, 0, 18)
                    outlineSpotShadowColor = Color.rgb(220, 18, 38)
                }
            }

        val logoHalo =
            FrameLayout(this).apply {
                background =
                    GradientDrawable().apply {
                        shape = GradientDrawable.OVAL
                        gradientType = GradientDrawable.RADIAL_GRADIENT
                        colors =
                            intArrayOf(
                                Color.argb(115, 235, 20, 42),
                                Color.argb(35, 180, 0, 20),
                                Color.TRANSPARENT,
                            )
                        setGradientRadius(dp(68).toFloat())
                    }
            }
        logoHalo.addView(
            ImageView(this).apply {
                setImageResource(R.mipmap.ic_launcher)
                contentDescription = "PG Music"
                scaleType = ImageView.ScaleType.CENTER_INSIDE
            },
            FrameLayout.LayoutParams(dp(94), dp(94), Gravity.CENTER),
        )
        card.addView(logoHalo, LinearLayout.LayoutParams(dp(132), dp(132)))

        card.addView(
            TextView(this).apply {
                text = "LICENCIA OFICIAL"
                textSize = 10f
                gravity = Gravity.CENTER
                setTextColor(Color.rgb(255, 190, 196))
                setTypeface(typeface, Typeface.BOLD)
                setPadding(dp(12), dp(5), dp(12), dp(5))
                background =
                    roundedBackground(
                        color = Color.argb(115, 120, 0, 18),
                        radius = 20,
                        strokeColor = Color.rgb(176, 20, 38),
                        strokeWidth = 1,
                    )
            },
            wrapWrap().apply {
                topMargin = dp(2)
            },
        )

        card.addView(
            TextView(this).apply {
                text = "P G  M U S I C"
                textSize = 15f
                gravity = Gravity.CENTER
                setTextColor(Color.rgb(238, 45, 58))
                setTypeface(typeface, Typeface.BOLD)
                setPadding(0, dp(14), 0, 0)
            },
            matchWrap(),
        )

        card.addView(
            TextView(this).apply {
                text = "Activar dispositivo"
                textSize = 25f
                gravity = Gravity.CENTER
                setTextColor(Color.WHITE)
                setTypeface(typeface, Typeface.BOLD)
                setPadding(0, dp(8), 0, 0)
            },
            matchWrap(),
        )

        card.addView(
            TextView(this).apply {
                text = "Ingresá tu clave de licencia para escuchar música"
                textSize = 14f
                gravity = Gravity.CENTER
                setTextColor(Color.rgb(184, 184, 192))
                setPadding(0, dp(6), 0, dp(10))
            },
            matchWrap(),
        )

        card.addView(
            TextView(this).apply {
                text = "ACTIVACIÓN SEGURA  •  HASTA 3 DISPOSITIVOS"
                textSize = 10f
                gravity = Gravity.CENTER
                letterSpacing = 0.08f
                setTextColor(Color.rgb(206, 65, 78))
                setTypeface(typeface, Typeface.BOLD)
                setPadding(0, 0, 0, dp(22))
            },
            matchWrap(),
        )

        keyInput =
            EditText(this).apply {
                hint = "PG-XXXX-XXXX-XXXX"
                setHintTextColor(Color.rgb(115, 115, 124))
                setTextColor(Color.WHITE)
                textSize = 17f
                gravity = Gravity.CENTER
                isSingleLine = true
                inputType =
                    InputType.TYPE_CLASS_TEXT or
                    InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
                imeOptions = EditorInfo.IME_ACTION_DONE
                setPadding(dp(14), 0, dp(14), 0)
                background =
                    roundedBackground(
                        color = Color.rgb(7, 7, 10),
                        radius = 12,
                        strokeColor = Color.rgb(188, 25, 38),
                        strokeWidth = 1,
                    )
                setOnEditorActionListener { _, actionId, _ ->
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        submit()
                        true
                    } else {
                        false
                    }
                }
            }
        card.addView(keyInput, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(58)))

        activateButton =
            Button(this).apply {
                text = "ACTIVAR LICENCIA"
                textSize = 15f
                setTextColor(Color.WHITE)
                setTypeface(typeface, Typeface.BOLD)
                isAllCaps = false
                background =
                    GradientDrawable(
                        GradientDrawable.Orientation.LEFT_RIGHT,
                        intArrayOf(Color.rgb(242, 35, 48), Color.rgb(168, 0, 18)),
                    ).apply {
                        cornerRadius = dp(12).toFloat()
                        setStroke(dp(1), Color.rgb(255, 72, 84))
                    }
                setOnClickListener { submit() }
            }
        card.addView(
            activateButton,
            LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(58)).apply {
                topMargin = dp(14)
            },
        )

        progress =
            ProgressBar(this).apply {
                visibility = View.GONE
                isIndeterminate = true
            }
        card.addView(
            progress,
            LinearLayout.LayoutParams(dp(32), dp(32)).apply {
                topMargin = dp(14)
            },
        )

        status =
            TextView(this).apply {
                textSize = 14f
                gravity = Gravity.CENTER
                setTextColor(Color.rgb(225, 225, 230))
                setPadding(0, dp(10), 0, 0)
            }
        card.addView(status, matchWrap())

        card.addView(
            TextView(this).apply {
                text = "PG MUSIC  •  ACCESO PROTEGIDO"
                textSize = 10f
                gravity = Gravity.CENTER
                letterSpacing = 0.1f
                setTextColor(Color.rgb(105, 105, 114))
                setPadding(0, dp(18), 0, 0)
            },
            matchWrap(),
        )

        val availableWidth = (resources.configuration.screenWidthDp - 44).coerceAtLeast(280)
        val cardWidth = dp(minOf(availableWidth, 520))
        screen.addView(
            card,
            LinearLayout.LayoutParams(cardWidth, LinearLayout.LayoutParams.WRAP_CONTENT),
        )
        scroll.addView(screen)
        root.addView(scroll, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT))
        setContentView(root)
    }

    private fun submit() {
        validate(keyInput.text.toString())
    }

    private fun validate(key: String) {
        if (key.isBlank()) {
            showStatus("Ingresá una KEY para continuar", isError = true)
            return
        }

        setBusy(true)
        showStatus("Validando licencia…", isError = false)

        LicenseManager.validate(this, key) { result ->
            runOnUiThread {
                if (isFinishing || isDestroyed) return@runOnUiThread
                setBusy(false)

                if (result.valid) {
                    LicenseManager.saveKey(this, key)
                    LicenseManager.markSessionValidated()
                    showStatus("Licencia activa", isError = false)
                    activateButton.postDelayed({ launchMain() }, 250)
                } else {
                    if (result.status != "network_error") {
                        LicenseManager.clearKey(this)
                    }
                    showStatus(statusMessage(result), isError = true)
                }
            }
        }
    }

    private fun setBusy(busy: Boolean) {
        keyInput.isEnabled = !busy
        activateButton.isEnabled = !busy
        activateButton.alpha = if (busy) 0.55f else 1f
        progress.visibility = if (busy) View.VISIBLE else View.GONE
    }

    private fun showStatus(
        message: String,
        isError: Boolean,
    ) {
        status.text = message
        status.setTextColor(
            if (isError) {
                Color.rgb(255, 105, 115)
            } else {
                Color.rgb(225, 225, 230)
            },
        )
    }

    private fun statusMessage(result: LicenseManager.Result): String =
        when (result.status) {
            "expired" -> "La licencia venció"
            "blocked" -> "La licencia está bloqueada"
            "device_limit" -> "Se alcanzó el límite de 3 dispositivos"
            "invalid" -> "La KEY no pertenece a PG Music o no existe"
            "invalid_request" -> "La KEY ingresada no es válida"
            "network_error" -> "Sin conexión con el servidor. Intentá nuevamente"
            else -> result.message ?: "No se pudo validar la licencia"
        }

    private fun launchMain() {
        val destination =
            forwardedIntent()?.apply {
                setClass(this@LicenseActivity, MainActivity::class.java)
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

    private fun roundedBackground(
        color: Int,
        radius: Int,
        strokeColor: Int,
        strokeWidth: Int,
    ): GradientDrawable =
        GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(radius).toFloat()
            setStroke(dp(strokeWidth), strokeColor)
        }

    private fun matchWrap() =
        LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
        )

    private fun wrapWrap() =
        LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
        )

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    private class ActivationBackgroundView(
        context: Context,
    ) : View(context) {
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val wavePaint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
            }
        private val wavePath = Path()
        private val points =
            floatArrayOf(
                0.08f, 0.18f,
                0.18f, 0.31f,
                0.86f, 0.20f,
                0.92f, 0.38f,
                0.11f, 0.63f,
                0.81f, 0.69f,
                0.22f, 0.86f,
                0.72f, 0.90f,
            )

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            val width = width.toFloat()
            val height = height.toFloat()
            if (width <= 0f || height <= 0f) return

            paint.shader =
                RadialGradient(
                    width * 0.18f,
                    height * 0.16f,
                    width * 0.72f,
                    intArrayOf(
                        Color.argb(90, 205, 0, 28),
                        Color.argb(25, 120, 0, 18),
                        Color.TRANSPARENT,
                    ),
                    floatArrayOf(0f, 0.45f, 1f),
                    Shader.TileMode.CLAMP,
                )
            canvas.drawCircle(width * 0.18f, height * 0.16f, width * 0.72f, paint)

            paint.shader =
                RadialGradient(
                    width * 0.92f,
                    height * 0.72f,
                    width * 0.64f,
                    intArrayOf(
                        Color.argb(55, 150, 0, 25),
                        Color.TRANSPARENT,
                    ),
                    null,
                    Shader.TileMode.CLAMP,
                )
            canvas.drawCircle(width * 0.92f, height * 0.72f, width * 0.64f, paint)

            paint.shader =
                LinearGradient(
                    0f,
                    height * 0.28f,
                    width,
                    height * 0.62f,
                    intArrayOf(
                        Color.TRANSPARENT,
                        Color.argb(20, 255, 35, 55),
                        Color.TRANSPARENT,
                    ),
                    null,
                    Shader.TileMode.CLAMP,
                )
            canvas.drawRect(0f, 0f, width, height, paint)
            paint.shader = null

            points.forEachIndexed { index, value ->
                if (index % 2 == 0) {
                    val x = value * width
                    val y = points[index + 1] * height
                    paint.color = Color.argb(80, 235, 45, 62)
                    canvas.drawCircle(x, y, if (index % 4 == 0) 2.5f else 1.6f, paint)
                }
            }

            repeat(3) { layer ->
                wavePath.reset()
                val centerY = height * (0.76f + layer * 0.055f)
                val amplitude = height * (0.016f + layer * 0.004f)
                var x = 0f
                while (x <= width) {
                    val phase = (x / width) * PI * (3.4 + layer * 0.35)
                    val y = centerY + sin(phase + layer * 0.8).toFloat() * amplitude
                    if (x == 0f) wavePath.moveTo(x, y) else wavePath.lineTo(x, y)
                    x += 8f
                }
                wavePaint.color = Color.argb(42 - layer * 9, 235, 30, 50)
                wavePaint.strokeWidth = (2.2f - layer * 0.45f).coerceAtLeast(1f)
                canvas.drawPath(wavePath, wavePaint)
            }
        }
    }

    companion object {
        private const val EXTRA_FORWARD_INTENT = "pg_music_forward_intent"

        fun createIntent(
            context: Context,
            sourceIntent: Intent?,
        ): Intent =
            Intent(context, LicenseActivity::class.java).apply {
                sourceIntent?.let {
                    putExtra(EXTRA_FORWARD_INTENT, Intent(it))
                }
            }
    }
}
