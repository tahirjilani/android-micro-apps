package com.tahir.cacheapp.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase


/*
* Room Database that has CacheItem table
* */

@Database(entities = [CacheItem::class], version = 1, exportSchema = false)
abstract class CacheDB : RoomDatabase() {

    abstract fun cacheItemDao(): CacheItemDao

    companion object {

        @Volatile private var INSTANCE: CacheDB? = null

        fun getInstance(context: Context): CacheDB =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(context.applicationContext,
                CacheDB::class.java, "cache.db")
                .build()
    }
}