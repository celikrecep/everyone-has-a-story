package com.loyer.storytracking.model

import android.net.Uri
import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Created by celikrecep on 6.04.2019.
 */
data class Story(
        @SerializedName("id")
        val id: Int,
        @SerializedName("name")
        val name: String,
        @SerializedName("video_url")
        val url: String
): Serializable