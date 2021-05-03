function audit() {
    try {
        let response = http.get("https://bb.dom-l.at/");
        console.log("Response body:");
        console.log(response);
        return AuditResult.of(true);
    } catch (e) {
        console.log("Error: " + e);
        return AuditResult.of(false);
    }
}
