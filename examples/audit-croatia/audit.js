function abortAudit(message) {
    Output.renderText("Please try again by following the instructions in the script.");
    return AuditResult.aborted(message);
}

function abortOrInvalidateAudit(message) {
    if (Input.readBoolean("Should the audit of this asset be marked as failed?")) {
        return AuditResult.failure(message);
    }

    Output.renderText("Please try again by following the instructions in the script.");
    return AuditResult.aborted(message);
}

function audit(auditData) {
    Output.renderHtml(Ipfs.getFile("step-1.html"));
    Input.button("Continue");
    Output.renderHtml(Ipfs.getFile("step-2.html"));

    if (!Input.readBoolean("Is everything as described above?")) {
        return abortAudit("Audit aborted before validation");
    }

    Output.renderHtml(Ipfs.getFile("step-3.html"));

    if (!Input.readBoolean("Does the search prompt look like described above?")) {
        return abortAudit("Audit aborted before cadastral office was found");
    }

    Output.renderHtml(
        Ipfs.getFile("step-4.html")
            .replace(/{cadastralOffice}/g, auditData.assetInfo.cadastralOffice)
    );

    if (!Input.readBoolean("Did you manage to find the requested value?")) {
        return abortOrInvalidateAudit("Cadastral office not found");
    }

    Output.renderHtml(Ipfs.getFile("step-5.html"));

    if (!Input.readBoolean("Does the search prompt look like described above?")) {
        return abortAudit("Audit aborted before municipality was found");
    }

    Output.renderHtml(
        Ipfs.getFile("step-6.html")
            .replace(/{cadastralMunicipalityName}/g, auditData.assetInfo.cadastralMunicipalityName)
            .replace(/{cadastralMunicipalityNumber}/g, auditData.assetInfo.cadastralMunicipalityNumber)
    );

    if (!Input.readBoolean("Did you manage to find the requested value?")) {
        return abortOrInvalidateAudit("Cadastral municipality not found");
    }

    Output.renderHtml(
        Ipfs.getFile("step-7.html")
            .replace(/{parcelNumber}/g, auditData.assetInfo.parcelNumber)
            .replace(/{parcelSubNumber}/g, auditData.assetInfo.parcelSubNumber)
    );

    if (!Input.readBoolean("Does the first table below the input fields show any values after passing the captcha?")) {
        return abortOrInvalidateAudit("Asset info not found");
    }

    Output.renderHtml(Ipfs.getFile("step-8.html"));

    if (!Input.readBoolean("Do you see the document after clicking on the button?")) {
        return abortOrInvalidateAudit("Ownership document not found");
    }

    let tableRowTemplate = Ipfs.getFile("step-9-table-row-template.html");
    let tableRows = "";

    for (owner of auditData.ownerInfo) {
        tableRows += tableRowTemplate
            .replace(/{ordinalNumber}/g, owner.ordinalNumber)
            .replace(/{totalAssetShares}/g, owner.totalAssetShares)
            .replace(/{ownerName}/g, owner.ownerName)
            .replace(/{pid}/g, owner.pid)
            .replace(/{subdivisionShares}/g, owner.subdivisionShares) + "\n";
    }

    Output.renderHtml(
        Ipfs.getFile("step-9.html")
            .replace(/{tableRows}/g, tableRows)
    );

    if (!Input.readBoolean("Do the document contain the required data?")) {
        return abortOrInvalidateAudit("Ownership not confirmed");
    }

    return AuditResult.success();
}
