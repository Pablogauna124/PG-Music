package com.maxrave.simpmusic.expect

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import org.koin.mp.KoinPlatform.getKoin
import java.io.File

actual fun getDeviceAbi(): String? {
    return Build.SUPPORTED_ABIS.firstOrNull()
}

actual fun getAppCacheDir(): String {
    val context: AppCompatActivity = getKoin().get()
    return context.cacheDir.absolutePath
}

actual fun installDownloadedApk(
    apkPath: String,
    onRequiresPermission: (() -> Unit)?,
    onError: ((Throwable) -> Unit)?,
) {
    try {
        val context: AppCompatActivity = getKoin().get()
        val apkFile = File(apkPath)
        if (!apkFile.exists()) {
            onError?.invoke(IllegalStateException("El archivo APK no existe en $apkPath"))
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!context.packageManager.canRequestPackageInstalls()) {
                onRequiresPermission?.invoke()
                val permissionIntent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                    data = Uri.parse("package:${context.packageName}")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(permissionIntent)
                return
            }
        }

        val apkUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.FileProvider",
            apkFile,
        )

        val installIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, "application/vnd.android.package-archive")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
        }

        context.startActivity(installIntent)
    } catch (t: Throwable) {
        onError?.invoke(t)
    }
}
