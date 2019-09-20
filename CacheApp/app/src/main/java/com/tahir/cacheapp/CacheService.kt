package com.tahir.cacheapp

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.tahir.cacheapp.db.CacheDB
import com.tahir.cacheapp.model.CacheRepository


private const val TAG = "CacheService"

/*
* This service will be remotely bound via AIDL and used to store/retrieve data from database which
* serves data caching purpose.
* */

class CacheService : Service() {

    private lateinit var cacheRepository: CacheRepository


    override fun onCreate() {

        super.onCreate()

        val dao = CacheDB.getInstance(this).cacheItemDao()
        cacheRepository = CacheRepository(dao)


        //TODO: Uncomment this to populate data on startup if you wish to run without internet connection
        //writeTestData()

        Log.d(TAG, "onCreate called")
    }



    /*
    * Note: We can connect multiple clients to a service simultaneously. However, the system caches the IBinder service communication channel.
    * In other words, the system calls the service's onBind() method to generate the IBinder only when the first client binds.
    * The system then delivers that same IBinder to all additional clients that bind to that same service, without calling onBind() again.
    * When the last client unbinds from the service, the system destroys the service, unless the service was also started by startService().
    *
    * This CacheService will be only bound to Proxy service hence we could think about using Messenger
    * rather than Aidl. However, what if multiple apps are using proxy service that in result generates
    * lot of calls to CacheService. Messenger will queue those calls hence can cause lag. So to make the calls
    * independent of queuing we prefer AIDL. Each request to read/write data will be done in separate background thread
    * regardless of remote.
    * */
    override fun onBind(intent: Intent): IBinder {

        Log.d(TAG, "onBind called")
        return binder
    }


    /*
    * Save sample data in db
    * */
    private fun writeTestData() {

        Thread(Runnable {

            saveDataInDb(Constants.URL, Constants.RESPONSE)
        }).start()
    }


    private val binder = object : ICacheService.Stub() {

        /**
         * Get data from db against your url.
         *
         * Note that unlike other application components, calls on to the
         * IBinder interface returned here may not happen on the main thread
         * of the process. However, we must be prepared for incoming calls from unknown threads,
         * with multiple calls happening at the same time. In other words,
         * an implementation of an AIDL interface must be completely thread-safe. Hence we call
         * readDataAsync and saveData in separate threads regardless of remote client's thread state.
         *
         */
        override fun getData(key: String?, callback: ICacheResponseCallback) {

            Log.d(TAG, "getData called")

            Thread(Runnable {

                readDataAsync(key!!, callback)
            }).start()

        }

        /**
         * Save your key value data in cache.
         */
        override fun saveData(key: String?, value: String?) {

            Log.d(TAG, "saveData() called with: $value against: $key")

            if (!key.isNullOrEmpty() && !value.isNullOrEmpty()) {

                Thread(Runnable {
                    saveDataInDb(key, value)
                }).start()
            }
        }

    }


    /**
     * Read data from database and return to the client via ICacheResponseCallback.
     * */
    private fun readDataAsync(key: String, callback: ICacheResponseCallback) {

        val cacheRecord = cacheRepository.getResponse(key)

        Log.d(TAG, "Data found from cache: $cacheRecord against: $key")

        callback.onResponse(cacheRecord)
    }

    /*
    * Save data in db
    * */
    private fun saveDataInDb(key: String, value: String){
        Log.d(TAG, "Saving data: $value against: $key")
        cacheRepository.save(key, value)
    }

}
