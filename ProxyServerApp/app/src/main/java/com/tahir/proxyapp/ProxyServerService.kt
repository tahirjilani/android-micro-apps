package com.tahir.proxyapp

import android.app.Service
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Handler
import android.os.IBinder
import android.os.SystemClock
import android.util.Log
import android.widget.Toast
import com.tahir.cacheapp.ICacheService
import com.tahir.proxyapp.server.ProxyServer
import com.tahir.proxyapp.server.ConnectionStatusListener
import java.io.IOException


/*
* This service starts/stops Proxy Server when its created and destroyed respectively.
*
* */
class ProxyServerService: Service() {


    private val TAG = "ProxyServerService"

    //Proxy server instance
    private var mProxyServer: ProxyServer? = null
    private var isProxyServerConnected = false

    //Remote CacheService reference to get and save data. ProxyServer will use it to get
    // data from CacheService otherwise save fresh data from Internet.
    private var iCacheService: ICacheService? = null


    /**
     * Called by the system when the service is first created.
     */
    override fun onCreate() {
        super.onCreate()

        //start server when service was started.
        startProxyServer()

        Log.d(TAG, "onCreate called")

    }



    /**
     * Return the communication channel to the service.  May return null if
     * clients can not bind to the service.
     * @return Return an IBinder through which clients can call on to the
     * service.
     */
    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        return super.onUnbind(intent)
    }

    private val binder = object : IProxyServerService.Stub() {
        override fun getStatus(callback: IProxyStatusCallback?) {

            if(isProxyServerConnected){
                callback?.onStatus(true, ProxyServer.HOST, ProxyServer.PORT)
            }else {

                //It can take sometime to start proxy server socket and binding remote cache service
                // lets wait for 10 seconds before we get back to proxy client.
                var waitStart = SystemClock.elapsedRealtime()
                var currentTime = SystemClock.elapsedRealtime()
                //lets wait for max 10 seconds.
                while (!isProxyServerConnected && currentTime < waitStart + 10000){
                    currentTime = SystemClock.elapsedRealtime()
                    Log.d(TAG, "Waiting for proxy server socket to run")
                }
                callback?.onStatus(isProxyServerConnected, ProxyServer.HOST, ProxyServer.PORT)
            }
        }

    }




    /*
    * Called when service is stopped
    * */
    override fun onDestroy() {
        //stop server while stopping this service
        stopProxyServer()

        //close connection with remote CacheService
        unbindRemoteCacheService()

        super.onDestroy()
        Log.d(TAG, "onDestroy called")

    }


    /*
    * Start proxy server. When started, bind CacheService
    * */
    private fun startProxyServer(){

        if(mProxyServer != null){
            Log.d(TAG, "Server socket already running")
            return
        }

        mProxyServer = ProxyServer(object :ConnectionStatusListener{
            override fun onStatusChange(isConnected: Boolean) {
                if(isConnected){

                    isProxyServerConnected = true
                    //Create connection to remote CacheService
                    Handler(mainLooper).post{
                        bindRemoteCacheService()
                    }
                }
            }

        })
        mProxyServer?.startServer()

    }

    /*
    * Stop server
    * */
    private fun stopProxyServer(){
        mProxyServer?.shutdown()
        mProxyServer = null
    }


    /*
    * create connection to the remote CacheService within com.tahir.cacheapp application.
    * successful connection will trigger mCacheServiceConnection.onServiceConnected(..)
    * */
    private fun bindRemoteCacheService(){

        var it = Intent()
        it.component = ComponentName("com.tahir.cacheapp", "com.tahir.cacheapp.CacheService")
        bindService(it, mCacheServiceConnection, Service.BIND_AUTO_CREATE)

    }


    /*
   * close remote service connection. It will not trigger mCacheServiceConnection.onServiceDisconnected(..)
   * */
    private fun unbindRemoteCacheService() {
        unbindService(mCacheServiceConnection)
    }



    /**
     * ServiceConnection that will hold connection to the remote CacheService
     * within com.tahir.cacheapp application
     * */
    private val mCacheServiceConnection = object : ServiceConnection {

        /*
         Called when the connection with the service is established
         */
        override fun onServiceConnected(className: ComponentName, service: IBinder) {

            //This gets an instance of the ICacheService, which we can use to call on the service
            iCacheService = ICacheService.Stub.asInterface(service)

            //we pass this to Proxy Server to connect with  CacheService
            mProxyServer?.setCacheServiceConnection(iCacheService)

            Log.d(TAG, "CacheService Service connected")
        }


        /*
         Called when the connection with the service disconnects unexpectedly
         */
        override fun onServiceDisconnected(className: ComponentName) {

            Log.e(TAG, "CacheService Service has disconnected unexpectedly. " +
                    "Try relaunching Proxy and Cache apps afresh.")
            iCacheService = null

            Toast.makeText(baseContext, "CacheService stopped unexpectedly. Hence going to stop ProxyServer",
                Toast.LENGTH_LONG).show()
            stopSelf()

        }
    }




}
