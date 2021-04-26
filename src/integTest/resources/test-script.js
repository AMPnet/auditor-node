try {
    var response = http.get("https://bb.dom-l.at/");
    console.log("Response body:");
    console.log(response);
    console.log("JSON:");
    console.log(JSON.parse("{\"test\":1}").test);
} catch (e) {
    console.log("Error: " + e);
}
