# android-micro-apps

Android Micro Apps IPC using AIDL
This exercise consists of three micro-apps


# 1) Cache App 
This is headless app and has CacheService that can be bound by Proxy Server App.
Stores data(url & response) in local RoomDatabase


# 2) Proxy Server App 
This is again headless app containing ProxyService that can be bound from UI App via AIDL. It runs a server socket on localhost and port number. 
A http request received by server socket and it serves data from Cache App, or fetch from original destination if unavailable in Cache App.

ProxyService binds Cache App’s CacheService via AIDL. Proxy will request Cache for data against specific URL. If data unavailable, it will serve data to UI App from Internet.


# 3) UI App: 
WebView based micro app which loads html page index.html file from Assets folder. Binds ProxyService app via AIDL. Http call will be intercepted by WebViewClient and routed to Proxy Server App and render response in WebView.
