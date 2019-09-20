package com.tahir.cacheapp.model

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tahir.cacheapp.db.CacheDB
import com.tahir.cacheapp.db.CacheItemDao
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException


@RunWith(AndroidJUnit4::class)
class CacheRepositoryTest{

    private lateinit var cacheItemDao: CacheItemDao
    private lateinit var db: CacheDB
    private lateinit var cacheRepository: CacheRepository

    @Before
    fun createDbAndRepository() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, CacheDB::class.java).build()
        cacheItemDao = db.cacheItemDao()

        cacheRepository = CacheRepository(cacheItemDao)
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun providedCacheItemDao_createRepository_shouldCreateRepository(){
        assertNotNull(cacheRepository)
    }


    @Test
    @Throws(Exception::class)
    fun givenCacheRepository_writeAndRead_shouldWriteAndReadDatabase() {

        val url = "http://sampleurl.com/"
        val response = "[{\"name\": \"consectetur\"}]"

        cacheRepository.save(url, response)

        val responseFromRepo = cacheRepository.getResponse(url)

        assertEquals(response, responseFromRepo)
    }


}