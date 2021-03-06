# Interactive script web socket API

Scripts can be run interactively via web socket. When running in this mode, input values can be requested from the user
and rendering of text, HTML and Markdown is possible. All the web socket messages sent by the auditor application will
be in JSON format. Client responses do not necessarily need to be in JSON format and depend on the web socket command
which was sent by the auditor application to the client.

## Storing scripts for interactive execution

Before the script can be executed interactively, it must be made available to the auditor application. This can be done
by storing the script via `POST /script/store` call. The script should be sent to this endpoint as `text/plain` content
type. When the script is stored, it will get a UUID under which it can be accessed for interactive execution. This UUID
is returned as a response to the `POST /script/store` call in the following JSON format:

```json
{
    "id": "2c504f32-13af-49b3-83ee-43ce6bb0b447"
}
```

To verify that the script was stored correctly under the returned UUID, the following request can be made:
`GET /script/load/{uuid}`. This endpoint will return the stored script under the provided UUID as `text/plain` content
type. If the script cannot be found for the provided UUID, 404 HTTP status code is returned.

## Interactive script execution via web socket

To start a stored script interactively, simply connect to `/script/interactive/{uuid}` via web socket. To associate an
IPFS directory with the script being executed, provide the IPFS directory hash via `ipfs-directory` query parameter. All
the files in the provided IPFS directory will then be readable via `Ipfs.getFile()` method by providing the file name
inside the directory. Upon establishing the connection, auditor application will send a response to the web socket:

```json
{
    "messageType": "INFO",
    "message": "connected"
}
```

This first response informs the client that the connection is successfully established, and the script is being looked
up in the auditor application. If the script cannot be found, then the following message will be sent next to the web
socket client:

```json
{
    "messageType": "ERROR",
    "error": "notFound",
    "message": "Error details"
}
```

After this message the web socket connection will be closed as it is not possible to execute non-existent script. On the
other hand, if the script was found then the following message will be sent to web socket client instead:

```json
{
    "messageType": "COMMAND",
    "command": "readInputJson",
    "message": "Please provide script input JSON"
}
```

This message indicates that the client should send a JSON value which will be used as an input argument of the script.
If the provided value is not a valid JSON, the following message will be returned, and the script execution will not
start:

```json
{
    "messageType": "ERROR",
    "error": "invalidInputJson",
    "message": "Error details"
}
```

If the input value is a valid JSON, then the following message is sent to the client:

```json
{
    "messageType": "INFO",
    "message": "executing"
}
```

This message informs the web socket client that the script is currently being executed. After the execution message, web
socket client may receive any number of command messages which depend on the script under execution. The command
messages are described further in this document. When the script execution finishes, the response message is sent to the
web socket client:

```json
{
    "messageType": "RESPONSE",
    "success": true,
    "message": "Script execution finished",
    "payload": {
        "success": true
    },
    "transaction": {
        "to": "target contract address",
        "data": "transaction data"
    }
}
```

The top-level `success` field indicates if the script execution finished without any unhandled errors. When this value
is `true`, then the `payload` will contain an object as in the example above. The `transaction` value is included only
when the audit was not aborted, otherwise it will not be present. When the value is `false`, then the `payload` will
contain a string - error message description instead of an object.

## Auditing script execution via web socket

Auditing procedure can be executed by connecting to `/audit` via web socket. The web socket messages received on this
endpoint are similar to the ones received on the interactive script execution endpoint - the main difference is that the
input JSON does not need to be provided as it is fetched from IPFS. There are three possible errors that can happen
before the script execution starts:

1) Invalid input JSON - this message is sent if the audit info file contains invalid JSON

```json
{
    "messageType": "ERROR",
    "error": "invalidInputJson",
    "message": "Error details"
}
```

2) RPC error - this message is sent if it is not possible to connect to RPC or read values from the auditing contracts

```json
{
    "messageType": "ERROR",
    "error": "rpcError",
    "message": "Error details"
}
```

3) IPFS read error - this message is sent if IPFS is not available or if any specific file cannot be fetched

```json
{
    "messageType": "ERROR",
    "error": "ipfsReadError",
    "message": "Error details"
}
```

## Web socket command messages

Command messages are sent to the web socket client to indicate some action or input is required. Web socket client
should send a message via web socket only when a command which requires some web socket input is received.

### Input commands

These commands require some input from the user, which means that web socket client must send a response when it becomes
available.

<details>
<summary><b>ReadInputJson command</b></summary>

Requests the script input JSON from the user. This message is sent only once when the script is being executed
interactively - before the script is executed. Web socket message:

```json
{
    "messageType": "COMMAND",
    "command": "readInputJson",
    "message": "Please provide script input JSON"
}
```

Response: web socket client should send a valid JSON value. This can be a JSON array, object, string, number, boolean or
null.

</details>

<details>
<summary><b>ReadBoolean command</b></summary>

Requests boolean input from the user. Web socket message:

```json
{
    "messageType": "COMMAND",
    "command": "readBoolean",
    "message": "Message to display to the user"
}
```

Response: web socket client should send a raw (non-quoted) string containing either `true` or `false`, depending on the
user input.

</details>

<details>
<summary><b>ReadNumber command</b></summary>

Requests number input from the user. Web socket message:

```json
{
    "messageType": "COMMAND",
    "command": "readNumber",
    "message": "Message to display to the user"
}
```

Response: web socket client should send a number which was provided by the user.

</details>

<details>
<summary><b>ReadString command</b></summary>

Requests string input from the user. Web socket message:

```json
{
    "messageType": "COMMAND",
    "command": "readString",
    "message": "Message to display to the user"
}
```

Response: web socket client should send a raw (non-quoted) string which was provided by the user.

</details>

<details>
<summary><b>ReadFields command</b></summary>

Requests multiple inputs from the user. Web socket message:

```json
{
    "messageType": "COMMAND",
    "command": "readFields",
    "message": "Message to display to the user",
    "fields": [
        {
            "type": "BOOLEAN",
            "name": "booleanField",
            "description": "Boolean field description for the user"
        },
        {
            "type": "NUMBER",
            "name": "numberField",
            "description": "Number field description for the user"
        },
        {
            "type": "STRING",
            "name": "stringField",
            "description": "String field description for the user"
        }
    ]
}
```

Response: web socket client should send one message for each of the objects in the `fields` array. The order of the
messages must match the order of the fields in the array. For the example message above, web socket client should send
three messages: first a boolean, then a number and finally a string. The `fields` array can contain any number of
objects which describe the required input fields, not only three as shown above. The available field types are:
`BOOLEAN`, `NUMBER` and `STRING`. The `name` value will be the identifier of the field in the running script, which
should not be important to the web socket client, and it can be ignored. Each field has its `descritpion` which should
be displayed to the user. The `message` top-level field acts as a general message the user should see when being
prompted to input the specified fields.

</details>

<details>
<summary><b>Button command</b></summary>

Requests button click from the user. Web socket message:

```json
{
    "messageType": "COMMAND",
    "command": "button",
    "message": "Button message"
}
```

Response: web socket client should send any value when the user clicks on the button.

</details>

### Output commands

These commands inform the web socket client that something needs to be rendered and displayed to the user. The client
should not respond to such commands.

<details>
<summary><b>RenderText command</b></summary>

Requests rendering of the provided text. Web socket message:

```json
{
    "messageType": "COMMAND",
    "command": "renderText",
    "text": "Text to render"
}
```

No response should be sent by the web socket client.

</details>

<details>
<summary><b>RenderHtml command</b></summary>

Requests rendering of the provided HTML. Web socket message:

```json
{
    "messageType": "COMMAND",
    "command": "renderHtml",
    "text": "<p>HTML to render</p>"
}
```

No response should be sent by the web socket client.

</details>

<details>
<summary><b>RenderText command</b></summary>

Requests rendering of the provided Markdown. Web socket message:

```json
{
    "messageType": "COMMAND",
    "command": "renderMarkdown",
    "text": "## Markdown to render"
}
```

No response should be sent by the web socket client.

</details>

### Other commands

<details>
<summary><b>SpecifyIpfsDirectoryHash command</b></summary>

Requests IPFS hash of a directory which will be set as audit result directory. This command can only be received once
per web socket client connection. Web socket message:

```json
{
    "messageType": "COMMAND",
    "command": "specifyIpfsDirectoryHash",
    "payload": {
        "success": true
    }
}
```

Response: web socket client should send IPFS hash of the directory which should be set as audit result directory.

</details>

## Web socket info messages

Web socket messages which are purely informational. Web socket client should not respond to these messages.

<details>
<summary><b>Connected info message</b></summary>

Sent after successfully opening the web socket connection. Web socket message:

```json
{
    "messageType": "INFO",
    "message": "connected"
}
```

No response should be sent by the web socket client.

</details>

<details>
<summary><b>Executing info message</b></summary>

Send to indicate that the script execution has started. Web socket message:

```json
{
    "messageType": "INFO",
    "message": "executing"
}
```

No response should be sent by the web socket client.

</details>

## Web socket error messages

Web socket messages which indicate an error before the script execution has started. Web socket client should not
respond to these messages.

<details>
<summary><b>NotFound error message</b></summary>

Indicates that the requested script cannot be found. Web socket message:

```json
{
    "messageType": "ERROR",
    "error": "notFound",
    "message": "Error details"
}
```

No response should be sent by the web socket client.

</details>

<details>
<summary><b>InvalidInputJson error message</b></summary>

Indicates that the provided script input JSON is invalid. Web socket message:

```json
{
    "messageType": "ERROR",
    "error": "invalidInputJson",
    "message": "Error details"
}
```

No response should be sent by the web socket client.

</details>

<details>
<summary><b>IpfsRead error message</b></summary>

Indicates that IPFS is not reachable, or that a file cannot be found on IPFS. Web socket message:

```json
{
    "messageType": "ERROR",
    "error": "ipfsReadError",
    "message": "Error details"
}
```

No response should be sent by the web socket client.

</details>

<details>
<summary><b>Rpc error message</b></summary>

Indicates that RPC is not reachable, or that contract cannot be read. Web socket message:

```json
{
    "messageType": "ERROR",
    "error": "rpcError",
    "message": "Error details"
}
```

No response should be sent by the web socket client.

</details>
