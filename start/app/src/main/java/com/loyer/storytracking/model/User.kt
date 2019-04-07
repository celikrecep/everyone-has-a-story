package com.loyer.storytracking.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class User(
        @SerializedName("id")
        val id: Int,
        @SerializedName("username")
        val username: String,
        @SerializedName("pic_url")
        val picUrl: String,
        @SerializedName("stories")
        val stories: List<Story>
) : Serializable {
    val path: String
        get() = "drawable/$picUrl"
}