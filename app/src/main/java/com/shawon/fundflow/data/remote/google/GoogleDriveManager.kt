package com.shawon.fundflow.data.remote.google

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.ByteArrayContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.Collections
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleDriveManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val jsonFactory = GsonFactory.getDefaultInstance()
    private val httpTransport = NetHttpTransport()

    private fun getDriveService(): Drive? {
        val account = GoogleSignIn.getLastSignedInAccount(context) ?: return null
        val credential = GoogleAccountCredential.usingOAuth2(
            context, Collections.singleton(DriveScopes.DRIVE_APPDATA)
        ).apply {
            selectedAccount = account.account
        }

        return Drive.Builder(httpTransport, jsonFactory, credential)
            .setApplicationName("FundFlow")
            .build()
    }

    suspend fun uploadBackup(jsonString: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val service = getDriveService() ?: throw Exception("Not signed in")
            
            // Search for existing backup file in appDataFolder
            val result = service.files().list()
                .setSpaces("appDataFolder")
                .setQ("name = 'fundflow_backup.json'")
                .execute()
            
            val existingFile = result.files.firstOrNull()

            val metadata = File().apply {
                name = "fundflow_backup.json"
                parents = Collections.singletonList("appDataFolder")
            }

            val content = ByteArrayContent.fromString("application/json", jsonString)

            if (existingFile != null) {
                service.files().update(existingFile.id, null, content).execute()
            } else {
                service.files().create(metadata, content).execute()
            }
            Unit
        }
    }

    suspend fun downloadBackup(): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val service = getDriveService() ?: throw Exception("Not signed in")
            
            val result = service.files().list()
                .setSpaces("appDataFolder")
                .setQ("name = 'fundflow_backup.json'")
                .execute()
            
            val file = result.files.firstOrNull() ?: throw Exception("No backup found on Drive")

            val outputStream = ByteArrayOutputStream()
            service.files().get(file.id).executeMediaAndDownloadTo(outputStream)
            outputStream.toString("UTF-8")
        }
    }
}
