package com.loyer.storytracking.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Story(
        @SerializedName("id")
        val id: Int,
        @SerializedName("name")
        val name: String,
        @SerializedName("video_url")
        val url: String
) : Serializable