document.addEventListener("DOMContentLoaded", function () {
    const buttons = document.querySelectorAll(".resend-otp");
    buttons.forEach((button) => {
        button.addEventListener("click", function (event) {
            event.preventDefault();
            const form = button.closest("form"); // Find the closest parent form
            if (form) {
                form.submit();
            }
        });
    });
});
