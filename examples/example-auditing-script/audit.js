function audit(auditData) {
    Output.renderText("Script input: " + JSON.stringify(auditData));

    const displayHtmlFragment = Input.readBoolean("Display HTML fragment?");

    if (displayHtmlFragment) {
        const htmlFragment = Ipfs.getFile("fragment.html");

        if (htmlFragment === null) {
            Output.renderText("Cannot fetch file named 'fragment.html' from specified IPFS directory.");
            return AuditResult.failure("File not found: 'fragment.html'");
        }

        Output.renderHtml(htmlFragment);
    } else {
        Output.renderText("HTML fragment not rendered");
    }

    return AuditResult.success();
}
