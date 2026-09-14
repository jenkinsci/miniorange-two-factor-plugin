(function () {
    'use strict';

    var STORAGE_KEY = 'mo_tfa_monitor_dismissed_at';
    var DISMISS_INTERVAL_MS = 72 * 60 * 60 * 1000;

    function getDismissedAt() {
        try {
            var raw = window.localStorage.getItem(STORAGE_KEY);
            if (!raw) {
                return 0;
            }
            var parsed = parseInt(raw, 10);
            return isNaN(parsed) ? 0 : parsed;
        } catch (e) {
            return 0;
        }
    }

    function setDismissedAt(timestamp) {
        try {
            window.localStorage.setItem(STORAGE_KEY, String(timestamp));
        } catch (e) {
            /* storage blocked — non-blocking */
        }
    }

    function isDismissed() {
        var dismissedAt = getDismissedAt();
        if (dismissedAt === 0) {
            return false;
        }
        return Date.now() - dismissedAt < DISMISS_INTERVAL_MS;
    }

    function hideMonitor() {
        var monitor = document.querySelector('.mo-tfa-security-monitor');
        if (!monitor) {
            return;
        }
        monitor.style.display = 'none';
        var next = monitor.nextElementSibling;
        if (next && next.tagName === 'BR') {
            next.style.display = 'none';
        }
    }

    function dismissMonitor() {
        setDismissedAt(Date.now());
        hideMonitor();
    }

    function init() {
        if (isDismissed()) {
            hideMonitor();
            return;
        }

        var remindBtn = document.getElementById('mo-monitor-remind-later');
        if (remindBtn) {
            remindBtn.addEventListener('click', function (event) {
                event.preventDefault();
                dismissMonitor();
            });
        }
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }
})();
