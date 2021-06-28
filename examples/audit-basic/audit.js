function audit(auditData) {
    Output.renderHtml("<div>Audit data:<br><pre>" + JSON.stringify(auditData, null, 4) + "</pre></div>");

    const successfulAudit = Input.readBoolean("Asset should be marked as valid according to the provided audit data: ");

    if (successfulAudit) {
        return AuditResult.success();
    } else {
        const failureReason = Input.readString("Please provide a reason for failed audit: ");
        return AuditResult.failure(failureReason);
    }
}
