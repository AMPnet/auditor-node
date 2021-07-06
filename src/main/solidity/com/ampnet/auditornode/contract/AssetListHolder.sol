// SPDX-License-Identifier: MIT
pragma solidity ^0.8.0;

import "./shared/Structs.sol";

interface AssetListHolder {
    function getAssets() external view returns (Structs.AssetDescriptor[] memory);
    function getAssetById(uint256 id) external view returns (Structs.AssetDescriptor memory);
}
