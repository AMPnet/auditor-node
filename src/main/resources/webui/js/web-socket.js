const webSocketContext = {
    inputIndex: 0
}

function processInfoMessage(message, outputDiv) {
    const textElement = document.createElement("p");

    switch (message.message) {
        case "connected":
            textElement.innerText = "Connection established";
            break;
        case "notFound":
            textElement.innerText = "Script with not found";
            break;
        case "executing":
            textElement.innerText = "Script execution has started";
            break;
        case "invalidInputJson":
            textElement.innerText = "Invalid input JSON was provided";
            break;
        default:
            textElement.innerText = "Unknown web socket INFO message: " + JSON.stringify(message);
            break;
    }

    outputDiv.appendChild(textElement);
}

function submitInputField(buttonElement, inputElement, valueField, webSocket) {
    buttonElement.disabled = true;
    inputElement.disabled = true;
    webSocket.send(inputElement[valueField]);
}

function createInputField(message, inputType, valueField, outputDiv, webSocket) {
    const inputFieldDivElement = document.createElement("div");

    const inputId = "input-" + webSocketContext.inputIndex;
    const labelElement = document.createElement("label");
    labelElement.setAttribute("for", inputId);
    labelElement.innerText = message;
    inputFieldDivElement.appendChild(labelElement);

    const inputElement = document.createElement("input");
    inputElement.setAttribute("id", inputId);
    inputElement.setAttribute("type", inputType);
    inputFieldDivElement.appendChild(inputElement);

    const buttonElement = document.createElement("button");
    buttonElement.onclick = function () {
        submitInputField(buttonElement, inputElement, valueField, webSocket);
    }
    buttonElement.innerText = "Submit";
    inputFieldDivElement.appendChild(buttonElement);

    outputDiv.appendChild(inputFieldDivElement);

    webSocketContext.inputIndex += 1;
}

function submitForm(formId, numFields, webSocket) {
    for (let i = 0; i < numFields; i++) {
        document.getElementById(formId + "-" + i).disabled = true;
    }

    document.getElementById(formId).disabled = true;

    for (let i = 0; i < numFields; i++) {
        const inputElement = document.getElementById(formId + "-" + i);

        if (inputElement.type === "checkbox") {
            webSocket.send(inputElement.checked);
        } else {
            webSocket.send(inputElement.value);
        }
    }
}

function createInputForm(message, fields, outputDiv, webSocket) {
    const fieldsDivElement = document.createElement("div");

    const formLabelElement = document.createElement("h3");
    formLabelElement.innerText = message;
    fieldsDivElement.appendChild(formLabelElement);

    const formId = "form-" + webSocketContext.inputIndex;

    for (let i = 0; i < fields.length; i++) {
        const field = fields[i];
        let inputType;

        switch (field.type) {
            case "BOOLEAN":
                inputType = "checkbox";
                break;
            case "NUMBER":
                inputType = "number";
                break;
            case "STRING":
                inputType = "text";
                break;
            default:
                const warningElement = document.createElement("p");
                warningElement.innerText = "Unknown field type: " + field.type + "; defaulting to text";
                fieldsDivElement.appendChild(warningElement);
                inputType = "text";
                break;
        }

        const fieldDivElement = document.createElement("div");

        const fieldId = formId + "-" + i;
        const fieldLabelElement = document.createElement("label");

        fieldLabelElement.setAttribute("for", fieldId);
        fieldLabelElement.innerText = field.description;
        fieldDivElement.appendChild(fieldLabelElement);

        const fieldInputElement = document.createElement("input");
        fieldInputElement.setAttribute("id", fieldId);
        fieldInputElement.setAttribute("type", inputType);
        fieldDivElement.appendChild(fieldInputElement);

        fieldsDivElement.appendChild(fieldDivElement);
    }

    const buttonElement = document.createElement("button");
    buttonElement.setAttribute("id", formId);
    buttonElement.onclick = function () {
        submitForm(formId, fields.length, webSocket);
    }
    buttonElement.innerText = "Submit";
    fieldsDivElement.appendChild(buttonElement);

    outputDiv.appendChild(fieldsDivElement);

    webSocketContext.inputIndex += 1;
}

function createButton(message, outputDiv, webSocket) {
    const buttonDivElement = document.createElement("div");

    const buttonElement = document.createElement("button");
    buttonElement.onclick = function () {
        buttonElement.disabled = true;
        webSocket.send("click");
    }
    buttonElement.innerText = message;
    buttonDivElement.appendChild(buttonElement);

    outputDiv.appendChild(buttonDivElement);
}

function render(value, attribute, elementType, outputDiv) {
    const element = document.createElement(elementType);
    element[attribute] = value;
    outputDiv.appendChild(element);
    outputDiv.appendChild(document.createElement("br"));
}

function processCommandMessage(message, outputDiv, webSocket, inputJson) {
    switch (message.command) {
        case "readInputJson":
            webSocket.send(inputJson);
            break;
        case "readBoolean":
            createInputField(message.message, "checkbox", "checked", outputDiv, webSocket);
            break;
        case "readNumber":
            createInputField(message.message, "number", "value", outputDiv, webSocket);
            break;
        case "readString":
            createInputField(message.message, "text", "value", outputDiv, webSocket);
            break;
        case "readFields":
            createInputForm(message.message, message.fields, outputDiv, webSocket);
            break;
        case "button":
            createButton(message.message, outputDiv, webSocket);
            break;
        case "renderText":
            render(message.text, "innerText", "div", outputDiv);
            break;
        case "renderHtml":
            render(message.html, "innerHTML", "div", outputDiv);
            break;
        case "renderMarkdown":
            render(message.markdown, "innerText", "pre", outputDiv);
            break;
        default:
            const textElement = document.createElement("p");
            textElement.innerText = "Unknown web socket COMMAND message: " + JSON.stringify(message);
            outputDiv.appendChild(textElement);
    }
}

function processResponseMessage(message, outputDiv) {
    const textElement = document.createElement("p");

    if (message.success) {
        textElement.innerText = "Result:";
        outputDiv.appendChild(textElement);

        const codeElement = document.createElement("code");
        codeElement.innerText = JSON.stringify(message.payload);
        outputDiv.appendChild(codeElement);

        if ((message.payload.status === "SUCCESS" || message.payload.status === "FAILURE") && message.transaction &&
            metamaskContext.status === metamaskReadyStatus) {

            const transactionElement = document.createElement("p");
            transactionElement.innerText = "Transaction:";
            outputDiv.appendChild(transactionElement);

            const transactionJson = document.createElement("code");
            transactionJson.innerText = JSON.stringify(message.transaction);
            outputDiv.appendChild(transactionJson);

            const sendTransactionMessage = document.createElement("p");

            if (message.payload.status === "SUCCESS") {
                sendTransactionMessage.innerText = "Send successful audit transaction?";
            } else {
                sendTransactionMessage.innerText = "Send failed audit transaction?";
            }

            outputDiv.appendChild(sendTransactionMessage);

            const sendTransactionButton = document.createElement("button");
            const ignoreTransactionButton = document.createElement("button");

            sendTransactionButton.onclick = async function () {
                sendTransactionButton.disabled = true;
                ignoreTransactionButton.disabled = true;

                const transactionParameters = {
                    to: message.transaction.to,
                    from: ethereum.selectedAddress,
                    value: '0x00',
                    data: message.transaction.data
                };

                const txHash = await ethereum.request({
                    method: 'eth_sendTransaction',
                    params: [transactionParameters]
                });

                const txHashElement = document.createElement("p");
                txHashElement.innerText = "Transaction hash: " + txHash;
                outputDiv.appendChild(txHashElement);
            }
            sendTransactionButton.innerText = "Yes";
            outputDiv.appendChild(sendTransactionButton);

            ignoreTransactionButton.onclick = function () {
                sendTransactionButton.disabled = true;
                ignoreTransactionButton.disabled = true;
            }
            ignoreTransactionButton.innerText = "No";
            outputDiv.appendChild(ignoreTransactionButton);
        }
    } else {
        textElement.innerText += "Error: " + JSON.stringify(message.payload);
        outputDiv.appendChild(textElement);
    }
}

function processWebSocketMessage(rawMessage, outputDiv, webSocket, inputJson) {
    const message = JSON.parse(rawMessage);

    // noinspection JSUnreachableSwitchBranches - IntelliJ does not recognize `messageType` as string here
    switch (message.messageType) {
        case "INFO":
            processInfoMessage(message, outputDiv);
            break;
        case "COMMAND":
            processCommandMessage(message, outputDiv, webSocket, inputJson);
            break;
        case "RESPONSE":
            processResponseMessage(message, outputDiv);
            break;
        default:
            const textElement = document.createElement("p");
            textElement.innerText = "Unknown web socket message: " + rawMessage;
            outputDiv.appendChild(textElement);
            break;
    }
}

function connectToWebSocket(webSocketUrl, outputDiv, inputJson) {
    const webSocket = new WebSocket(webSocketUrl);

    webSocket.onopen = function () {
        const textElement = document.createElement("p");
        textElement.innerText = "Connection opened";
        outputDiv.appendChild(textElement);
    }

    webSocket.onmessage = function (event) {
        processWebSocketMessage(event.data, outputDiv, webSocket, inputJson);
    }

    webSocket.onclose = function () {
        const textElement = document.createElement("p");
        textElement.innerText = "Connection closed";
        outputDiv.appendChild(textElement);
    }

    return webSocket;
}
