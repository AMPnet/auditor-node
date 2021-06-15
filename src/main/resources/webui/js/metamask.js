const metamaskNav = document.getElementById("metamask-nav");
const metamaskDiv = document.getElementById("metamask-div");
const metamaskErrorClass = "metamask-error";
const metamaskWarningClass = "metamask-warning";
const metamaskOkClass = "metamask-ok";
const metamaskNotInstalledStatus = "NOT_INSTALLED";
const metamaskNotConnectedStatus = "NOT_CONNECTED";
const metamaskReadyStatus = "READY";

const metamaskContext = {
    status: null
}

function toggleMetamaskPanel() {
    metamaskDiv.hidden = !metamaskDiv.hidden;
}

function checkMetamask() {
    const messageElement = document.createElement("p");

    if (!window.ethereum) {
        metamaskContext.status = metamaskNotInstalledStatus;
        metamaskNav.innerText = "!";

        metamaskDiv.innerHTML = "";
        messageElement.innerText = "MetaMask not installed";
        metamaskDiv.appendChild(messageElement);
    } else if (ethereum.selectedAddress === null) {
        metamaskContext.status = metamaskNotConnectedStatus;
        metamaskNav.classList.remove(metamaskErrorClass);
        metamaskNav.classList.add(metamaskWarningClass);
        metamaskNav.innerText = "●";

        metamaskDiv.innerHTML = "";
        messageElement.innerText = "MetaMask not connected";
        metamaskDiv.appendChild(messageElement);

        const connectButton = document.createElement("button");
        connectButton.setAttribute("class", "large-button success-button");
        connectButton.innerText = "Connect";
        connectButton.onclick = async function () {
            connectButton.disabled = true;

            try {
                await ethereum.request({method: 'eth_requestAccounts'});
            } finally {
                checkMetamask();
            }
        }
        metamaskDiv.appendChild(connectButton);
    } else {
        metamaskContext.status = metamaskReadyStatus;
        metamaskNav.classList.remove(metamaskErrorClass);
        metamaskNav.classList.add(metamaskOkClass);
        metamaskNav.innerText = "✓";

        metamaskDiv.innerHTML = "";
        messageElement.innerText = "MetaMask connected";
        metamaskDiv.appendChild(messageElement);
    }
}

setTimeout(checkMetamask, 500);
