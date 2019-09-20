package com.tahir.proxyapp

import android.util.Log
import java.io.OutputStream
import java.net.Socket
import java.nio.charset.Charset
import java.util.*
import kotlin.concurrent.thread

class Client(private val address: String, private val port: Int) {

    private val TAG = "Client"

    interface ResponseCallback{
        fun onResponse(response:String?)
    }


    init {
        Log.d(TAG, "Connected to server at $address on port $port")
    }


    fun enqueue(url: String, callback: ResponseCallback) {

        thread { request(url, callback) }
    }


    private fun request(url: String, callback: ResponseCallback) {


        val connection = Socket(address, port)
        val reader = Scanner(connection.getInputStream())
        val writer: OutputStream = connection.getOutputStream()

        //send request
        writer.write((url + '\n').toByteArray(Charset.defaultCharset()))
        writer.flush()

        val urlResponse = reader.nextLine()

        //get welcome response
        Log.d(TAG, urlResponse)

        reader.close()
        connection.close()

        callback.onResponse(urlResponse)

    }

}