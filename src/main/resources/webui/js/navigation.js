const auditingButton = document.getElementById("auditing-nav");
const scriptDevelopmentButton = document.getElementById("script-development-nav");
const auditingDiv = document.getElementById("auditing-div");
const scriptDevelopmentDiv = document.getElementById("script-development-div");
const navbarButtonSelectedClass = "navbar-button-selected";
const navbarButtonClass = "navbar-button";
const hiddenClass = "hidden"
let selectedNavbarButton = auditingButton;

function selectNav(button, otherButton, div, otherDiv) {
    if (selectedNavbarButton !== button) {
        selectedNavbarButton = button;
        button.classList.remove(navbarButtonClass);
        button.classList.add(navbarButtonSelectedClass);
        otherButton.classList.remove(navbarButtonSelectedClass);
        otherButton.classList.add(navbarButtonClass);
        div.classList.remove(hiddenClass);
        otherDiv.classList.add(hiddenClass);
    }
}

function selectAuditing() {
    selectNav(auditingButton, scriptDevelopmentButton, auditingDiv, scriptDevelopmentDiv);
}

function selectScriptDevelopment() {
    selectNav(scriptDevelopmentButton, auditingButton, scriptDevelopmentDiv, auditingDiv);
}
