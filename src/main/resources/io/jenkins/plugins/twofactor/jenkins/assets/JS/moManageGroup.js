document.addEventListener("DOMContentLoaded", function () {
    // Add User form handlers
    const addUserBtn = document.getElementById("addUserBtn");
    const userAdditionForm = document.getElementById("userAdditionForm");
    const addUserSelect = document.getElementById("addUser");


    if (addUserBtn) {
        addUserBtn.addEventListener("click", function(e) {
            e.preventDefault();
            userAdditionForm.style.display = "block";
            addUserSelect.focus();
        });
    }


    // Delete Group handler
    const deleteGroupBtn = document.getElementById("deleteGroupBtn");
    const deleteGroupModal = document.getElementById("deleteGroupModal");
    const groupNameToDelete = document.getElementById("groupNameToDelete");
    const modalCloseBtn = document.getElementById("modalCloseBtn");
    const cancelDeleteBtn = document.getElementById("cancelDeleteBtn");
    const confirmDeleteBtn = document.getElementById("confirmDeleteBtn");
    
    if (deleteGroupBtn) {
        deleteGroupBtn.addEventListener("click", function(e) {
            e.preventDefault();
            const groupName = deleteGroupBtn.getAttribute("data-group-name");
            groupNameToDelete.textContent = groupName;
            deleteGroupModal.style.display = "block";
        });
    }
    
    // Close modal handlers
    function closeModal() {
        deleteGroupModal.style.display = "none";
    }
    
    if (modalCloseBtn) {
        modalCloseBtn.addEventListener("click", closeModal);
    }
    
    if (cancelDeleteBtn) {
        cancelDeleteBtn.addEventListener("click", closeModal);
    }
    
    // Close modal when clicking backdrop
    if (deleteGroupModal) {
        deleteGroupModal.addEventListener("click", function(e) {
            if (e.target === deleteGroupModal || e.target.classList.contains("jenkins-modal-backdrop")) {
                closeModal();
            }
        });
    }
    
    // Confirm delete handler
    if (confirmDeleteBtn) {
        confirmDeleteBtn.addEventListener("click", function(e) {
            e.preventDefault();
            const groupName = deleteGroupBtn.getAttribute("data-group-name");
            
            // Create a form and submit it
            const form = document.createElement('form');
            form.method = 'post';
            form.action = 'deleteGroup';
            
            const input = document.createElement('input');
            input.type = 'hidden';
            input.name = 'groupName';
            input.value = groupName;
            
            form.appendChild(input);
            document.body.appendChild(form);
            form.submit();
        });
    }
});

