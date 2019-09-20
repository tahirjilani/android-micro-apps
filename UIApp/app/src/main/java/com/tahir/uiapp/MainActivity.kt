package com.tahir.uiapp

import android.app.Service
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.tahir.uiapp.net.ProxyManager
import kotlinx.android.synthetic.main.activity_main.*
import java.io.ByteArrayInputStream
import java.util.*
import com.tahir.proxyapp.IProxyServerService
import com.tahir.proxyapp.IProxyStatusCallback


class MainActivity : AppCompatActivity() {


    private val TAG = "MainActivity"
    private var iProxyServerService:IProxyServerService? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bindProxyService()
    }


    override fun onDestroy() {

        unbindProxyService()
        super.onDestroy()
    }

    /*
    * create connection to the remote ProxyService within com.tahir.proxyapp application.
    * successful connection will trigger iProxyServerService.onServiceConnected(..)
    * */
    private fun bindProxyService(){

        var it = Intent()
        it.component = ComponentName("com.tahir.proxyapp", "com.tahir.proxyapp.ProxyServerService")
        bindService(it, mProxyConnection, Service.BIND_AUTO_CREATE)

    }


    /*
   * close remote service connection. It will not trigger mProxyConnection.onServiceDisconnected(..)
   * */
    private fun unbindProxyService() {
        unbindService(mProxyConnection)
    }


    /**
     * ServiceConnection that will hold connection to the remote ProxyServerService
     * within com.tahir.proxyapp application
     * */
    private val mProxyConnection = object : ServiceConnection {

        /*
         Called when the connection with the service is established
         */
        override fun onServiceConnected(className: ComponentName, service: IBinder) {

            //This gets an instance of the IProxyServerService, which we can use to call on the service
            iProxyServerService = IProxyServerService.Stub.asInterface(service)
            Log.d(TAG, "ProxyService Service connected")

            checkProxyServerStatus()
        }


        /*
         Called when the connection with the service disconnects unexpectedly
         */
        override fun onServiceDisconnected(className: ComponentName) {

            Log.e(TAG, "ProxyService Service has disconnected unexpectedly.")
            iProxyServerService = null

        }
    }


    /*
    * Get proxy server status
    * */
    private fun checkProxyServerStatus(){

        iProxyServerService?.getStatus(object : IProxyStatusCallback.Stub(){
            override fun onStatus(isConnected: Boolean, host:String, port:Int) {
                if(isConnected){
                    initWebViewWithProxyServer(host, port)
                }else{
                    Log.e(TAG, "Connection to Proxy Server was failed. Check remote service logs for details.")
                }
            }

        })
    }



    /**
     * Initialize WebView and intercept all url requests and route them towards ProxyServer application
     * */
    private fun initWebViewWithProxyServer(host:String, port:Int){

        val webSettings = webView.settings
        webSettings.javaScriptEnabled = true

        webView.webViewClient = object: WebViewClient() {

            override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
                //This is background thread.
                return getProxyServerResponse(host, port, request.url.toString())
            }

            override fun shouldInterceptRequest(view: WebView, url: String): WebResourceResponse? {
                return getProxyServerResponse(host, port, url)
            }

            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                return true
            }

            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                return true
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                proressBar.visibility = View.GONE
            }
        }

        webView.loadUrl("file:///android_asset/index.html")
    }


    /**
     * Connect with proxy server using host & port and ask for response against url
     * */
    fun getProxyServerResponse(host:String, port:Int, url: String): WebResourceResponse?{

        //get response from proxy server
        var response = ProxyManager.request(host, port, url)

        //Create WebResourceResponse from response.
        val inputStream = ByteArrayInputStream(response.toByteArray())
        val responseHeaders = HashMap<String, String>()
        responseHeaders["Access-Control-Allow-Origin"] = "*"

        return WebResourceResponse(
            "text/plane",
            "UTF-8",
            200,
            "OK",
            responseHeaders,
            inputStream
        )

    }
}
