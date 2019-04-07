package com.loyer.storytracking.model

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Created by celikrecep on 5.04.2019.
 */
object UserStore {
    private lateinit var users: List<User>

    fun loadUsers(context: Context) {
        val json = loadJSONFromAsset("users.json", context)
        val listType = object : TypeToken<List<User>>() {}.type
        val gson = Gson()
        users = gson.fromJson(json, listType)
    }

    fun getUsers() = users

    private fun loadJSONFromAsset(filename: String, context: Context): String? {
        var json: String? = null
        try {
            val inputStream = context.assets.open(filename)
            val size = inputStream.available()

            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            json = String(buffer)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return json
    }
}