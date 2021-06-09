const availableAssetsForAudit = document.getElementById("assets-available-for-audit");
const auditingStartStopButton = document.getElementById("auditing-start-stop-button");
const selectedAsset = "selected-asset";
const startAuditing = "start-auditing";
const abortAuditing = "abort-auditing";
let auditRunning = false;

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
    if (!auditRunning) {
        auditRunning = true;
        auditingStartStopButton.classList.remove(startAuditing);
        auditingStartStopButton.classList.add(abortAuditing);
        auditingStartStopButton.innerText = "Abort Audit";

        for (const child of availableAssetsForAudit.children) {
            child.disabled = true;
            child.setAttribute("disabled", "disabled");
        }

        // TODO start audit
    } else {
        auditRunning = false;
        auditingStartStopButton.classList.remove(abortAuditing);
        auditingStartStopButton.classList.add(startAuditing);
        auditingStartStopButton.innerText = "Start Audit";

        for (const child of availableAssetsForAudit.children) {
            child.disabled = false;
            child.removeAttribute("disabled");
        }
        // TODO abort audit
    }
}

// TODO connect to web socket & handle messages

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
