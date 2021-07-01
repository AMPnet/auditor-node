// SPDX-License-Identifier: MIT
pragma solidity ^0.8.0;

import "./shared/Structs.sol";

interface AssetHolder {

    // getters
    function id() external view returns (uint256);
    function typeId() external view returns (uint256);
    function info() external view returns (string memory);
    function tokenizedAsset() external view returns (address);
    function listedBy() external view returns (address);
    function listingInfo() external view returns (string memory);

    // functions
    function getLatestAudit() external view returns (Structs.AuditResult memory);
}
