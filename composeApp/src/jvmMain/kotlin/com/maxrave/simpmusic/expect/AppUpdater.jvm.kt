package com.maxrave.simpmusic.expect

actual fun getDeviceAbi(): String? = null

actual fun getAppCacheDir(): String = System.getProperty("java.io.tmpdir")

actual fun installDownloadedApk(
    apkPath: String,
    onRequiresPermission: (() -> Unit)?,
    onError: ((Throwable) -> Unit)?,
) {
    // Desktop no instala APKs
}
