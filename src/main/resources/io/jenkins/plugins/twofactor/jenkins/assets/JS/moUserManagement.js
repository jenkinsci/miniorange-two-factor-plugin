//hi-lighting user tab
var sidePanelTabs = document.querySelectorAll(".task-link.task-link-no-confirm");
const currentLocation = window.location.href;
    sidePanelTabs.forEach(function (tab){
        if(tab.href===currentLocation){
           tab.classList.add('task-link--active');
        }
    });

//Function to select all boxes in filtered/original table
var mainCheckbox = document.getElementById("selectAllCheckbox");
mainCheckbox.addEventListener("click", function () {
    var visibleCheckboxes = document.querySelectorAll('.user-row:not([style*="display: none"]) input[name="selectedUsers"]');
    var isChecked = mainCheckbox.checked;
    visibleCheckboxes.forEach(function(checkbox) {
        checkbox.checked = isChecked;
    });
});

//filtering users in table from search bar
function filterTable() {
         var input = document.getElementById("userSearch");
         var filter = input.value.toLowerCase();
         var rows = document.querySelectorAll(".user-row");

         rows.forEach(function(row) {
               var name = row.querySelector(".user-name").textContent.toLowerCase();
               var id = row.querySelector(".user-id").textContent.toLowerCase();
               if (name.includes(filter) || id.includes(filter)) {
                   row.style.display = "";
               }
               else {
                  row.style.display = "none";
               }
         });
}

//prevent form submission.
const form = document.getElementById("Form");
form.addEventListener("submit", function (event) {
    event.preventDefault();
});

//bottom alert banner
document.addEventListener("DOMContentLoaded", function () {
    // Select all spans with the class 'actionLink'
    var allSpans = document.querySelectorAll('.actionLink');
    var allAlertTriggers = Array.from(allSpans);
    allAlertTriggers.push(document.querySelector('.jenkins-button'));
    var banner = document.getElementById('premiumBanner');
     allAlertTriggers.forEach(function (span) {
             span.addEventListener('click', function () {
                 banner.style.display = 'block';
                 setTimeout(function () {
                    banner.style.display = 'none';
                 }, 3000);
            });
     });
})