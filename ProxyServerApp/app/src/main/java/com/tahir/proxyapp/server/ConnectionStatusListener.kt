package com.tahir.proxyapp.server

interface ConnectionStatusListener {

    fun onStatusChange(isConnected:Boolean)
}