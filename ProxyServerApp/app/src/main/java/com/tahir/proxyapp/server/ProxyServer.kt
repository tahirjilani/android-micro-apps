package com.tahir.proxyapp.server

import android.util.Log
import android.widget.Toast
import com.tahir.cacheapp.ICacheService
import java.io.IOException
import java.net.ServerSocket


/**
 * Local proxy ServerSocket.
 * Only one instance is allowed to run.
 * */

class ProxyServer(private val mStateCallback:ConnectionStatusListener){


    private val TAG = "ProxyServer"

    //Server socket port on localhost
    companion object {
        val PORT = 2779
        val HOST = "localhost"
    }
    private var mServerSocket: ServerSocket? = null
    private var keepListening = false


    //Remote CacheService reference to get and save data.
    private var iCacheService: ICacheService? = null


    /**
     * Start ServerSocket in background thread.
     * */
    fun startServer(){

        //lets keep only one instance of proxy mServerSocket
        if(mServerSocket != null){

            Log.e(TAG, "Server instance is already running.")
            return
        }

        Thread(Runnable {
            createServerSocket()
        }).start()
    }


    /**
     * Shutdown ServerSocket
     * */
    fun shutdown(){
        Thread(Runnable {

            keepListening = false
            try {
                mServerSocket?.close()
                mServerSocket = null

            }catch (ioe:IOException) {
                ioe.printStackTrace()
            }

        }).start()

    }

    fun setCacheServiceConnection(iCache: ICacheService?) {
        this.iCacheService = iCache
    }


    /**
     * Start mServerSocket in background thread
     * */
    private fun createServerSocket() {


        //try to start mServerSocket socket on given port number

        /*
          Note:
          The maximum queue length for incoming connection indications (a request to connect) is set to {@code 50}.
          If a connection indication arrives when the queue is full, the connection is refused.
          In that case we may perform load balancing by creating another instance of the mServerSocket.
          Currently we are not interested to go in that much detail so lets keep it simple
          */
        try {
            //create ServerSocket on localhost and provided port
            mServerSocket = ServerSocket(PORT)

            //With this option set to a non-zero
            //timeout, a call to accept() for this ServerSocket
            //will block for only this amount of time.  If the timeout expires,
            //a java.net.SocketTimeoutException is raised, though the
            //ServerSocket is still valid.
            mServerSocket!!.soTimeout = 60000

            keepListening = true

            mStateCallback.onStatusChange(isConnected = true)

        }catch (ioe: IOException){

            ioe.printStackTrace()
            Log.e(TAG, "Error: " + ioe.message + " \n check stack trace for details")

            mStateCallback.onStatusChange(isConnected = false)
        }

        Log.d(TAG, "Server is running on port ${mServerSocket?.localPort}")

        //Keep listening for incoming requests.
        while (keepListening) {
            listenConnectionRequest()
        }
    }


    /**
     * Listen for incoming connection request and handle in separate thread
     *
     * */
    private fun listenConnectionRequest(){

        try {

            //Will throw SocketException if mServerSocket socket is closed. Hence we've this try catch.
            // Will block upto mServerSocket.soTimeout
            // Can throw // IOException, SecurityException, SocketTimeoutException
            // or IllegalBlockingModeException hence for simplicity just catch Exception

            val client = mServerSocket?.accept()
            client?.apply {

                Log.d(TAG,"Request received from: ${this.inetAddress.hostAddress}")

                // Run client in separate background thread than serverSocket
                Thread(Runnable {
                    RequestHandler(iCacheService,this).run()
                }).start()
            }
        }catch (ioe: Exception){
            ioe.printStackTrace()
        }
    }


}