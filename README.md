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


# Want to try?
Please clone and install all three apps numberwise. Headless apps will not show any launcher icon. You can see them running in Applications section in phone settings.
Run UI App to see the records.

The api: http://3z0yy.mocklab.io/menu/sample has been mocked as a free trial for 14 days on mocklab. That returns the following data:

[
  {
    "id": "58ab140932dfbcc4253b5236",
    "name": "consectetur",
    "price": 1200,
    "type": "main course"
  },
  {
    "id": "58ab140904117a99a73565e4",
    "name": "adipisicing",
    "price": 1400,
    "type": "drink"
  }
]

In case API is down you can hard code this response in Proxy Server App.
