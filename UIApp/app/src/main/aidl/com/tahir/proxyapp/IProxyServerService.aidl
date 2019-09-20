package com.tahir.proxyapp;

import com.tahir.proxyapp.IProxyStatusCallback;

interface IProxyServerService{

    void getStatus(in IProxyStatusCallback callback);
}
