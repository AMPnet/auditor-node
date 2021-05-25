# Auditing script input

The script input JSON value can be different depending on the asset jurisdiction. The JSON should contain all the data
necessary for successful asset audit.

### Croatia

Input JSON:

```json
{
    "assetInfo": {
        "cadastralOffice": "ZAGREB",
        "cadastralMunicipalityName": "CENTAR",
        "cadastralMunicipalityNumber": 335240,
        "parcelNumber": 9999,
        "parcelSubNumber": 99
    },
    "ownerInfo": [
        {
            "ordinalNumber": 1,
            "totalAssetShares": "10000/10000",
            "ownerName": "John Doe",
            "pid": "12345678901",
            "subdivisionShares": "100/100"
        }
    ]
}
```
