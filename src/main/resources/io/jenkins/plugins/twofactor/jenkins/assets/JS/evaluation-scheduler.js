(function () {
    'use strict';

    var STORAGE_KEY_LAST_AUTO = 'mo_tfa_eval_last_auto_popup';
    var AUTO_POPUP_INTERVAL_MS = 72 * 60 * 60 * 1000;
    var PAGE_OPEN_DELAY_MS = 1000;

    var HEADLINE = 'Harden Jenkins 2FA for production teams';

    var scheduledTimer = null;

    function isConfigPage() {
        return window.location.pathname.indexOf('/tfaGlobalConfig') !== -1;
    }

    function hasDeepLinkOpenRequest() {
        return (window.location.search || '').indexOf('moOpenEvaluation=1') !== -1;
    }

    function getLastAutoPopupAt() {
        try {
            var raw = window.localStorage.getItem(STORAGE_KEY_LAST_AUTO);
            if (!raw) {
                return 0;
            }
            var parsed = parseInt(raw, 10);
            return isNaN(parsed) ? 0 : parsed;
        } catch (e) {
            return 0;
        }
    }

    function setLastAutoPopupAt(timestamp) {
        try {
            window.localStorage.setItem(STORAGE_KEY_LAST_AUTO, String(timestamp));
        } catch (e) {
            /* storage blocked — non-blocking */
        }
    }

    function shouldOfferAutoPopup() {
        var lastShown = getLastAutoPopupAt();
        if (lastShown === 0) {
            return true;
        }
        return Date.now() - lastShown >= AUTO_POPUP_INTERVAL_MS;
    }

    function setModalHeadline() {
        var titleEl = document.getElementById('mo-premium-modal-title');
        if (!titleEl) {
            return;
        }
        titleEl.textContent = HEADLINE;
    }

    function recordAutoPopupShown() {
        setLastAutoPopupAt(Date.now());
    }

    function openEvaluationModal() {
        setModalHeadline();
        if (window.MoEvaluationModal && window.MoEvaluationModal.open) {
            window.MoEvaluationModal.open();
        }
    }

    function scheduleAutoPopup() {
        recordAutoPopupShown();
        scheduledTimer = window.setTimeout(function () {
            scheduledTimer = null;
            openEvaluationModal();
        }, PAGE_OPEN_DELAY_MS);
    }

    function cancelScheduled() {
        if (scheduledTimer) {
            window.clearTimeout(scheduledTimer);
            scheduledTimer = null;
        }
    }

    function init() {
        if (!isConfigPage() || !shouldOfferAutoPopup() || hasDeepLinkOpenRequest()) {
            return;
        }
        scheduleAutoPopup();
    }

    window.MoEvaluationScheduler = {
        cancelScheduled: cancelScheduled,
        openEvaluationModal: function () {
            cancelScheduled();
            openEvaluationModal();
        }
    };

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }
})();
