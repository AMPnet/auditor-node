# Auditing scripts

The auditor application supports running JavaScript files in order to perform the auditing procedure. The provided
JavaScript runtime is compatible with [ECMAScript 2020 specification](https://262.ecma-international.org/11.0/) which is
powered by GraalVM. For more info, check out
[GraalVM JavaScript Compatibility](https://www.graalvm.org/reference-manual/js/JavaScriptCompatibility/).

## Entry point and return value

Each auditing script should define a function called `audit()` with a single argument. This function should do all the
necessary steps in the auditing process and return an `AuditResult` at the end. The simplest possible auditing script is
the following:

```javascript
function audit(auditData) {
    return AuditResult.success();
}
```

The input argument `auditData` is a JSON object which will vary depending on asset category. See script input
documentation for more info.

## Script API

Auditing scripts can call methods and read fields from the objects specified in this section. Apart from the API
described here, several other global objects are available (e.g. `console` and `JSON`) which can also be used. IO
operations, process and thread creation are not allowed in the auditing scripts (unless when available via API
documented below). Auditing scripts can be tested via `POST /script/execute` when the application is running. This
endpoint expects the script payload as `text/plain` content type.

### Models

Models are objects which are used primarily for the data transfer between the auditing script and its runtime.

#### AuditResult

Model of the result that should be returned from the `audit()` function.  
Static object name: `AuditResult`

###### Fields

There are no readable fields.

###### Methods

| Signature | Description | Example call |
| --------- | ----------- | ------------ |
| `success(): AuditResult` | Used to create a successful `AuditResult` object. | `AuditResult.success();` |
| `failure(message: String): AuditResult` | Used to create a failed `AuditResult` object with provided message. | `AuditResult.failure("Owner mismatch");` |
| `aborted(message: String): AuditResult` | Used to create an aborted `AuditResult` object with provided message. | `AuditResult.aborted("Will be audited later");` |

#### List&lt;E&gt;

Model of the lists returned from the API objects. Contains elements of type `E`.  
There is no static object available in the scripts.

###### Fields

| Field | Description |
| ----- | ----------- |
| `length: Int` | Number of elements in the list. |

###### Methods

| Signature | Description | Example call |
| --------- | ----------- | ------------ |
| <code>get(index: Int): E &#124; null</code> | Fetch an item at the specified index from the list. | `someList.get(0);` |

#### Map&lt;K, V&gt;

Model of the maps returned from the API objects. Contains elements of type `V` stored under keys of type `K`.  
There is no static object available in the scripts.

###### Fields

| Field | Description |
| ----- | ----------- |
| `size: Int` | Number of elements in the map. |

###### Methods

| Signature | Description | Example call |
| --------- | ----------- | ------------ |
| <code>get(key: K): V &#124; null</code> | Fetch an item for the specified key from the map. | `someMap.get("exampleKey");` |
| `keys(): List<K>` | Returns a list of all the keys contained in the map. | `someMap.keys();` |

#### HttpCookie

Model of HTTP cookie objects.  
There is no static object available in the scripts.

###### Fields

| Field | Description |
| ----- | ----------- |
| `name: String` | Name of the cookie. |
| `value: String` | Value of the cookie. |
| <code>domain: String &#124; null</code> | Cookie domain, if specified. |
| <code>path: String &#124; null</code> | Cookie path, if specified. |
| `httpOnly: Boolean` | Specifies whether the cookie is HTTP-only. |
| `secure: Boolean` | Specifies whether the cookie is secure. |
| `maxAge: Long` | Maximum age of the cookie in seconds. |
| `sameSite: String` | `SameSite` attribute value of the cookie. |

###### Methods

There are no methods available.

#### HttpResponse

Model of HTTP response objects.  
There is no static object available in the scripts.

###### Fields

| Field | Description |
| ----- | ----------- |
| <code>body: String &#124; null</code> | Response body. |
| `statusCode: Int` | Response status code. |
| `headers: Map<String, List<String>>` | Response headers. |
| `cookies: List<HttpCookie>` | Response cookies. |

###### Methods

There are no methods available.

### Utilities

Utility objects provide ease of access between JavaScript and the scripting runtime environment.

#### Converters

Contains utility functions to convert values between JavaScript native objects and script models.  
Static object name: `Converters`

###### Fields

There are no readable fields.

###### Methods

| Signature | Description | Example call |
| --------- | ----------- | ------------ |
| `arrayToList(array: Array<?>): List<?>` | Converts JavaScript array into a List model. If the provided argument is not an array, empty list will be returned. | `Converters.arrayToList([1, 2, 3]);` |
| `objectToMap(obj: Object): Map<String, ?>` | Converts JavaScript object into a Map model. If the provided argument is not an object, empty map will be returned. | `Converters.objectToMap({ example: true });` |
| `listToArray(list: List<?>): Array<?>` | Converts List model into a JavaScript array. If the provided argument is not a List model, this method will either return an empty array or throw an exception. | `Converters.listToArray(someList);` |
| `mapToObject(map: Map<?, ?>): Object` | Converts Map model into a JavaScript object. If the provided argument is not a Map mode, this method will either return an empty object or throw an exception. | `Converters.mapToObject(someMap);` |

#### Properties

Properties object contains all the application properties with `script.properties` prefix which were defined as
`-script.properties.<propertyName>=<propertyValue>` program arguments or otherwise. An important note here is that all
the defined property names will be visible through `kebab-case`. For example, specifying
`-script.properties.examplePropety=exampleValue` will cause the `example-property` to be visible in the `Properties`
object:

```javascript
console.log(Properties["example-property"]); // prints out "exampleValue"
```

Static object name: `Properties`

###### Fields

The fields of this object are dynamic and depend on the available `script.properties` values. All the fields will always
be of the `String` type.

| Field | Description |
| ----- | ----------- |
| `<propertyName>: String` | Value of the property under `<propertyName>` key. |

###### Methods

There are no methods available.

### API

Objects which provide various APIs (e.g. HTTP requests).

#### HttpClient

Provides support for making blocking HTTP calls from the auditing scripts. Request and response bodies are always of
`String` type, and the default request content type is `application/json` if request body is provided. This content type
can be changed by specifying the `Content-Type` header value.  
Static object name: `HttpClient`

###### Fields

There are no readable fields.

###### Methods

| Signature | Description | Example call |
| --------- | ----------- | ------------ |
| `get(url: String): HttpResponse` | Sends a `GET` request to the specified URL and returns the response. | `HttpClient.get("http://example.com/");` |
| `get(url: String, headers: Object): HttpResponse` | Sends a `GET` request with provided headers to the specified URL and returns the response. The headers object should consist of key-value pairs which are of `String` type. | `HttpClient.get("http://example.com/", { "Accept": "application/json" });` |
| `post(url: String): HttpResponse` | Sends a `POST` request with empty request body to the specified URL and returns the response. | `HttpClient.post("http://example.com/");` |
| `post(url: String, body: String): HttpResponse` | Sends a `POST` request with provided request body to the specified URL and returns the response. | `HttpClient.post("http://example.com/", "exampleRequestBody");` |
| `post(url: String, body: String, headers: Object): HttpResponse` | Sends a `POST` request with provided request body and headers to the specified URL and returns the response. The headers object should consist of key-value pairs which are of `String` type. | `HttpClient.post("http://example.com/", "exampleRequestBody", { "Accept": "application/json" });` |
| `request(url: String, method: String): HttpResponse` | Sends a request with specified HTTP method to the specified URL and returns the response. | `HttpClient.request("http://example.com/", "CUSTOM_METHOD");` |
| `request(url: String, method: String, body: String): HttpResponse` | Sends a request with specified HTTP method and request body to the specified URL and returns the response. | `HttpClient.request("http://example.com/", "CUSTOM_METHOD", "exampleRequestBody");` |
| `request(url: String, method: String, body: String, headers: Object): HttpResponse` | Sends a request with specified HTTP method, request body and headers to the specified URL and returns the response. The headers object should consist of key-value pairs which are of `String` type. | `HttpClient.request("http://example.com/", "CUSTOM_METHOD", "exampleRequestBody", { "Accept": "application/json" });` |

#### Input

Provides support for user input when script is running interactively via web socket. When script is not running
interactively, all methods of this object will always return `null`. See web socket documentation for more info on
running scripts interactively.  
Static object name: `Input`

###### Fields

There are no readable fields.

###### Methods

| Signature | Description | Example call |
| --------- | ----------- | ------------ |
| <code>readBoolean(message: String): Boolean &#124; null</code> | Requests a boolean input from the user via web socket and returns the value when it becomes available. | `Input.readBoolean("Did you check this box?");` |
| <code>readNumber(message: String): Number &#124; null</code> | Requests a number input from the user via web socket and returns the value when it becomes available. Returns `null` for invalid values. | `Input.readNumber("The answer is:");` |
| <code>readString(message: String): String &#124; null</code> | Requests a number input from the user via web socket and returns the value when it becomes available. | `Input.readString("Name:");` |
| <code>readFields(fields: Object, message: String): Map&lt;String, Boolean &#124; Number &#124; String&gt; &#124; null</code> | Requests multiple fields from the user. The fields can be specified via the `fields` argument which is described below this table. The return value is a map which consists of field identifiers and their values. | Example given below. |
| `button(message: String): Void` | Requests button click from the user via web socket and blocks execution until the user clicks on the button. Does not block execution and returns immediately if the script is not running interactively. | `Input.button("Continue");` |

The `fields` argument of `readFields` method must be a list of JavaScript objects which describe the required input
fields that the user should fill in. The format of the object is:

```json
{
    "type": "boolean",
    "name": "fieldName",
    "description": "field description"
}
```

The `type` of the field can be one of: `boolean`, `number` or `string`. The `name` is the field identifier, and it
should be unique because the returned map will use it as a key. The `description` is the field description which should
be displayed to the user.

Full example on calling `readFields` method:

```javascript
const fields = [
    {
        "type": "boolean",
        "name": "booleanField",
        "description": "Yes/no?"
    },
    {
        "type": "number",
        "name": "numberField",
        "descripiton": "Enter a number:"
    },
    {
        "type": "string",
        "name": "stringField",
        "description": "Enter some text:"
    }
];

const userInput = Input.readFields(fields, "Form header message");

console.log(userInput.get("booleanField"));
console.log(userInput.get("numberField"));
console.log(userInput.get("stringField"));
```

#### Output

Provides support for rendering text, HTML and Markdown when the script is running interactively via web socket. When the
script is not running interactively, the methods will do nothing. See web socket documentation for more info on running
scripts interactively.  
Static object name: `Output`

###### Fields

There are no readable fields.

###### Methods

| Signature | Description | Example call |
| --------- | ----------- | ------------ |
| `renderText(text: String): Void` | Requests rendering of provided text. | `Output.renderText("example");` |
| `renderHtml(html: String): Void` | Requests rendering of provided HTML. | `Output.renderHtml("<p>example<p/>");` |
| `renderMarkdown(markdown: String): Void` | Requests rendering of provided Markdown. | `Output.renderMarkdown("# Example");` |

#### Ipfs

Provides support for fetching files located in the same IPFS directory which contains the auditing script. If the loaded
script was provided locally instead of via IPFS and no IPFS directory was specified, then `null` will always be
returned. For specifying the IPFS directory for locally provided scripts, see web socket documentation.  
Static object name: `Ipfs`

###### Fields

There are no readable fields.

###### Methods

| Signature | Description | Example call |
| --------- | ----------- | ------------ |
| <code>getFile(fileName: String): String &#124; null</code> | Reads content of the IPFS file with provided name. The file must be located in the IPFS directory associated with the script. If the file cannot be found, `null` is returned. | `Ipfs.getFile("example.html");` |
