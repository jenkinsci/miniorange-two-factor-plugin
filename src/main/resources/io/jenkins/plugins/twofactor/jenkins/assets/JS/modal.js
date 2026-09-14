(function () {
    'use strict';

    var modal;
    var compareModal;
    var confirmationPopup;

    var lastFocusedBeforeModal = null;
    var lastFocusedBeforeCompare = null;

    function openPremiumModal() {
        if (!modal) {
            modal = document.getElementById('modal-dialog');
        }
        if (!modal) {
            return;
        }
        lastFocusedBeforeModal = document.activeElement;
        modal.classList.add('mo-premium-modal--visible');
        modal.setAttribute('aria-hidden', 'false');
        var closeBtn = document.getElementById('close-btn');
        if (closeBtn) {
            closeBtn.focus();
        }
    }

    function closePremiumModal() {
        if (!modal) {
            return;
        }
        modal.classList.remove('mo-premium-modal--visible');
        modal.setAttribute('aria-hidden', 'true');
        if (lastFocusedBeforeModal && typeof lastFocusedBeforeModal.focus === 'function') {
            lastFocusedBeforeModal.focus();
        }
    }

    function openCompareModal() {
        if (!compareModal) {
            compareModal = document.getElementById('mo-compare-modal-dialog');
        }
        if (!compareModal) {
            return;
        }
        lastFocusedBeforeCompare = document.activeElement;
        compareModal.classList.add('mo-compare-modal--visible');
        compareModal.setAttribute('aria-hidden', 'false');
        var closeBtn = document.getElementById('mo-compare-close-btn');
        if (closeBtn) {
            closeBtn.focus();
        }
    }

    function closeCompareModal() {
        if (!compareModal) {
            compareModal = document.getElementById('mo-compare-modal-dialog');
        }
        if (!compareModal) {
            return;
        }
        compareModal.classList.remove('mo-compare-modal--visible');
        compareModal.setAttribute('aria-hidden', 'true');
        if (lastFocusedBeforeCompare && typeof lastFocusedBeforeCompare.focus === 'function') {
            lastFocusedBeforeCompare.focus();
        }
    }

    function openCompareFromCapabilities() {
        closePremiumModal();
        window.setTimeout(openCompareModal, 100);
    }

    window.MoEvaluationModal = {
        open: openPremiumModal,
        close: closePremiumModal,
        openCompare: openCompareModal,
        openCompareFromCapabilities: openCompareFromCapabilities
    };

    function openCapabilitiesModal() {
        if (window.MoEvaluationScheduler && window.MoEvaluationScheduler.openEvaluationModal) {
            window.MoEvaluationScheduler.openEvaluationModal();
        } else if (window.MoEvaluationModal && window.MoEvaluationModal.open) {
            window.MoEvaluationModal.open();
        }
    }

    function tryDeepLinkOpen() {
        var search = window.location.search || '';
        if (search.indexOf('moOpenEvaluation=1') === -1) {
            return;
        }
        if (window.MoEvaluationScheduler && window.MoEvaluationScheduler.openEvaluationModal) {
            window.MoEvaluationScheduler.openEvaluationModal();
        } else {
            openPremiumModal();
        }
    }

    function openMonitorEnhancements(event) {
        if (event) {
            event.preventDefault();
        }
        if (document.getElementById('modal-dialog')) {
            openCapabilitiesModal();
            return;
        }
        var trigger = document.getElementById('mo-monitor-view-enhancements');
        var fallbackHref = trigger && trigger.getAttribute('data-fallback-href');
        if (fallbackHref) {
            window.location.href = fallbackHref;
        }
    }

    function bindPremiumGatedControls() {
        document.querySelectorAll('.disable_').forEach(function (el) {
            el.addEventListener('click', function (event) {
                event.preventDefault();
                event.stopPropagation();
                openCapabilitiesModal();
            });
        });

        document.querySelectorAll(
            'input[disabled][placeholder*="Premium"], textarea[readonly], select[style*="pointer-events: none"]'
        ).forEach(function (el) {
            el.addEventListener('click', function (event) {
                event.preventDefault();
                event.stopPropagation();
                openCapabilitiesModal();
            });
        });
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

    function submitInstallForm() {
        document.getElementById('installPluginForm').submit();
    }

    function closeConfirmationPopup() {
        document.getElementById('overlay').style.display = 'none';
        document.getElementById('confirmationPopup').style.display = 'none';
    }

    function showConfirmationPopup() {
        closeCompareModal();
        closePremiumModal();
        document.getElementById('overlay').style.display = 'block';
        document.getElementById('confirmationPopup').style.display = 'block';
    }

    function onBackdropClick(event) {
        if (!modal) {
            modal = document.getElementById('modal-dialog');
        }
        if (!compareModal) {
            compareModal = document.getElementById('mo-compare-modal-dialog');
        }
        if (modal && event.target === modal) {
            closePremiumModal();
        }
        if (compareModal && event.target === compareModal) {
            closeCompareModal();
        }
    }

    function onKeyDown(event) {
        if (event.key !== 'Escape') {
            return;
        }
        if (confirmationPopup && confirmationPopup.style.display === 'block') {
            closeConfirmationPopup();
            return;
        }
        if (!compareModal) {
            compareModal = document.getElementById('mo-compare-modal-dialog');
        }
        if (compareModal && compareModal.classList.contains('mo-compare-modal--visible')) {
            closeCompareModal();
            return;
        }
        if (!modal) {
            modal = document.getElementById('modal-dialog');
        }
        if (modal && modal.classList.contains('mo-premium-modal--visible')) {
            closePremiumModal();
        }
    }

    function onDocumentClick(event) {
        var target = event.target;
        if (!target || !target.closest) {
            return;
        }

        if (target.closest('.mo-premium-upgrade-link')) {
            event.preventDefault();
            event.stopPropagation();
            openCapabilitiesModal();
            return;
        }
        if (target.closest('.mo-banner-compare-btn')) {
            event.preventDefault();
            openCompareModal();
            return;
        }
        if (target.closest('#mo-modal-goto-compare')) {
            event.preventDefault();
            openCompareFromCapabilities();
            return;
        }
        if (target.closest('#mo-monitor-view-enhancements')) {
            openMonitorEnhancements(event);
            return;
        }
        if (target.closest('#modal-open-button')) {
            if (window.MoEvaluationScheduler && window.MoEvaluationScheduler.openEvaluationModal) {
                window.MoEvaluationScheduler.openEvaluationModal();
            } else {
                openPremiumModal();
            }
            return;
        }
        if (target.closest('#close-btn')) {
            closePremiumModal();
            return;
        }
        if (target.closest('#mo-premium-continue-free')) {
            closePremiumModal();
            return;
        }
        if (target.closest('#mo-compare-close-btn') || target.closest('#mo-compare-close-footer')) {
            closeCompareModal();
            return;
        }
        if (target.closest('#mo-compare-start-eval') || target.closest('#GetYourPremiumTrialBtn')) {
            showConfirmationPopup();
            return;
        }
        if (target.closest('#confirm-button')) {
            submitConfirmationPopupForm()
                .then(submitInstallForm)
                .catch(function () {
                    /* validation failed */
                });
            return;
        }
        if (target.closest('#cancel-button')
                || target.closest('#confirmation-popup-close-btn')
                || target.closest('#confirmationPopup .close-btn')) {
            closeConfirmationPopup();
        }
    }

    function init() {
        modal = document.getElementById('modal-dialog');
        compareModal = document.getElementById('mo-compare-modal-dialog');
        confirmationPopup = document.getElementById('confirmationPopup');

        bindPremiumGatedControls();
        tryDeepLinkOpen();

        window.addEventListener('click', onBackdropClick);
        document.addEventListener('keydown', onKeyDown);
        document.addEventListener('click', onDocumentClick);
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }
})();
