package com.tahir.proxyapp.server

import android.util.Log
import com.tahir.cacheapp.ICacheResponseCallback
import com.tahir.cacheapp.ICacheService
import java.io.*
import java.net.HttpURLConnection
import java.net.Socket
import java.net.URL
import java.nio.charset.Charset
import java.util.*


class RequestHandler(private val iCacheService: ICacheService?, private val client: Socket) {

    private val TAG = "ConnectionRequest"
    private val requestScanner: Scanner = Scanner(client.getInputStream())
    private val outputStream: OutputStream = client.getOutputStream()

    fun run() {

        //get url from requesting client
        var url = requestScanner.nextLine()

        iCacheService?.getData(url, object : ICacheResponseCallback.Stub(){
            override fun onResponse(cachedData: String?) {

                Log.d(TAG, "Data found from cache: $cachedData")

                if(cachedData != null){
                    //data was found from cache
                    this@RequestHandler.serveClientWith(cachedData)
                }else{
                    //data not found in cache, try reading from internet.
                    Log.d(TAG, "Going to fetch data from internet")
                    loadDataFromInternet(url)
                }
            }

        })
    }


    /**
     * Look for data on Internet
     * */
    private fun loadDataFromInternet(urlString:String){

        var remoteResponse:String? = null

        val url = URL(urlString)
        try {
            remoteResponse = downloadUrl(url)
        }catch (ioe: IOException){
            ioe.printStackTrace()
        }

        //if remoteResponse is not empty or null then save in cache
        if(!remoteResponse.isNullOrEmpty()){
            iCacheService?.saveData(urlString, remoteResponse)
        }

        this.serveClientWith(remoteResponse)
    }


    /**
     * Reply to client with data.
     * */
    private fun serveClientWith(message: String?) {

        if (message.isNullOrEmpty()){
            //send an empty line.
            outputStream.write(("\n").toByteArray(Charset.defaultCharset()))
        }else {
            //send
            outputStream.write((message + "\n").toByteArray(Charset.defaultCharset()))
        }
        outputStream.flush()

        client.close()
    }


    /**
     * Given a URL, sets up a connection and gets the HTTP response body from the server.
     * If the network request is successful, it returns the response body in String form. Otherwise,
     * it will throw an IOException.
     * (Copied android developers docs)
     */
    @Throws(IOException::class)
    private fun downloadUrl(url: URL): String? {
        var connection: HttpURLConnection? = null
        return try {
            connection = (url.openConnection() as? HttpURLConnection)
            connection?.run {
                // Timeout for reading InputStream arbitrarily set to 3000ms.
                readTimeout = 3000
                // Timeout for connection.connect() arbitrarily set to 3000ms.
                connectTimeout = 3000
                // For this use case, set HTTP method to GET.
                requestMethod = "GET"
                // Already true by default but setting just in case; needs to be true since this request
                // is carrying an input (response) body.
                doInput = true
                // Open communications link (network traffic occurs here).
                connect()
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    throw IOException("HTTP error code: $responseCode")
                }
                // Retrieve the response body as an InputStream.
                inputStream?.let { stream ->
                    // Converts Stream to String
                    readStream(stream)
                }
            }
        } finally {
            // Close Stream and disconnect HTTPS connection.
            connection?.inputStream?.close()
            connection?.disconnect()
        }
    }


    /**
     * Converts the contents of an InputStream to a String.
     */
    @Throws(IOException::class, UnsupportedEncodingException::class)
    fun readStream(stream: InputStream): String? {

        val sb = StringBuilder()
        var line: String?
        val br = BufferedReader(InputStreamReader(stream, "UTF-8"))
        line = br.readLine()
        while (line != null) {
            sb.append(line)
            line = br.readLine()
        }
        Log.d(TAG, sb.toString())
        return sb.toString()
    }
}