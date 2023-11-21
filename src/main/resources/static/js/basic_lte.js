
$( document ).ajaxSend(function( event, jqxhr, settings ) {
    jqxhr.setRequestHeader("AJAX", true);
});

$( document ).ajaxError(function( event, jqxhr, settings, thrownError  ) {
    if ( jqxhr != undefined && (jqxhr.status == 401 || jqxhr.status == 403) ) {
        alert("Your session has expired. Please log in again");
        location.href = "/";
    }
});

$(document).ready(function (){
    //form data -> json data
    $.fn.serializeObject = function() {
        "use strict";
        var result = {};
        var extend = function (i, element) {
            var node = result[element.name];
            // If node with same name exists already, need to convert it to an array as it
            // is a multi-value field (i.e., checkboxes)

            var isArrObject = "watchers" == element.name;

            if ('undefined' !== typeof node && node !== null) {
                if ($.isArray(node)) {
                    node.push(element.value);
                } else {
                    result[element.name] = [node, element.value];
                }
            } else {
                if(isArrObject) {
                    node = [element.value];

                    result[element.name] = node;
                } else {
                    result[element.name] = element.value;
                }
            }
        };

        $.each(this.serializeArray(), extend);

        return result;
    }
});

// create tab in frame
function createTabInFrame(id, tabTitle, panelSrc) {
    const existingPanel = findExistingPanelByPanelId(id);

    if (existingPanel) {
        activateTab(id);
        return;
    }

    $('.tab-empty').hide();

    const newTabListItem = $('<li>', {
        class: 'nav-item',
        role: 'presentation',
        append: [
            $('<a>', {
                href: '#',
                class: 'btn-iframe-close',
                'data-widget': 'iframe-close',
                'data-type': 'only-this',
                html: '<i class="fas fa-times"></i>',
            }),
            $('<a>', {
                href: '#panel--'+id,
                class: 'nav-link active',
                'data-toggle': 'row',
                id: 'tab--' + id,
                role: 'tab',
                'aria-controls': 'panel--' + id,
                text: tabTitle,
            })
        ],
    });

    const tabList = $('.iframe-mode .navbar .navbar-nav');
    tabList.append(newTabListItem);

    const newTabPanel = $('<div>', {
        class: 'tab-pane fade active show',
        id: 'panel--' + id,
        role: 'tabpanel',
        'aria-labelledby': 'tab--' + id,
        append: [
            $('<iframe>', {
                src: panelSrc,
                style: 'height: 1066px',
            }),
        ],
    });

    const tabContent = $('.tab-content');
    tabContent.append(newTabPanel);

    activateTab(id);
}

function activateTab(id) {
    const panels = document.querySelectorAll('.tab-pane');
    panels.forEach(panel => {
        panel.classList.remove('active', 'show');
    });

    const activePanel = document.getElementById('panel--' + id);
    activePanel.classList.add('active', 'show');


    const tabs = document.querySelectorAll('.iframe-mode .navbar .navbar-nav .nav-item .nav-link');
    tabs.forEach(tab => {
        tab.classList.remove('active');
    });

    const activeTab = document.getElementById('tab--' + id);
    activeTab.classList.add('active');
}

function findExistingPanelByPanelId(id) {
    return document.querySelector('.tab-pane[aria-labelledby="tab--' + id + '"]') || null;
}

// call create tab in frame
function callCreateTabInFrame(id, tabTitle, panelSrc) {
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
            createTabInFrame(data.tabData[0], data.tabData[1], data.tabData[2]);

            break;
    }
}

// Event handler for adding tags (used for nicknames or watchers).
function addTag(e, elId_01, elId_02, tgId) {
    e.preventDefault();

    const firstWord = $("#" + elId_01).val();
    const lastWord = $("#" + elId_02).val();
    const word = firstWord + " / " + lastWord;

    if (
        !firstWord ||
        !lastWord ||
        firstWord.length === 0 ||
        lastWord.length === 0
    ) {
        return;
    }

    const el = $("<div/>", {
        class: "external-event",
        name: "addTagButton",
        text: word,
    }).append(
        '<i class="fas fa-times float-right mt-1" name="deleteTagButton"></i>'
    );

    $("#" + tgId).prepend(el);

    $("#" + elId_01 + ", #" + elId_02).val("");
}
