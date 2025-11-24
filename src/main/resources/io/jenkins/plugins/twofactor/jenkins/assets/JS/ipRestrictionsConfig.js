document.addEventListener('DOMContentLoaded', function() {
    const tabs = document.querySelectorAll('.tab');
    const contents = document.querySelectorAll('.content');

    tabs.forEach(tab => {
        tab.addEventListener('click', function() {
            const tabId = this.getAttribute('data-tab');

            tabs.forEach(t => t.classList.remove('active'));
            contents.forEach(c => c.classList.remove('active'));

            this.classList.add('active');
            document.getElementById(`content-${tabId}`).classList.add('active');
        });
    });
});

document.addEventListener('DOMContentLoaded', function() {
    const tabs = document.querySelectorAll('.tab');
    const contents = document.querySelectorAll('.content');

    function setActiveTab(tabId) {
        tabs.forEach(t => t.classList.remove('active'));
        contents.forEach(c => c.classList.remove('active'));

        const tab = document.querySelector(`.tab[data-tab="${tabId}"]`);
        const content = document.getElementById(`content-${tabId}`);
        if (tab && content) {
            tab.classList.add('active');
            content.classList.add('active');
            localStorage.setItem('activeTab', tabId);
        }
    }

    const savedTab = localStorage.getItem('activeTab') || 'whitelist';
    setActiveTab(savedTab);

    tabs.forEach(tab => {
        tab.addEventListener('click', function() {
            const tabId = this.getAttribute('data-tab');
            setActiveTab(tabId);
        });
    });
});

function reloadPageAfterDelay() {
    setTimeout(function() {
        window.location.reload();
    }, 1000);
}
