document.addEventListener("DOMContentLoaded", function () {
    const checkboxes = document.querySelectorAll(".checkboxToSkip");
    checkboxes.forEach((checkbox) => {
        checkbox.addEventListener("click", function (event) {
            event.preventDefault();
        });
    });
});
