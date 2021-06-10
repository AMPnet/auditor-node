const availableAssetsForAudit = document.getElementById("assets-available-for-audit");
const auditingStartStopButton = document.getElementById("auditing-start-stop-button");
const auditingOutputDiv = document.getElementById("auditing-output-div");
const selectedAsset = "selected-asset";
const startAuditingButtonClass = "success-button";
const abortAuditingButtonClass = "error-button";
const auditingContext = {
    auditRunning: false,
    webSocket: null
};

function selectAsset(index) {
    let assetSelected = false;

    for (let i = 0; i < availableAssetsForAudit.children.length; i++) {
        const childElement = availableAssetsForAudit.children[i];

        if (i === index && !childElement.classList.contains(selectedAsset)) {
            childElement.classList.add(selectedAsset);
            assetSelected = true;
        } else {
            childElement.classList.remove(selectedAsset);
        }
    }

    auditingStartStopButton.disabled = !assetSelected;
}

function addAsset(assetValue, assetName) {
    const index = availableAssetsForAudit.children.length;
    const assetItem = document.createElement("li");

    assetItem.innerText = assetName;
    assetItem.setAttribute("value", assetValue);
    assetItem.onclick = function () {
        if (!assetItem.disabled) {
            selectAsset(index);
        }
    }

    availableAssetsForAudit.appendChild(assetItem);
}

function startOrAbortAuditing() {
    if (!auditingContext.auditRunning) {
        auditingStartStopButton.disabled = true;

        for (const child of availableAssetsForAudit.children) {
            child.disabled = true;
            child.setAttribute("disabled", "disabled");
        }

        auditingOutputDiv.innerHTML = "";

        // TODO use relative path, use selected asset
        const webSocketUrl = "ws://localhost:8080/audit";

        auditingContext.webSocket = connectToWebSocket(webSocketUrl, auditingOutputDiv, "{}");
        auditingContext.webSocket.addEventListener("open", function () {
            auditingContext.auditRunning = true;
            auditingStartStopButton.classList.remove(startAuditingButtonClass);
            auditingStartStopButton.classList.add(abortAuditingButtonClass);
            auditingStartStopButton.innerText = "Abort Audit";
            auditingStartStopButton.disabled = false;
        });
        auditingContext.webSocket.addEventListener("close", function () {
            auditingContext.auditRunning = false;
            auditingStartStopButton.classList.remove(abortAuditingButtonClass);
            auditingStartStopButton.classList.add(startAuditingButtonClass);
            auditingStartStopButton.innerText = "Start Audit";
            auditingStartStopButton.disabled = false;

            for (const child of availableAssetsForAudit.children) {
                child.disabled = false;
                child.removeAttribute("disabled");
            }
        });
    } else {
        auditingContext.webSocket.close();
    }
}

// TODO remove this later and fetch assets from backend
addAsset("asset1", "Asset1");
addAsset("asset2", "Asset2");
addAsset("asset3", "Asset3");
addAsset("asset4", "Asset4");
addAsset("asset5", "Asset5");
addAsset("asset6", "Asset6");
addAsset("asset7", "Asset7");
addAsset("asset8", "Asset8");
addAsset("asset9", "Asset9");
addAsset("asset10", "Asset10");
addAsset("asset11", "Asset11");
addAsset("asset12", "Asset12");
addAsset("asset13", "Asset13");
addAsset("asset14", "Asset14");
addAsset("asset15", "Asset15");
addAsset("asset16", "Asset16");
addAsset("asset17", "Asset17");
addAsset("asset18", "Asset18");
addAsset("asset19", "Asset19");
addAsset("asset20", "Asset20");
addAsset("asset21", "Asset21");
