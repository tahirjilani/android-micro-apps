package com.tahir.proxyapp;

interface IProxyStatusCallback {

    void onStatus(boolean isConnected, String host, int port);
}
