package com.maxrave.simpmusic.expect

expect fun getDeviceAbi(): String?

expect fun getAppCacheDir(): String

expect fun installDownloadedApk(
    apkPath: String,
    onRequiresPermission: (() -> Unit)? = null,
    onError: ((Throwable) -> Unit)? = null,
)
