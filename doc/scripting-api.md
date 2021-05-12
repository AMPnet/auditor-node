# Auditing scripts

The auditor application supports running JavaScript files in order to perform the auditing procedure. The provided
JavaScript runtime is compatible with [ECMAScript 2020 specification](https://262.ecma-international.org/11.0/) which is
powered by GraalVM. For more info, check out
[GraalVM JavaScript Compatibility](https://www.graalvm.org/reference-manual/js/JavaScriptCompatibility/).

## Entry point and return value

Each auditing script should define a function called `audit()` which takes no arguments. This function should do all the
necessary steps in the auditing process and return an `AuditResult` at the end. The simplest possible auditing script is
therefore:

```javascript
function audit() {
    return AuditResult.of(true);
}
```

## Script API

Auditing scripts can call methods and read fields from the objects specified in this section. Apart from the API
described here, several other global objects are available (e.g. `console` and `JSON`) which can also be used. Auditing
scripts can be tested via `POST /script/execute` when the application is running. This endpoint expects the script
payload as `text/plain` content type.

### Models

Models are objects which are used primarily for the data transfer between the auditing script and its runtime.

#### AuditResult

Model of the result that should be returned from the `audit()` function.  
Object name: `AuditResult`

###### Fields

No fields are readable in the scripts.

###### Methods

| Signature | Description | Example call |
| --------- | ----------- | ------------ |
| `of(value: Boolean): AuditResult` | Constructs an `AuditResult` from the provided argument. | `AuditResult.of(true);` |

#### List&lt;E&gt;

Model of the lists returned from the API objects. Contains elements of type `E`.  
No static object is provided in the scripts.

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
No static object is provided in the scripts.

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
No static object is provided in the scripts.

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

No methods are available in the scripts.

#### HttpResponse

Model of HTTP response objects.  
No static object is provided in the scripts.

###### Fields

| Field | Description |
| ----- | ----------- |
| <code>body: String &#124; null</code> | Response body. |
| `statusCode: Int` | Response status code. |
| `headers: Map<String, List<String>>` | Response headers. |
| `cookies: List<HttpCookie>` | Response cookies. |

###### Methods

No methods are available in the scripts.
