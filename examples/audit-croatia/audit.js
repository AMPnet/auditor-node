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
    Output.renderHtml(
        Ipfs.getFile("step-2.html")
            .replace(/{image1}/g, Ipfs.linkToFile("step-2.png"))
    );

    if (!Input.readBoolean("Is everything as described above?")) {
        return abortAudit("Audit aborted before validation");
    }

    Output.renderHtml(
        Ipfs.getFile("step-3.html")
            .replace(/{image1}/g, Ipfs.linkToFile("step-3-1.png"))
            .replace(/{image2}/g, Ipfs.linkToFile("step-3-2.png"))
    );

    if (!Input.readBoolean("Does the search prompt look like described above?")) {
        return abortAudit("Audit aborted before cadastral office was found");
    }

    Output.renderHtml(
        Ipfs.getFile("step-4.html")
            .replace(/{cadastralOffice}/g, auditData.assetInfo.cadastralOffice)
            .replace(/{image1}/g, Ipfs.linkToFile("step-4-1.png"))
            .replace(/{image2}/g, Ipfs.linkToFile("step-4-2.png"))
            .replace(/{image3}/g, Ipfs.linkToFile("step-4-3.png"))
    );

    if (!Input.readBoolean("Did you manage to find the requested value?")) {
        return abortOrInvalidateAudit("Cadastral office not found");
    }

    Output.renderHtml(
        Ipfs.getFile("step-5.html")
            .replace(/{image1}/g, Ipfs.linkToFile("step-5-1.png"))
            .replace(/{image2}/g, Ipfs.linkToFile("step-5-2.png"))
    );

    if (!Input.readBoolean("Does the search prompt look like described above?")) {
        return abortAudit("Audit aborted before municipality was found");
    }

    Output.renderHtml(
        Ipfs.getFile("step-6.html")
            .replace(/{cadastralMunicipalityName}/g, auditData.assetInfo.cadastralMunicipalityName)
            .replace(/{cadastralMunicipalityNumber}/g, auditData.assetInfo.cadastralMunicipalityNumber)
            .replace(/{image1}/g, Ipfs.linkToFile("step-6-1.png"))
            .replace(/{image2}/g, Ipfs.linkToFile("step-6-2.png"))
            .replace(/{image3}/g, Ipfs.linkToFile("step-6-3.png"))
    );

    if (!Input.readBoolean("Did you manage to find the requested value?")) {
        return abortOrInvalidateAudit("Cadastral municipality not found");
    }

    Output.renderHtml(
        Ipfs.getFile("step-7.html")
            .replace(/{parcelNumber}/g, auditData.assetInfo.parcelNumber)
            .replace(/{parcelSubNumber}/g, auditData.assetInfo.parcelSubNumber)
            .replace(/{image1}/g, Ipfs.linkToFile("step-7-1.png"))
            .replace(/{image2}/g, Ipfs.linkToFile("step-7-2.png"))
            .replace(/{image3}/g, Ipfs.linkToFile("step-7-3.png"))
    );

    if (!Input.readBoolean("Does the first table below the input fields show any values after passing the captcha?")) {
        return abortOrInvalidateAudit("Asset info not found");
    }

    Output.renderHtml(
        Ipfs.getFile("step-8.html")
            .replace(/{image1}/g, Ipfs.linkToFile("step-8.png"))
    );

    if (!Input.readBoolean("Do you see the document after clicking on the button?")) {
        return abortOrInvalidateAudit("Ownership document not found");
    }

    const tableRowTemplate = Ipfs.getFile("step-9-table-row-template.html");
    let tableRows = "";

    for (const owner of auditData.ownerInfo) {
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

    if (!Input.readBoolean("Does the document contain the required data?")) {
        return abortOrInvalidateAudit("Ownership not confirmed");
    }

    return AuditResult.success();
}
