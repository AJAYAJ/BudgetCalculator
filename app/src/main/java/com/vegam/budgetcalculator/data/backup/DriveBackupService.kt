package com.vegam.budgetcalculator.data.backup

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.URL
import java.net.URLEncoder
import java.security.MessageDigest
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import javax.net.ssl.HttpsURLConnection

class DriveAuthorizationExpired : IOException("Google Drive access expired. Please try again to reconnect.")

data class DriveBackupFile(val id: String, val modifiedTime: String)

@Singleton
class DriveBackupService @Inject constructor() {
    suspend fun latest(token: String, userId: String): DriveBackupFile? = withContext(Dispatchers.IO) {
        val query = URLEncoder.encode("name = '${fileName(userId)}' and trashed = false", "UTF-8")
        val response = request(token, "GET", "https://www.googleapis.com/drive/v3/files" +
            "?spaces=appDataFolder&q=$query&orderBy=modifiedTime%20desc&pageSize=1&fields=files(id,modifiedTime)")
        val files = JSONObject(response).getJSONArray("files")
        if (files.length() == 0) null else files.getJSONObject(0).let {
            DriveBackupFile(it.getString("id"), it.getString("modifiedTime"))
        }
    }

    suspend fun upload(token: String, userId: String, json: String): Unit = withContext(Dispatchers.IO) {
        val data = json.toByteArray(Charsets.UTF_8)
        require(data.size <= MAX_BYTES) { "Backup exceeds the 20 MB limit." }
        val boundary = "budget-${UUID.randomUUID()}"
        val metadata = JSONObject().put("name", fileName(userId))
            .put("parents", org.json.JSONArray().put("appDataFolder"))
        val body = ByteArrayOutputStream().apply {
            write(("--$boundary\r\nContent-Type: application/json; charset=UTF-8\r\n\r\n" +
                "$metadata\r\n--$boundary\r\nContent-Type: application/json\r\n\r\n").toByteArray())
            write(data)
            write("\r\n--$boundary--\r\n".toByteArray())
        }.toByteArray()
        val response = request(token, "POST",
            "https://www.googleapis.com/upload/drive/v3/files?uploadType=multipart&fields=id",
            body, "multipart/related; boundary=$boundary")
        check(JSONObject(response).getString("id").isNotBlank()) { "Drive did not confirm the backup." }
    }

    suspend fun download(token: String, file: DriveBackupFile): String = withContext(Dispatchers.IO) {
        require(file.id.matches(Regex("[A-Za-z0-9_-]+"))) { "Invalid Drive file ID." }
        request(token, "GET", "https://www.googleapis.com/drive/v3/files/${file.id}?alt=media")
    }

    private fun fileName(userId: String): String {
        val hash = MessageDigest.getInstance("SHA-256").digest(userId.toByteArray())
            .joinToString("") { "%02x".format(it) }
        return "budget-calculator-$hash-v1.json"
    }

    private fun request(
        token: String,
        method: String,
        url: String,
        body: ByteArray? = null,
        contentType: String = "application/json"
    ): String {
        val connection = URL(url).openConnection() as HttpsURLConnection
        try {
            connection.requestMethod = method
            connection.connectTimeout = 30_000
            connection.readTimeout = 60_000
            connection.instanceFollowRedirects = false
            connection.setRequestProperty("Authorization", "Bearer $token")
            if (body != null) {
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", contentType)
                connection.setFixedLengthStreamingMode(body.size)
                connection.outputStream.use { it.write(body) }
            }
            when (val status = connection.responseCode) {
                401 -> throw DriveAuthorizationExpired()
                403 -> throw IOException("Drive access denied. Check Drive API setup, permission, and storage quota.")
                404 -> throw IOException("Backup no longer exists. Check Google Drive again.")
                in 200..299 -> Unit
                else -> throw IOException("Google Drive request failed ($status). Check your connection and try again.")
            }
            return connection.inputStream.use { input ->
                val output = ByteArrayOutputStream()
                val buffer = ByteArray(8192)
                while (true) {
                    val count = input.read(buffer)
                    if (count == -1) break
                    if (output.size() + count > MAX_BYTES) throw IOException("Backup exceeds the 20 MB limit.")
                    output.write(buffer, 0, count)
                }
                output.toString("UTF-8")
            }
        } finally {
            connection.disconnect()
        }
    }

    companion object {
        const val SCOPE = "https://www.googleapis.com/auth/drive.appdata"
        private const val MAX_BYTES = 20 * 1024 * 1024
    }
}
