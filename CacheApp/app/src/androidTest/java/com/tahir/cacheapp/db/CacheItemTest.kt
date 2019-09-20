package com.tahir.cacheapp.db

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException


@RunWith(AndroidJUnit4::class)
class CacheItemTest{

    private lateinit var cacheItemDao: CacheItemDao
    private lateinit var db: CacheDB

    @Before
    fun createInMemoryDatabase() {

        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, CacheDB::class.java).build()
        cacheItemDao = db.cacheItemDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDatabase() {
        db.close()
    }


    @Test
    @Throws(Exception::class)
    fun databaseIsOpen_writeAndRead_shouldWriteAndReadRecordCorrectly() {

        val url = "http://sampleurl.com/"
        val response = "[{\"name\": \"consectetur\"}]"
        val cacheItem = CacheItem(url, response)

        cacheItemDao.insert(cacheItem)

        val itemFromDB = cacheItemDao.getCachedResponse(url)
        assertNotNull(itemFromDB)

        assertEquals(url, itemFromDB?.url)
        assertEquals(response, itemFromDB?.response)

    }

    @Test
    fun databaseIsOpen_clearDatabase_shouldClearAllRecordsInDatabase(){

        cacheItemDao.clearCache()

        //lets add new item
        val url = "http://sampleurl.com/"
        val response = "[{\"name\": \"consectetur\"}]"
        val cacheItem = CacheItem(url, response)
        cacheItemDao.insert(cacheItem)

        //fetch count to see if save was successful
        var count = cacheItemDao.getRowCount()
        assertNotEquals(0, count)

        //Now clear
        cacheItemDao.clearCache()
        //check count
        count = cacheItemDao.getRowCount()
        assertEquals(0, count)
    }


    @Test
    fun databaseIsOpen_tryDuplicateDataAgainstUrl_shouldOverrideExistingRecord(){

        // Add data against a url
        val url = "http://sampleurl.com/"
        val response = "[{\"name\": \"consectetur\"}]"
        val cacheItem = CacheItem(url, response)
        cacheItemDao.insert(cacheItem)
        // read saved item1
        val itemFromDB = cacheItemDao.getCachedResponse(url)

        // Add new response against same url
        val response2 = "[{\"name\": \"altered\"}]"
        val cacheItem2 = CacheItem(url, response2)
        cacheItemDao.insert(cacheItem2)
        // read saved item2
        val itemFromDB2 = cacheItemDao.getCachedResponse(url)

        //both item1 and item2's urls should be same
        assertEquals(itemFromDB?.url, itemFromDB2?.url)

        // but not respose data
        assertNotEquals(itemFromDB?.response, itemFromDB2?.response)


    }


}