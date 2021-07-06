// SPDX-License-Identifier: MIT
pragma solidity ^0.8.0;

interface Structs {

    struct AssetDescriptor {
        address assetHolder;
        address tokenizedAsset;
        uint256 id;
        uint256 typeId;
        string name;
        string ticker;
    }

    struct AuditorPool {
        uint256 id;
        string name;
        string info;
        bool active;
        uint256 activeMembers;
    }

    struct Auditor {
        address auditor;
        uint256 totalAuditsPerformed;
        uint256 totalListingsPerformed;
        string info;
    }

    struct AuditResult {
        bool assetVerified;
        string additionalInfo;
        uint256 timestamp;
    }
}
