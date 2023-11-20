function activatePanel(panelId) {
    const panels = document.querySelectorAll('.tab-pane');
    panels.forEach(panel => {
        panel.classList.remove('active', 'show');
    });

    const activeTab = document.getElementById('panel--' + panelId);
    activeTab.classList.add('active', 'show');
}

function activateTab(tabId) {
    const tabs = document.querySelectorAll('.iframe-mode .navbar .navbar-nav .nav-item .nav-link');
    tabs.forEach(tab => {
        tab.classList.remove('active');
    });

    const activeTab = document.getElementById('tab--' + tabId);
    activeTab.classList.add('active');
}

function findExistingPanelByPanelId(panelId) {
    const existingPanels = document.querySelectorAll('.tab-pane');
    for (const existingPanel of existingPanels) {
        const existingPanelId = existingPanel.getAttribute('aria-labelledby');
        if (existingPanelId  === 'panel--' + panelId) {
            return existingPanel;
        }
    }
    return null;
}

function addNewTab(id, tabTitle, panelSrc) {
    console.log("호출");
    console.log(id);
    console.log(tabTitle);
    console.log(tabTitle);
    console.log(panelSrc);

    const existingPanel = findExistingPanelByPanelId(id);

    if (existingPanel) {
        activateTab(id);
        activatePanel(id);
        return;
    }

    const newTabListItem = document.createElement('li');
    newTabListItem.className = 'nav-item';
    newTabListItem.setAttribute('role', 'presentation');

    $('.tab-empty').hide();

    const closeButton = document.createElement('a');
    closeButton.href = '#';
    closeButton.className = 'btn-iframe-close';
    closeButton.setAttribute('data-widget', 'iframe-close');
    closeButton.setAttribute('data-type', 'only-this');
    closeButton.innerHTML = '<i class="fas fa-times"></i>';
    newTabListItem.appendChild(closeButton);

    const newTabLink = document.createElement('a');
    newTabLink.href = '#' + id;
    newTabLink.className = 'nav-link active';
    newTabLink.setAttribute('data-toggle', 'row');
    newTabLink.id = 'tab--' + id;
    newTabLink.setAttribute('role', 'tab');
    newTabLink.setAttribute('aria-controls', id);
    newTabLink.textContent = tabTitle;

    newTabListItem.appendChild(newTabLink);

    const tabList = document.querySelector('.iframe-mode .navbar .navbar-nav');
    tabList.appendChild(newTabListItem);
    activateTab(id);

    const newTabPanel = document.createElement('div');
    newTabPanel.className = 'tab-pane fade active show';
    newTabPanel.id = 'panel--' + id;
    newTabPanel.setAttribute('role', 'tabpanel');
    newTabPanel.setAttribute('aria-labelledby', 'panel--' + id);

    const newPanelIframe = document.createElement('iframe');
    newPanelIframe.src = panelSrc;
    newPanelIframe.style.height = '1066px';

    newTabPanel.appendChild(newPanelIframe);

    const tabContent = document.querySelector('.tab-content');
    tabContent.appendChild(newTabPanel);
    activatePanel(id);
}

function callAddNewTab(id, tabTitle, panelSrc) {
    var tabData = [];
    tabData[0] = id;
    tabData[1] = tabTitle;
    tabData[2] = panelSrc;

    var data = {
        tabData: tabData,
        action:'create'
    };

    parent.postMessage(JSON.stringify(data),"*");
}

if ('addEventListener' in window){
    window.addEventListener('message', receiveMessage, false);
} else if ('attachEvent' in window){ //IE
    window.attachEvent('onmessage', receiveMessage);
}

function receiveMessage(event) {
    var data = JSON.parse(event.data);

    switch(data.action){
        case 'create':
            addNewTab(data.tabData[0], data.tabData[1], data.tabData[2]);

            break;
    }
}