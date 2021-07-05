// SPDX-License-Identifier: MIT
pragma solidity ^0.8.0;

import "./shared/Structs.sol";

interface ApxCoordinator {

    // This dummy function is needed for Web3j to correctly generate structure class. This is because Web3j does not
    // generate structure classes when structure is used only in an array (e.g. for Structs.Auditor[]). We don't need
    // to do this for Structs.AuditorPool because we already have a function which uses that structure without an array:
    // function getPoolById(uint256 id) external view returns (Structs.AuditorPool memory);
    function _define_struct_Auditor(Structs.Auditor memory auditor) external;

    // getters
    function stablecoin() external view returns (address);
    function assetListHolder() external view returns (address);
    function auditGapDuration() external view returns (uint256);
    function usdcPerAudit() external view returns (uint256);
    function usdcPerList() external view returns (uint256);

    // functions
    function getPoolMemberships(address auditor) external view returns (uint256[] memory);
    function getPools() external view returns (Structs.AuditorPool[] memory);
    function getPoolById(uint256 id) external view returns (Structs.AuditorPool memory);
    function getPoolMembers(uint256 id) external view returns (Structs.Auditor[] memory);
    function performAudit(uint256 assetId, bool assetValid, string memory additionalInfo) external;
}
