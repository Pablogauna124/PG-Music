package com.maxrave.simpmusic.update

import com.maxrave.simpmusic.expect.getDeviceAbi
import com.maxrave.simpmusic.utils.VersionManager
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.HttpResponse
import io.ktor.http.isSuccess
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class PgMusicReleaseAsset(
    val name: String,
    @SerialName("browser_download_url")
    val browserDownloadUrl: String,
    val size: Long = 0L,
)

@Serializable
data class PgMusicRelease(
    @SerialName("tag_name")
    val tagName: String,
    val name: String? = null,
    val body: String? = null,
    @SerialName("published_at")
    val publishedAt: String? = null,
    val prerelease: Boolean = false,
    val draft: Boolean = false,
    val assets: List<PgMusicReleaseAsset> = emptyList(),
)

data class PgMusicUpdate(
    val version: String,
    val releaseName: String,
    val releaseNotes: String,
    val publishedAt: String?,
    val downloadUrl: String,
    val assetName: String,
    val assetSize: Long = 0L,
)

object PgMusicUpdateManager {

    private const val LATEST_RELEASE_URL =
        "https://api.github.com/repos/Pablogauna124/PG-Music/releases/latest"

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    suspend fun checkForUpdate(client: HttpClient): PgMusicUpdate? {
        val response: HttpResponse =
            client.get(LATEST_RELEASE_URL) {
                header("Accept", "application/vnd.github+json")
                header("X-GitHub-Api-Version", "2022-11-28")
                header("User-Agent", "PG-Music-Android")
            }

        if (!response.status.isSuccess()) {
            return null
        }

        val rawBody: String = response.body()
        val release = json.decodeFromString<PgMusicRelease>(rawBody)

        if (release.draft || release.prerelease) {
            return null
        }

        val remoteVersion = normalizeVersion(release.tagName)
        val localVersion = normalizeVersion(VersionManager.getVersionName())

        if (!isNewerVersion(remoteVersion, localVersion)) {
            return null
        }

        val chosenAsset = selectBestAsset(release.assets) ?: return null

        return PgMusicUpdate(
            version = remoteVersion,
            releaseName = release.name ?: "PG Music $remoteVersion",
            releaseNotes = release.body.orEmpty(),
            publishedAt = release.publishedAt,
            downloadUrl = chosenAsset.browserDownloadUrl,
            assetName = chosenAsset.name,
            assetSize = chosenAsset.size,
        )
    }

    internal fun selectBestAsset(assets: List<PgMusicReleaseAsset>): PgMusicReleaseAsset? {
        val apkAssets = assets.filter { it.name.endsWith(".apk", ignoreCase = true) }
        if (apkAssets.isEmpty()) return null

        val deviceAbi = getDeviceAbi()?.lowercase().orEmpty()

        if (deviceAbi.isNotEmpty()) {
            val abiMatch = apkAssets.firstOrNull { it.name.lowercase().contains(deviceAbi) }
            if (abiMatch != null) return abiMatch

            if (deviceAbi.contains("arm64")) {
                val v8 = apkAssets.firstOrNull { it.name.lowercase().contains("v8a") || it.name.lowercase().contains("arm64") }
                if (v8 != null) return v8
            } else if (deviceAbi.contains("v7a") || deviceAbi.contains("arm")) {
                val v7 = apkAssets.firstOrNull { it.name.lowercase().contains("v7a") || it.name.lowercase().contains("armeabi") }
                if (v7 != null) return v7
            }
        }

        val universal = apkAssets.firstOrNull { it.name.lowercase().contains("universal") }
        if (universal != null) return universal

        return apkAssets.firstOrNull()
    }

    internal fun normalizeVersion(version: String): String =
        version
            .trim()
            .removePrefix("v")
            .removePrefix("V")
            .substringBefore("-")

    internal fun isNewerVersion(
        remote: String,
        local: String,
    ): Boolean {
        val remoteParts = remote.split(".").map { it.toIntOrNull() ?: 0 }
        val localParts = local.split(".").map { it.toIntOrNull() ?: 0 }

        val size = maxOf(remoteParts.size, localParts.size)

        for (index in 0 until size) {
            val remotePart = remoteParts.getOrElse(index) { 0 }
            val localPart = localParts.getOrElse(index) { 0 }

            if (remotePart > localPart) return true
            if (remotePart < localPart) return false
        }

        return false
    }
}
