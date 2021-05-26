# Web socket API

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
    "messageType": "INFO",
    "message": "notFound"
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
    "messageType": "INFO",
    "message": "invalidInputJson"
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
    }
}
```

The top-level `success` field indicates if the script execution finished without any unhandled errors. When this value
is `true`, then the `payload` will contain and object as in the example above. When the value is `false`, then the
payload will contain a string - error message description instead of an object.

## Web socket command messages

Command messages are sent to the web socket client to indicate some action or input is required. Web socket client
should send a message via web socket only when a command which requires some web socket input is received.

### Input commands

These commands require some input from the user, which means that web socket client must send a response when it becomes
available.

#### ReadInputJsonBoolean command

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

#### ReadBoolean command

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

#### ReadNumber command

Requests number input from the user. Web socket message:

```json
{
    "messageType": "COMMAND",
    "command": "readNumber",
    "message": "Message to display to the user"
}
```

Response: web socket client should send a number which was provided by the user.

#### ReadString command

Requests string input from the user. Web socket message:

```json
{
    "messageType": "COMMAND",
    "command": "readString",
    "message": "Message to display to the user"
}
```

Response: web socket client should send a raw (non-quoted) string which was provided by the user.

#### ReadFields command

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

#### Button command

Requests button click from the user. Web socket message:

```json
{
    "messageType": "COMMAND",
    "command": "button",
    "message": "Button message"
}
```

Response: web socket client should send any value when the user clicks on the button.

### Output commands

These commands inform the web socket client that something needs to be rendered and displayed to the user. The client
should not respond to such commands.

#### RenderText command

Requests rendering of the provided text. Web socket message:

```json
{
    "messageType": "COMMAND",
    "command": "renderText",
    "text": "Text to render"
}
```

No response should be sent by the web socket client.

#### RenderHtml command

Requests rendering of the provided HTML. Web socket message:

```json
{
    "messageType": "COMMAND",
    "command": "renderHtml",
    "text": "<p>HTML to render</p>"
}
```

No response should be sent by the web socket client.

#### RenderText command

Requests rendering of the provided Markdown. Web socket message:

```json
{
    "messageType": "COMMAND",
    "command": "renderMarkdown",
    "text": "## Markdown to render"
}
```

No response should be sent by the web socket client.
