const scriptSourceDiv = document.getElementById("script-source-div");
const scriptOutputDiv = document.getElementById("script-output-div");
const storeScriptButton = document.getElementById("store-script-button");
const scriptSource = document.getElementById("script-source");
const scriptInputJson = document.getElementById("script-input-json");
const scriptIpfsDirectory = document.getElementById("script-ipfs-directory");
const scriptStartStopButton = document.getElementById("script-start-stop-button");
const hiddenScriptContentClass = "hidden-script-content";
const startScriptButtonClass = "success-button";
const stopScriptButtonClass = "error-button";
const scriptDevelopmentContext = {
    scriptRunning: false,
    scriptId: null,
    webSocket: null
};

function toggleScriptSourceAndScriptOutput() {
    scriptSourceDiv.classList.toggle(hiddenScriptContentClass);
    scriptOutputDiv.classList.toggle(hiddenScriptContentClass);
}

function checkInputJson(event) {
    const nonNullValue = event.target.value ?? "";
    scriptStartStopButton.disabled = nonNullValue.length === 0 || scriptDevelopmentContext.scriptId === null;
}

scriptInputJson.oninput = checkInputJson;

scriptSource.oninput = function (event) {
    const nonNullValue = event.target.value ?? "";
    storeScriptButton.disabled = nonNullValue.length === 0;
}

function storeAuditingScript() {
    storeScriptButton.disabled = true;

    const request = new XMLHttpRequest();

    request.open("POST", "/script/store", true);
    request.setRequestHeader("Content-Type", "text/plain");
    request.onreadystatechange = function () {
        if (this.readyState === XMLHttpRequest.DONE) {
            if (this.status === 200) {
                const responseJson = JSON.parse(this.responseText);
                scriptDevelopmentContext.scriptId = responseJson.id;
                checkInputJson({target: scriptInputJson});
            } else {
                storeScriptButton.disabled = false;
                scriptDevelopmentContext.scriptId = null;
            }
        }
    }
    request.send(scriptSource.value);
}

function startOrStopScript() {
    if (!scriptDevelopmentContext.scriptRunning) {
        scriptSource.disabled = true;
        storeScriptButton.disabled = true;
        scriptInputJson.disabled = true;
        scriptIpfsDirectory.disabled = true;
        scriptStartStopButton.disabled = true;

        if (scriptOutputDiv.classList.contains(hiddenScriptContentClass)) {
            scriptSourceDiv.classList.add(hiddenScriptContentClass);
            scriptOutputDiv.classList.remove(hiddenScriptContentClass);
        }

        scriptOutputDiv.innerHTML = "";

        const ipfsDirectoryHash = (scriptIpfsDirectory.value ?? "").trim();
        let webSocketUrl = "ws://" + window.location.host + "/script/interactive/" + scriptDevelopmentContext.scriptId;

        if (ipfsDirectoryHash.length !== 0) {
            webSocketUrl += "?ipfs-directory=" + ipfsDirectoryHash.trim();
        }

        const inputJson = (scriptInputJson.value ?? "").trim()

        scriptDevelopmentContext.webSocket = connectToWebSocket(webSocketUrl, scriptOutputDiv, inputJson);
        scriptDevelopmentContext.webSocket.addEventListener("open", function () {
            scriptDevelopmentContext.scriptRunning = true;
            scriptStartStopButton.classList.remove(startScriptButtonClass);
            scriptStartStopButton.classList.add(stopScriptButtonClass);
            scriptStartStopButton.innerText = "Stop Script";
            scriptStartStopButton.disabled = false;
        });
        scriptDevelopmentContext.webSocket.addEventListener("close", function () {
            scriptDevelopmentContext.scriptRunning = false;
            scriptStartStopButton.classList.remove(stopScriptButtonClass);
            scriptStartStopButton.classList.add(startScriptButtonClass);
            scriptStartStopButton.innerText = "Start Script";
            scriptSource.disabled = false;
            storeScriptButton.disabled = false;
            scriptInputJson.disabled = false;
            scriptIpfsDirectory.disabled = false;
            scriptStartStopButton.disabled = false;
        });
    } else {
        scriptDevelopmentContext.webSocket.close();
    }
}
