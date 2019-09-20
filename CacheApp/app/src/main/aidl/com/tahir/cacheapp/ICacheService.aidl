package com.tahir.cacheapp;

import com.tahir.cacheapp.ICacheResponseCallback;

interface ICacheService {

    /**
     * Load data from db against your key{url} and respond back to called via callback
     */
    void getData(String key, in ICacheResponseCallback callback);


     /**
     * Save your key value data in cache
     */
    void saveData(String key, String value);
}
