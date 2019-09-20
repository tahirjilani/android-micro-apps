package com.tahir.uiapp.net

import android.util.Log
import java.io.IOException
import java.io.OutputStream
import java.net.Socket
import java.nio.charset.Charset
import java.util.*

class ProxyManager{

    companion object {

        private val TAG = "ProxyManager"


        /**
         * Connect to Proxy server and request data against url and keep waiting until proxy server
         * responds. Will throw IOException if remote is not accessible. Must be called from
         * background thread to create socket connection otherwise system will raise exception for
         * accessing network on main thread.
         * */
        fun request(hostAddress:String, port:Int, url: String): String {

            //in case of some error we simply return empty string.
            var urlResponse = ""
            try {

                val connection = Socket(hostAddress, port)

                //a read() call on the InputStream associated with this Socket will block for only this amount of time.
                connection.soTimeout = 60000

                val inputScanner = Scanner(connection.getInputStream())
                val outputStream: OutputStream = connection.getOutputStream()

                //send request
                outputStream.write((url + '\n').toByteArray(Charset.defaultCharset()))

                Log.d(TAG, "Sent: $url")

                urlResponse = inputScanner.nextLine()

                Log.d(TAG, "Received: $urlResponse")

                inputScanner.close()
                connection.close()

            } catch (ioe: IOException) {

                ioe.printStackTrace()
            }
            return urlResponse
        }
    }
}