//hi-lighting user tab
var sidePanelTabs = document.querySelectorAll(".task-link.task-link-no-confirm");
var UserTab = sidePanelTabs[1];
var GroupTab= sidePanelTabs[2];
var currentPath = window.location.pathname;
if (currentPath.includes("userManagement/users")) {
    UserTab.classList.add("task-link--active");
} else if (currentPath.includes("userManagement/groups")) {
    GroupTab.classList.add("task-link--active");
}

//Function to select all boxes in filtered/original table
var mainCheckbox = document.getElementById("selectAllCheckbox");
if (mainCheckbox) {
    mainCheckbox.addEventListener("click", function () {
        var visibleCheckboxes = document.querySelectorAll('.user-row:not([style*="display: none"]) input[name="selectedUsers"]');
        var isChecked = mainCheckbox.checked;
        visibleCheckboxes.forEach(function(checkbox) {
            checkbox.checked = isChecked;
        });
    });
}

//bottom alert banner
document.addEventListener("DOMContentLoaded", function () {
    // Select all spans with the class 'actionLink'
    var allSpans = document.querySelectorAll('.actionLink');
    var allAlertTriggers = Array.from(allSpans);
    allAlertTriggers.push(document.querySelector('.jenkins-button'));
    allAlertTriggers.forEach(function (span) {
        span.addEventListener('click', function () {
            notificationBar.show("Available in Premium version !", notificationBar.WARNING);
        });
    });
})

//Function to select all group boxes in filtered/original table
var mainGroupCheckbox = document.getElementById("selectAllGroupsCheckbox");
if (mainGroupCheckbox) {
    mainGroupCheckbox.addEventListener("click", function () {
        var visibleCheckboxes = document.querySelectorAll('.group-row:not([style*="display: none"]) input[name="selectedGroups"]');
        var isChecked = mainGroupCheckbox.checked;
        visibleCheckboxes.forEach(function(checkbox) {
            checkbox.checked = isChecked;
        });
    });
}

// Function to filter table rows based on search input
function filterTable() {
         var input = document.getElementById("userSearch");
         var filter = input.value.toLowerCase();
         var rows = document.querySelectorAll(".user-row");

         rows.forEach(function(row) {
               var name = row.querySelector(".user-name").textContent.toLowerCase();
               var id = row.querySelector(".user-id").textContent.toLowerCase();
               if (name.includes(filter) || id.includes(filter)) {
                   row.style.display = ""; // Show row if it matches
               }
               else {
                  row.style.display = "none"; // Hide row if it doesn't match
               }
         });
}

// Function to filter group table rows based on search input
function filterGroupTable() {
    var input = document.getElementById("groupSearch");
    var filter = input.value.toLowerCase();
    var rows = document.querySelectorAll(".group-row");

    rows.forEach(function(row) {
        var name = row.querySelector(".group-name").textContent.toLowerCase();
        if (name.includes(filter)) {
            row.style.display = "";
        }
        else {
            row.style.display = "none";
        }
    });
}

//Handling form submission for users
const form = document.querySelector("#Form .apply-button");
if (form) {
    form.addEventListener('click', formSubmit);
}

function formSubmit() {
    let selectedUserIds = JSON.parse(document.getElementById('selectedUserIds').value || "[]");
    if (selectedUserIds.length === 0) {
        document.querySelectorAll('input[name="selectedUsers"]:checked').forEach(checkbox => {
            selectedUserIds.push(checkbox.value);
        });
        document.getElementById('selectedUserIds').value = JSON.stringify(selectedUserIds);
    }
}

//Handling form submission for groups
const groupForm = document.querySelector("#GroupForm .apply-button");
if (groupForm) {
    groupForm.addEventListener('click', formSubmitGroups);
}

function formSubmitGroups() {
    let selectedGroupNames = JSON.parse(document.getElementById('selectedGroupNames').value || "[]");
    // Only populate from checkboxes if no data is already set (for bulk actions)
    if (selectedGroupNames.length === 0) {
        document.querySelectorAll('input[name="selectedGroups"]:checked').forEach(checkbox => {
            selectedGroupNames.push(checkbox.value);
        });
        if (selectedGroupNames.length > 0) {
            document.getElementById('selectedGroupNames').value = JSON.stringify(selectedGroupNames);
        }
    }
    // If data is already set (from individual actions), don't overwrite it
}

document.addEventListener("DOMContentLoaded", function () {
    const createGroupBtn = document.getElementById("createGroupBtn");
    const groupCreationForm = document.getElementById("groupCreationForm");
    const saveGroupBtn = document.getElementById("saveGroupBtn");
    const cancelGroupBtn = document.getElementById("cancelGroupBtn");
    const newGroupNameInput = document.getElementById("newGroupName");

    if (createGroupBtn) {
        createGroupBtn.addEventListener("click", function(e) {
            e.preventDefault();
            groupCreationForm.style.display = "block";
            newGroupNameInput.focus();
        });
    }

    if (cancelGroupBtn) {
        cancelGroupBtn.addEventListener("click", function(e) {
            e.preventDefault();
            groupCreationForm.style.display = "none";
            newGroupNameInput.value = "";
        });
    }

    if (saveGroupBtn) {
        saveGroupBtn.addEventListener("click", function(e) {
            e.preventDefault();
            const groupName = newGroupNameInput.value.trim();
            if (groupName) {
                // Create a form and submit it
                const form = document.createElement('form');
                form.method = 'post';
                form.action = 'createGroup';

                const input = document.createElement('input');
                input.type = 'hidden';
                input.name = 'groupName';
                input.value = groupName;

                form.appendChild(input);
                document.body.appendChild(form);
                form.submit();
            } else {
                alert('Please enter a group name');
            }
        });
    }
});

function reloadPageAfterDelay() {
    setTimeout(function() {
       window.location.reload();
    }, 1500);
}