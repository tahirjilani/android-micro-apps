package com.tahir.cacheapp.db

import androidx.room.*


@Dao
interface CacheItemDao {

    /*
    * Get saved (if any) response against url
    * */
    @Query("SELECT * FROM cacheitem WHERE url LIKE :url LIMIT 1")
    fun getCachedResponse(url: String): CacheItem?


    /*
    * Save/replace record
    * */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(catchIte: CacheItem)


    /*
    * Delete all items
    * */
    @Query("DELETE FROM cacheitem")
    fun clearCache()


    /**
     * Get row count.
     * */
    @Query("SELECT COUNT(url) FROM cacheitem")
    fun getRowCount():Int

}