package com.tahir.cacheapp.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/*
*
* Database entity that will hold url and its response.
* We will use this class as our daa model
* */

@Entity
class CacheItem (


    /*
    * One url will have one response. So we made it as primary key
    * */
    @PrimaryKey
    @ColumnInfo(name = "url")
    val url: String,


    /*
    * Response against a url
    * */
    @ColumnInfo(name = "response")
    val response: String? = null
)