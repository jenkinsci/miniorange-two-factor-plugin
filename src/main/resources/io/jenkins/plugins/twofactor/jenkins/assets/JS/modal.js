var modal = document.getElementById("modal-dialog");
var uploadJson = document.getElementById("upload-saml-json-config");

function handleModal() {
    modal.style.display = "block";
}

function handleCloseBtn() {
    modal.style.display = "none";
}
function uploadConfig() {
    uploadJson.style.display = "block";
}


window.onclick = function (event) {
    if (event.target == modal) {
        modal.style.display = "none";
    }

    if (event.target == uploadJson) {
        uploadJson.style.display = "none";
    }
}



