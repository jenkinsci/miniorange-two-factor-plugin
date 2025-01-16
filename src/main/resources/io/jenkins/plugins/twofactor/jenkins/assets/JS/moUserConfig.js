document.addEventListener("DOMContentLoaded", function () {
    const resetButtons = document.querySelectorAll(".reset-button");
    resetButtons.forEach((button) => {
        button.addEventListener("click", function (event) {
            event.preventDefault();
            const form = button.closest("form");
            if (form) {
                form.submit();
            }
        });
    });
});
