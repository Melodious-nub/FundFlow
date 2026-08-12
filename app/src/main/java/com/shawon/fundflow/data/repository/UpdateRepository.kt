package com.shawon.fundflow.data.repository

import com.shawon.fundflow.data.remote.UpdateApiService
import com.shawon.fundflow.data.remote.model.GitHubRelease
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateRepository @Inject constructor(
    private val apiService: UpdateApiService
) {
    suspend fun getLatestRelease(): Result<GitHubRelease> {
        return runCatching {
            apiService.getLatestRelease()
        }
    }
}
