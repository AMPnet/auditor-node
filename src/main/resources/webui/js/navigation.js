const auditingButton = document.getElementById("auditing-nav");
const scriptDevelopmentButton = document.getElementById("script-development-nav");
let selectedNavbarButton = auditingButton;
const navbarButton = "navbar-button";
const navbarButtonSelected = "navbar-button-selected";
const auditingDiv = document.getElementById("auditing-div");
const scriptDevelopmentDiv = document.getElementById("script-development-div");
const hiddenDiv = "hidden-div"

function selectNav(button, otherButton, div, otherDiv) {
    if (selectedNavbarButton !== button) {
        selectedNavbarButton = button;
        button.classList.remove(navbarButton);
        button.classList.add(navbarButtonSelected);
        otherButton.classList.remove(navbarButtonSelected);
        otherButton.classList.add(navbarButton);
        div.classList.remove(hiddenDiv);
        otherDiv.classList.add(hiddenDiv);
    }
}

function selectAuditing() {
    selectNav(auditingButton, scriptDevelopmentButton, auditingDiv, scriptDevelopmentDiv);
}

function selectScriptDevelopment() {
    selectNav(scriptDevelopmentButton, auditingButton, scriptDevelopmentDiv, auditingDiv);
}
