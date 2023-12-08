/**
 * Ajax Utility
 * handle different types of AJAX requests (GET/POST)
 * */
function fnBasicAjaxData(data, url) {
    return $.ajax({	type: 'GET', url:url, data:data, headers: {'Content-Type': 'application/json'}});
}

function getAjaxJsonData(data, url, dataType, successCallback, errorCallback, completeCallback) {
    return $.ajax({
        type: 'GET',
        url: url,
        data: data,
        headers: {
            'Content-Type': 'application/json'
        },
        dataType: dataType,
        success: function (data, status, xhr) {
            if (successCallback && typeof successCallback === 'function') {
                successCallback(data, status, xhr);
            }
        },
        error: function (xhr, status, error) {
            if (errorCallback && typeof errorCallback === 'function') {
                errorCallback(xhr, status, error);
            }
        },
        complete: function (xhr, status, error) {
            if (completeCallback && typeof completeCallback === 'function') {
                completeCallback(xhr, status, error);
            }
        }
    });
}

function postAjaxJsonData(data, url, dataType, successCallback, errorCallback, completeCallback) {
    return $.ajax({
        type: 'POST',
        url: url,
        data: data,
        cache : false,
        headers: {
            'Content-Type': 'application/json'
        },
        dataType: dataType,
        success: function (data, status, xhr) {
            if (successCallback && typeof successCallback === 'function') {
                successCallback(data, status, xhr);
            }
        },
        error: function (xhr, status, error) {
            if (errorCallback && typeof errorCallback === 'function') {
                errorCallback(xhr, status, error);
            }
        },
        complete: function (xhr, status, error) {
            if (completeCallback && typeof completeCallback === 'function') {
                completeCallback(xhr, status, error);
            }
        }
    });
}

function getAjaxData(data, url, dataType, successCallback, errorCallback, completeCallback) {
    return $.ajax({
        type: 'GET',
        url: url,
        data: data,
        dataType: dataType,
        cache : false,
        success: function (data, status, xhr) {
            if (successCallback && typeof successCallback === 'function') {
                successCallback(data, status, xhr);
            }
        },
        error: function (xhr, status, error) {
            if (errorCallback && typeof errorCallback === 'function') {
                errorCallback(xhr, status, error);
            }
        },
        complete: function (xhr, status, error) {
            if (completeCallback && typeof completeCallback === 'function') {
                completeCallback(xhr, status, error);
            }
        }
    });
}

function postAjaxData(data, url, dataType, successCallback, errorCallback, completeCallback) {
    return $.ajax({
        type: 'POST',
        url: url,
        data: data,
        dataType: dataType,
        cache : false,
        success: function (data, status, xhr) {
            if (successCallback && typeof successCallback === 'function') {
                successCallback(data, status, xhr);
            }
        },
        error: function (xhr, status, error) {
            if (errorCallback && typeof errorCallback === 'function') {
                errorCallback(xhr, status, error);
            }
        },
        complete: function (xhr, status, error) {
            if (completeCallback && typeof completeCallback === 'function') {
                completeCallback(xhr, status, error);
            }
        }
    });
}

function formAjaxData(formId, url, successCallback, errorCallback) {
    $(formId).ajaxForm({
        url: url,
        type: 'POST',
        dataType: "json",
        cache: false,
        success: successCallback,
        error: errorCallback
    }).submit();
}

/**
 * Tab Management Utility
 * */
const SELECTOR_CONTENT_WRAPPER = '.content-wrapper'
const SELECTOR_TAB_CONTENT = `${SELECTOR_CONTENT_WRAPPER}.iframe-mode .tab-content`
const SELECTOR_TAB_LOADING = `${SELECTOR_TAB_CONTENT} .tab-loading`
const SELECTOR_TAB_NAVBAR_NAV = `${SELECTOR_CONTENT_WRAPPER}.iframe-mode .navbar-nav`
const SELECTOR_TAB_NAVBAR_NAV_LINK = `${SELECTOR_TAB_NAVBAR_NAV} .nav-link`
const SELECTOR_TAB_PANE = `${SELECTOR_TAB_CONTENT} .tab-pane`

function createTab_new(title, link,  uniqueName, autoOpen) {
    const existingPanel = document.querySelector('.tab-pane[aria-labelledby="tab--' + uniqueName + '"]') || null;
    if (existingPanel) {
        activateTab(uniqueName);
        return;
    }

    let tabId = `panel--${uniqueName}`
    let navId = `tab--${uniqueName}`

    const newTabListItem = `<li class="nav-item" role="presentation"><a href="#" class="btn-iframe-close" data-widget="iframe-close" data-type="only-this"><i class="fas fa-times"></i></a><a class="nav-link" data-toggle="row" id="${navId}" href="#${tabId}" role="tab" aria-controls="${tabId}" aria-selected="false">${title}</a></li>`
    $(SELECTOR_TAB_NAVBAR_NAV).append(unescape(escape(newTabListItem)))

    const newTabItem = `<div class="tab-pane fade" id="${tabId}" role="tabpanel" aria-labelledby="${navId}"><iframe src="${link}"></iframe></div>`
    $(SELECTOR_TAB_CONTENT).append(unescape(escape(newTabItem)))
    triggerWindowResize();

    if (autoOpen) {
        const $loadingScreen = $(SELECTOR_TAB_LOADING)
        $loadingScreen.fadeIn()
        $(`${tabId} iframe`).ready(() => {
            activateTab(uniqueName);
            setTimeout(() => {
                $loadingScreen.fadeOut()
            })
        })
    }
}

function deleteTab_new (uniqueName) {
    $("#tab--" + uniqueName).parent().find('.btn-iframe-close').trigger("click");
}

function reloadTab_new(link, uniqueName, act) {
    $('iframe').each(function(){
        var url = $(this).attr('src');

        if(url.indexOf("?") != -1){
            var tmp = url.split('?');
            url = tmp[0];
        }

        console.log(url)
        console.log(link)

        if(url == link){
            if(act === "create" || act === "reload"){
                $(this).attr('src', url);
            }
        }
        console.log("her")
        activateTab(uniqueName);
    });
}

function activateTab(uniqueName) {
    let tabId = `panel--${uniqueName}`
    let navId = `tab--${uniqueName}`

    const tabs = document.querySelectorAll(SELECTOR_TAB_PANE);
    tabs.forEach(tab => {
        tab.classList.remove('active', 'show');
    });

    const activeTab = document.getElementById(tabId);
    activeTab.classList.add('active', 'show');


    const navs = document.querySelectorAll(SELECTOR_TAB_NAVBAR_NAV_LINK);
    navs.forEach(nav => {
        nav.classList.remove('active');
    });

    const activeNav = document.getElementById(navId);
    activeNav.classList.add('active');
}

function triggerWindowResize() {
    $(window).trigger('resize');
}

// call create tab in frame
function callCreateTabInFrame(title, link, uniqueName, autoOpen) {
    var tabData = [];
    tabData[0] = title;
    tabData[1] = link;
    tabData[2] = uniqueName;
    tabData[3] = autoOpen;

    var data = {
        tabData: tabData,
        action:'create_new'
    };

    parent.postMessage(JSON.stringify(data),"*");
}

var callReloadTabInframe = function(link, uniqueName){
    var tabData = [];
    tabData[0] = link;
    tabData[1] = uniqueName;

    var data = {
        tabData:tabData,
        action:'reload_new'
    };

    parent.postMessage(JSON.stringify(data),"*");
}

var callDeleteTabInFrame = function(uniqueName){
    var tabData = [];
    tabData[0] = uniqueName;

    var data = {
        tabData:tabData,
        action:'delete_new'
    };

    parent.postMessage(JSON.stringify(data),"*");
}

/**
 * Jqgrid resizing Utility & Options
 * */
$(window).load(function () {
    resizingJqGidSet();
    resizingInnerJqGidSet();
    resizingOuterJqGidSet();
    /* 브라우저 창 크기에 따라 jqGrid Width 자동 조절 */
    $(window)
        .bind("resize", function () {
            resizingJqGidSet();
            resizingInnerJqGidSet();
            resizingOuterJqGidSet();
        })
        .trigger("resize");
});

function resizingJqGidSet() {
    if ($(".jqGridSet table").length > 0) {
        $(".jqGridSet table").jqGrid("setGridWidth", 0, true);
        $(".jqGridSet table").jqGrid("setGridWidth", $(".jqGridSet").width(), true);
    }
}

function resizingInnerJqGidSet() {
    if ($(".innerJqGridSet table").length > 0) {
        $(".innerJqGridSet table").jqGrid("setGridWidth", 0, true);
        $(".innerJqGridSet table").jqGrid(
            "setGridWidth",
            $(window).innerWidth() - 380,
            true
        );
    }
}

function resizingOuterJqGidSet() {
    if ($(".outerJqGridSet table").length > 0) {
        $(".outerJqGridSet table").jqGrid("setGridWidth", 0, true);
        $(".outerJqGridSet table").jqGrid(
            "setGridWidth",
            $(window).innerWidth() - 50,
            true
        );
    }
}

function displayUrl(cellvalue) {
    var icon1 =
        '<a href="https://opensource.org/licenses/BSD-2-Clause" class="urlLink" target="_blank">https://opensource.org/licenses/BSD-2-Clause</a>';
    return icon1;
}

function displayButtons(cellvalue, options, rowObject) {
    var deleted = "<input type='button' value='delete' class='btn-secondary' />";
    return deleted;
}

function escapeHtml(str) {
    var map = {
        "&": "&amp;",
        "<": "&lt;",
        ">": "&gt;",
        '"': "&quot;",
        "'": "&#039;",
    };

    return str.replace(/[&<>"']/g, function (m) {
        return map[m];
    });
}

/**
 * UI action Utility
 * */
function appendMultiTag(e, elId_01, elId_02, tgId) {
    e.preventDefault();

    const fword = $("#" + elId_01).val();
    const lword = $("#" + elId_02).val();
    const word = fword + " / " + lword;

    if (!fword || !lword || fword.length === 0 || lword.length === 0) {
        return;
    }

    const el = $("<div/>", {
        class: "external-event",
        text: word,
    }).append(
        '<i class="fas fa-times float-right mt-1" onclick="deleteTag()"></i>'
    );

    $("#" + tgId).prepend(el);
    $("#" + elId_01 + ", #" + elId_02)
        .val(null)
        .trigger("change");
}

function appendSingleTag(event, id) {
    event.preventDefault();

    const inputElement = $("#input_" + id);
    const word = inputElement.val().trim();

    if (!word) {
        return;
    }

    const gridDiv = $("<div/>", {
        class: "col-4 tag-container mb-1 pr-0"
    });

    const tagContainer = $("<div/>", {
        class: "external-event",
        name: id
    });

    const hiddenInput = $('<input>', {
        type: 'hidden',
        name: id,
        value: word
    });

    const deleteIcon = $('<i>', {
        class: 'fas fa-times text-blue-gray float-right mt-1',
    }).click(function() {
        deleteTag(this);
    });

    const content = $("<span/>", { text: word });

    tagContainer.append(hiddenInput, deleteIcon, content);
    gridDiv.append(tagContainer);
    $("#appendArea_" + id).prepend(gridDiv);
    inputElement.val(null).trigger("change");
}

function deleteTag (el) {
    $(el).closest(".tag-container").remove();
}

/**
 * Cookie action Utility
 * */
function getCookie(cookieName) {
    cookieName = cookieName + '=';
    let cookieData = document.cookie;
    let start = cookieData.indexOf(cookieName);
    let cookieValue = '';

    if(start != -1){
        start += cookieName.length;

        let end = cookieData.indexOf(';', start);
        if(end == -1) {
            end = cookieData.length;
        }



        cookieValue = cookieData.substring(start, end);
        console.log(cookieValue);
    }

    return unescape(cookieValue);
}

function setCookie(cookieName, value, exdays){
    var exdate = new Date();
    exdate.setDate(exdate.getDate() + exdays);

    var cookieValue = escape(value) + ((exdays==null) ? "" : "; expires=" + exdate.toGMTString());
    document.cookie = cookieName + "=" + cookieValue;
}

function deleteCookie(cookieName){
    var expireDate = new Date();
    expireDate.setDate(expireDate.getDate() - 1);

    document.cookie = cookieName + "= " + "; expires=" + expireDate.toGMTString();
}