package com.ampnet.auditornode

// language=js
val JS_CODE = """
        try {
            console.log("Hello from JS!");
            var http = Java.type('com.ampnet.auditornode.scriptapi.Http');
            console.log(Object.getOwnPropertyNames(http));
            
            var response = http.request("GET", "https://bb.dom-l.at/");
            console.log("Response in JS:");
            console.log(response);
        } catch (e) {
            console.log("Error: " + e);
        }
    """.trimIndent()
