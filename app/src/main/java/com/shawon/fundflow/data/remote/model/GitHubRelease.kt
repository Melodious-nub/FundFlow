package com.shawon.fundflow.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GitHubRelease(
    @SerialName("tag_name")
    val tagName: String,
    @SerialName("name")
    val name: String,
    @SerialName("body")
    val body: String,
    @SerialName("assets")
    val assets: List<GitHubAsset>
)

@Serializable
data class GitHubAsset(
    @SerialName("name")
    val name: String,
    @SerialName("browser_download_url")
    val downloadUrl: String,
    @SerialName("content_type")
    val contentType: String,
    @SerialName("size")
    val size: Long
)
