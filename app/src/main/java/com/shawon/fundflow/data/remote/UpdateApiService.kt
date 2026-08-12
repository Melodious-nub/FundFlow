package com.shawon.fundflow.data.remote

import com.shawon.fundflow.data.remote.model.GitHubRelease
import retrofit2.http.GET

interface UpdateApiService {
    @GET("repos/Melodious-nub/FundFlow/releases/latest")
    suspend fun getLatestRelease(): GitHubRelease
}
