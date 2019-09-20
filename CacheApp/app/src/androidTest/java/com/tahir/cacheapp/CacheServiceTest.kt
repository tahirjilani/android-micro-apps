package com.tahir.cacheapp

import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import androidx.test.rule.ServiceTestRule
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.junit.Rule
import org.junit.Test
import android.os.RemoteException
import org.junit.After
import org.junit.Before
import java.util.concurrent.TimeoutException


@RunWith(AndroidJUnit4::class)
@MediumTest
class CacheServiceTest{

    @get:Rule
    val serviceRule = ServiceTestRule()

    private lateinit var iBinder: IBinder
    private lateinit var iCacheService: ICacheService



    @Before
    fun bindRemoteService(){

        iBinder = serviceRule.bindService(
            Intent(ApplicationProvider.getApplicationContext<Context>(), CacheService::class.java)
        )
        iCacheService = ICacheService.Stub.asInterface(iBinder)
    }


    @After
    fun unbindRemoteService(){
        serviceRule.unbindService()
    }


    @Test
    fun cacheService_tryBindingAsRemote_bindService() {

        assertNotNull(iCacheService)
    }

    @Test
    @Throws(RemoteException::class)
    fun cacheServiceRemoteStubIsCreated_addAndReadData_shouldMatch(){

        val sample_data = "sample data"
        iCacheService.saveData("url", sample_data)

        iCacheService.getData("url", object : ICacheResponseCallback.Stub(){
            override fun onResponse(data: String?) {
                assertEquals(sample_data, data)
            }
        })
    }

}