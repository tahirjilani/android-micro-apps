package com.tahir.cacheapp.model

import com.tahir.cacheapp.db.CacheItem
import com.tahir.cacheapp.db.CacheItemDao

class CacheRepository(private val cacheItemDao: CacheItemDao) {

    fun getResponse(url:String): String? {

        val item = cacheItemDao.getCachedResponse(url)
        item?.apply {
            return response
        }
        return null
    }

    fun save(url:String, data:String){

        val item = CacheItem(url, data)
        cacheItemDao.insert(item)
    }
}