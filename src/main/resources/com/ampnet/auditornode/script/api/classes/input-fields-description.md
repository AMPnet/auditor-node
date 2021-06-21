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
