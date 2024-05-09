package com.example.plants

import android.content.Context

// DBHelperProvider.kt
object DBHelperProvider {
    private var instance: DBHelper? = null

    fun getDBHelper(context: Context): DBHelper {
        if (instance == null) {
            instance = DBHelper(context.applicationContext)
        }
        return instance!!
    }
}
