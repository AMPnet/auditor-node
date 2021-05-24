function audit() {
    let displayHtmlFragment = Input.readBoolean("Display HTML fragment?");

    if (displayHtmlFragment) {
        Output.renderHtml(Ipfs.getFile("fragment.html"));
    } else {
        Output.renderText("HTML fragment not rendered");
    }

    return AuditResult.of(true);
}
