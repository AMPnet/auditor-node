const availableAssetsForAudit = document.getElementById("assets-available-for-audit");
const auditingStartStopButton = document.getElementById("auditing-start-stop-button");
const auditingOutputDiv = document.getElementById("auditing-output-div");
const selectedAsset = "selected-asset";
const startAuditingButtonClass = "success-button";
const abortAuditingButtonClass = "error-button";
const auditingContext = {
    auditRunning: false,
    assetContractAddress: null,
    webSocket: null
};

function selectAsset(index) {
    let assetSelected = false;

    for (let i = 0; i < availableAssetsForAudit.children.length; i++) {
        const childElement = availableAssetsForAudit.children[i];

        if (i === index && !childElement.classList.contains(selectedAsset)) {
            childElement.classList.add(selectedAsset);
            assetSelected = true;
            auditingContext.assetContractAddress = childElement.getAttribute("value");
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

        // TODO use relative path
        const webSocketUrl = "ws://localhost:8080/audit/" + auditingContext.assetContractAddress;

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

function fetchAssets() {
    const request = new XMLHttpRequest();

    request.open("GET", "http://localhost:8080/assets/list", true); // TODO use relative path
    request.onreadystatechange = function () {
        if (this.readyState === XMLHttpRequest.DONE) {
            if (this.status === 200) {
                const responseJson = JSON.parse(this.responseText);

                for (asset of responseJson.assets) {
                    addAsset(asset.contractAddress, asset.name);
                }
            }
        }
    }
    request.send();
}

fetchAssets();
