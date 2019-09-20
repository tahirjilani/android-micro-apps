package com.tahir.cacheapp.db

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CacheDBTest{


    @Test
    fun database_createNew_shouldCreateDatabase() {

        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(
            context, CacheDB::class.java
        ).build()

        assertNotNull(db)

        db.close()
    }

}