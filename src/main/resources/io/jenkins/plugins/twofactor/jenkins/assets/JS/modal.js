var modal = document.getElementById("modal-dialog");
var uploadJson = document.getElementById("upload-saml-json-config");

var modalCloseButton = document.getElementById('close-btn');
modalCloseButton.addEventListener("click",handleCloseBtn);

var showModalButton = document.getElementById('modal-open-button');
showModalButton.addEventListener("click",handleModal);

const GetYourPremiumTrialBtn = document.getElementById('GetYourPremiumTrialBtn');
const GetYourPremiumTrialBtn2 = document.getElementById('GetYourPremiumTrialBtn2');
const confirmButton = document.getElementById('confirm-button');
const cancelButton = document.getElementById('cancel-button');
const closeButton = document.querySelector('.close-btn');

function handleModal() {
    modal.style.display = "block";
}

function handleCloseBtn() {
    modal.style.display = "none";
}
function uploadConfig() {
    uploadJson.style.display = "block";
}

function submitInstallForm() {
    document.getElementById('installPluginForm').submit();
}
function showConfirmationPopup() {
    modalCloseButton.click();
    document.getElementById('overlay').style.display = 'block';
    document.getElementById('confirmationPopup').style.display = 'block';
}
function closeConfirmationPopup() {
    document.getElementById('overlay').style.display = 'none';
    document.getElementById('confirmationPopup').style.display = 'none';
}
function submitConfirmationPopupForm() {
    return new Promise((resolve, reject) => {
        const name = document.getElementById('name').value;
        const orgName = document.getElementById('orgName').value;
        const email = document.getElementById('email').value;

        const isValidName = /^[a-zA-Z ]{2,30}$/.test(name);
        const isValidEmail = /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);

        if (!isValidName) {
            alert('Please enter a valid name (2-30 characters, letters and spaces only).');
            reject('Invalid name');
            return;
        }
        if (!isValidEmail) {
            alert('Please enter a valid email address.');
            reject('Invalid email');
            return;
        }
        document.forms['confirmationForm'].submit();
        setTimeout(() => {
            resolve();
        }, 3000);
    });
}

window.onclick = function (event) {
    if (event.target == modal) {
        modal.style.display = "none";
    }

    if (event.target == uploadJson) {
        uploadJson.style.display = "none";
    }
}

if(GetYourPremiumTrialBtn){
    GetYourPremiumTrialBtn.addEventListener('click',function (){
        showConfirmationPopup();
    });
}
if(GetYourPremiumTrialBtn2){
    GetYourPremiumTrialBtn2.addEventListener('click',function (){
        showConfirmationPopup();
    });
}
if (closeButton){
    closeButton.addEventListener('click',()=>{
        closeConfirmationPopup();
    })
}
if (confirmButton) {
    confirmButton.addEventListener('click', async function () {
        console.log("calling submitConfirmationPopupForm function.. ");
        try {
            await submitConfirmationPopupForm();
            submitInstallForm();
        } catch (error) {
            console.log("Error occurred:", error);
        }
    });
}
if (cancelButton){
    cancelButton.addEventListener('click',()=>{
        console.log("button clicked")
        closeConfirmationPopup();
    })
}


