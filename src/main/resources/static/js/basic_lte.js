const LINKREGEXP = /PRJ-\d+(?!.*\<\/a\>)|3rd-\d+(?!.*\<\/a\>)/gi;

$( document ).ajaxSend(function( event, jqxhr, settings ) {
    jqxhr.setRequestHeader("AJAX", true);
});

$( document ).ajaxError(function( event, jqxhr, settings, thrownError  ) {
    if ( jqxhr != undefined && (jqxhr.status == 401 || jqxhr.status == 403) ) {
        alert("Your session has expired. Please log in again");
        location.href = "/";
    }
});

if ('addEventListener' in window){
    window.addEventListener('message', receiveMessage, false);
} else if ('attachEvent' in window){ //IE
    window.attachEvent('onmessage', receiveMessage);
}

function receiveMessage(event) {
    var data = JSON.parse(event.data);

    switch(data.action){
        case 'create':
            createTab(data.tabData[0], data.tabData[1], data.tabData[2], data.tabData[2]);

            break;
        case 'delete':
            deleteTab(data.tabData[0]);

            break;
        case 'reload':
            reloadTab(data.tabData[0], data.tabData[1], data.action);

            break;
    }
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
        action:'create'
    };

    parent.postMessage(JSON.stringify(data),"*");
}

var callReloadTabInframe = function(link, uniqueName){
    var tabData = [];
    tabData[0] = link;
    tabData[1] = uniqueName;

    var data = {
        tabData:tabData,
        action:'reload'
    };

    parent.postMessage(JSON.stringify(data),"*");
}

var callDeleteTabInFrame = function(uniqueName){
    var tabData = [];
    tabData[0] = uniqueName;

    var data = {
        tabData:tabData,
        action:'delete'
    };

    parent.postMessage(JSON.stringify(data),"*");
}

const SELECTOR_CONTENT_WRAPPER = '.content-wrapper'
const SELECTOR_TAB_CONTENT = `${SELECTOR_CONTENT_WRAPPER}.iframe-mode .tab-content`
const SELECTOR_TAB_LOADING = `${SELECTOR_TAB_CONTENT} .tab-loading`
const SELECTOR_TAB_NAVBAR_NAV = `${SELECTOR_CONTENT_WRAPPER}.iframe-mode .navbar-nav`
const SELECTOR_TAB_NAVBAR_NAV_LINK = `${SELECTOR_TAB_NAVBAR_NAV} .nav-link`
const SELECTOR_TAB_PANE = `${SELECTOR_TAB_CONTENT} .tab-pane`



function createTab(title, link,  uniqueName, autoOpen) {
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

function deleteTab (uniqueName) {
    $("#tab--" + uniqueName).parent().find('.btn-iframe-close').trigger("click");
}

function reloadTab(link, uniqueName, act) {
    $('iframe').each(function(){
        var url = $(this).attr('src');

        if(url.indexOf("?") != -1){
            var tmp = url.split('?');
            url = tmp[0];
        }

        if(url == link){
            if(act === "create" || act === "reload"){
                $(this).attr('src', url);
            }
        }
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

const loading = {
    show: function(){
        if($('#loading_wrap').css("display") == "none" && !onAjaxLoadingHide){
            $('#loading_wrap').show();
        }
    },
    hide: function(){
        if("Y" != doNotUseAutoLoadingHideFlag) {
            $('#loading_wrap').hide();
        }
    }
}

var doNotUseAutoLoadingHideFlag = 'N';

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

    autoComplete.load();
    autoComplete.init();
});
// Auto complete
const commonAjax = {
    getLicenseTags : function(data){
        return fnBasicAjaxData(data, "/license/autoCompleteAjax");
    },
    getOssTags : function(data){
        return fnBasicAjaxData(data, "/oss/autoCompleteAjax");
    },
    //project
    getProjectNameTags : function(data){
        return fnBasicAjaxData(data, "/project/autoCompleteAjax");
    },
    getProjectNameConfTags : function(data){
        return fnBasicAjaxData(data, "/project/autoCompleteAjax?identificationStatus=CONF");
    },
    getProjectIdConfTags : function(data){
        return fnBasicAjaxData(data, "/project/autoCompleteIdAjax");
    },
    getProjectVersionTags : function(data){
        return fnBasicAjaxData(data, "/project/autoCompleteVersionAjax");
    },
    getProjectModelTags : function(data){
        return fnBasicAjaxData(data, "/project/autoCompleteModelAjax");
    },
    //partner
    getPartnerNmTags : function(data){
        return fnBasicAjaxData(data, "/partner/autoCompleteNmAjax");
    },
    //partner confirmed
    getPartnerConfNmTags : function(data){
        return fnBasicAjaxData(data, "/partner/autoCompleteConfNmAjax");
    },
    getPartnerConfIdTags : function(data){
        return fnBasicAjaxData(data, "/partner/autoCompleteConfIdAjax");
    },
    getPartnerSwNmTags : function(data){
        return fnBasicAjaxData(data, "/partner/autoCompleteSwNmAjax");
    },
    getPartnerConfSwNmTags : function(data){
        return fnBasicAjaxData(data, "/partner/autoCompleteConfSwNmAjax");
    },
    getPartnerSwVerTags : function(data){
        return fnBasicAjaxData(data, "/partner/autoCompleteSwVerAjax");
    },
    getPartnerConfSwVerTags : function(data){
        return fnBasicAjaxData(data, "/partner/autoCompleteConfSwVerAjax");
    },
    getPartnerModifierTags : function(data){
        return fnBasicAjaxData(data, "/partner/autoCompleteModifierAjax");
    },
    //binary
    getBatNmTags : function(data){
        return fnBasicAjaxData(data, "/bat/autoCompleteNmAjax");
    },
    getBatConfNmTags : function(data){
        return fnBasicAjaxData(data, "/bat/autoCompleteConfNmAjax");
    },
    getBatSwNmTags : function(data){
        return fnBasicAjaxData(data, "/bat/autoCompleteSwNmAjax");
    },
    getBatSwNmConfTags : function(data){
        return fnBasicAjaxData(data, "/bat/autoCompleteSwNmAjax?batStatus=60");
    },
    getBatSwVerTags : function(data){
        return fnBasicAjaxData(data, "/bat/autoCompleteSwVerAjax");
    },
    getBatDivisionTags : function(data){
        return fnBasicAjaxData(data, "/bat/autoCompleteDivisionAjax");
    },
    //code
    getCodeNoTags : function(data){
        return fnBasicAjaxData(data, "/system/code/autoCompleteNoAjax");
    },
    getCodeNmTags : function(data){
        return fnBasicAjaxData(data, "/system/code/autoCompleteNmAjax");
    },
    //Creator
    getCreatorTags : function(data){
        return fnBasicAjaxData(data, "/system/user/autoCompleteCreatorAjax");
    },
    getReviewerTags : function(data){
        return fnBasicAjaxData(data, "/system/user/autoCompleteReviewerAjax");
    },
    //Creator & Division
    getCreatorDivisionTags : function(data){
        return fnBasicAjaxData(data, "/system/user/autoCompleteCreatorDivisionAjax");
    }
};

function fnBasicAjaxData(data, url) {
    return $.ajax({	type: 'GET',url:url,data:data,headers: {'Content-Type': 'application/json'}});
}

const autoComplete = {
    licenseTags:[],
    licenseLongTags:[],
    ossTags:[],
    //project
    projectNameTags:[], projectNameConfTags:[], projectIdConfTags:[], projectVersionTags:[],projectModelTags:[],
    //partner
    partyNameTags:[], partyConfNameTags:[], partyConfIdTags:[], softwareNameTags:[], softwareConfNameTags:[],softwareVersionTags:[], softwareConfVersionTags:[],
    //binary
    binaryNameTags:[],binaryConfNameTags:[],binarySwNameTags:[],binarySwNameConfTags:[],binarySwVersionTags:[],binaryDivisionTags:[],
    //code
    codeNoTags:[],codeNmTags:[],
    //user
    creatorTag:[],reviewerTag:['N/A'],creatorDivisionTag:[],
    load : function(){
        if($(".autoComLicense").length > 0) {
            commonAjax.getLicenseTags().success(function(data, status, headers, config){
                if(data != null){
                    var tag = "";
                    data.forEach(function(obj){
                        if(obj!=null) {
                            tag ={
                                value : obj.shortIdentifier.length > 0 ? obj.shortIdentifier : obj.licenseName,
                                label : obj.licenseName + (obj.shortIdentifier.length > 0 ? (" (" + obj.shortIdentifier + ")") : ""),
                                type : obj.licenseType,
                                obligation : obj.obligation,
                                obligationChecks : obj.obligationChecks,
                                obligationCode : obj.obligationCode,
                                licenseTypeVal : obj.licenseTypeVal,
                                restriction : obj.restriction
                            }
                            autoComplete.licenseTags.push(tag);
                        }
                    });
                }
            });
        }

        if($(".autoComLicenseLong").length > 0) {
            commonAjax.getLicenseTags().success(function(data, status, headers, config){
                if(data != null){
                    var tag = "";
                    data.forEach(function(obj){
                        if(obj!=null) {
                            tag = {
                                value : obj.licenseName,
                                label : obj.licenseName + (obj.shortIdentifier.length > 0 ? (" (" + obj.shortIdentifier + ")") : ""),
                                type : obj.licenseType,
                                obligation : obj.obligation,
                                obligationChecks : obj.obligationChecks
                            }

                            autoComplete.licenseLongTags.push(tag);
                        }
                    });
                }
            });
        }

        if($(".autoComOss").length > 0) {
            commonAjax.getOssTags().success(function(data, status, headers, config){
                if(data != null){
                    data.forEach(function(obj){
                        if(obj!=null) {
                            autoComplete.ossTags.push(obj.ossName);
                        }
                    })
                }
            });
        }

        if($('.autoComProjectNm').length > 0) {
            commonAjax.getProjectNameTags().success(function(data, status, headers, config){
                if(data != null){
                    data.forEach(function(obj){
                        if(obj!=null) {
                            autoComplete.projectNameTags.push(xssPreventerUnescape(obj.prjName));
                        }
                    })
                }
            });
        }

        if($('.autoComProjectNmConf').length > 0) {
            commonAjax.getProjectNameConfTags().success(function(data, status, headers, config){
                if(data != null){
                    data.forEach(function(obj){
                        if(obj!=null) {
                            autoComplete.projectNameConfTags.push(obj.prjName);
                        }
                    })
                }
            });
        }

        if($('.autoComProjectVersion').length > 0) {
            commonAjax.getProjectVersionTags().success(function(data, status, headers, config){
                if(data != null){
                    data.forEach(function(obj){
                        if(obj!=null) {
                            autoComplete.projectVersionTags.push(obj.prjVersion);
                        }
                    })
                }
            });
        }

        if($('.autoComProjectIdConf').length > 0) {
            commonAjax.getProjectIdConfTags().success(function(data, status, headers, config){
                if(data != null){
                    data.forEach(function(obj){
                        if(obj!=null) {
                            autoComplete.projectIdConfTags.push(obj.prjId);
                        }
                    })
                }
            });
        }

        if($('.autoComProjectModel').length > 0) {
            commonAjax.getProjectModelTags().success(function(data, status, headers, config){
                if(data != null){
                    data.forEach(function(obj){
                        if(obj!=null) {
                            autoComplete.projectModelTags.push(obj.modelName);
                        }
                    })
                }
            });
        }

        if($('.autoComParty').length > 0) {
            commonAjax.getPartnerNmTags().success(function(data, status, headers, config){
                if(data != null){
                    data.forEach(function(obj){
                        if(obj!=null) {
                            autoComplete.partyNameTags.push(obj.partnerName);
                        }
                    })
                }
            });
        }
        if($('.autoComConfParty').length > 0) {
            commonAjax.getPartnerConfNmTags().success(function(data, status, headers, config){
                if(data != null){
                    data.forEach(function(obj){
                        if(obj!=null) {
                            autoComplete.partyConfNameTags.push(obj.partnerName);
                        }
                    })
                }
            });
        }
        if($('.autoComConfPartyId').length > 0) {
            commonAjax.getPartnerConfIdTags().success(function(data, status, headers, config){
                if(data != null){
                    data.forEach(function(obj){
                        if(obj!=null) {
                            autoComplete.partyConfIdTags.push(obj.partnerId);
                        }
                    })
                }
            });
        }
        if($('.autoComSwNm').length > 0) {
            commonAjax.getPartnerSwNmTags().success(function(data, status, headers, config){
                if(data != null){
                    data.forEach(function(obj){
                        if(obj!=null) {
                            autoComplete.softwareNameTags.push(obj.softwareName);
                        }
                    })
                }
            });
        }
        if($(".autoComConfSwNm").length > 0){
            commonAjax.getPartnerConfSwNmTags().success(function(data, status, headers, config){
                if(data != null){
                    data.forEach(function(obj){
                        if(obj!=null) {
                            autoComplete.softwareConfNameTags.push(obj.softwareName);
                        }
                    })
                }
            });
        }
        if($('.autoComSwVer').length > 0) {
            commonAjax.getPartnerSwVerTags().success(function(data, status, headers, config){
                if(data != null){
                    data.forEach(function(obj){
                        if(obj!=null) {
                            autoComplete.softwareVersionTags.push(obj.softwareVersion);
                        }
                    })
                }
            });
        }
        if($('.autoComConfSwVer').length > 0) {
            commonAjax.getPartnerConfSwVerTags().success(function(data, status, headers, config){
                if(data != null){
                    data.forEach(function(obj){
                        if(obj!=null) {
                            autoComplete.softwareConfVersionTags.push(obj.softwareVersion);
                        }
                    })
                }
            });
        }
        if($('.autoComBinary').length > 0) {
            commonAjax.getBatNmTags().success(function(data, status, headers, config){
                if(data != null){
                    data.forEach(function(obj){
                        if(obj!=null) {
                            autoComplete.binaryNameTags.push(obj.fileName);
                        }
                    })
                }
            });
        }
        if($('.autoComConfBinary').length > 0) {
            commonAjax.getBatConfNmTags().success(function(data, status, headers, config){
                if(data != null){
                    data.forEach(function(obj){
                        if(obj!=null) {
                            autoComplete.binaryConfNameTags.push(obj.fileName);
                        }
                    })
                }
            });
        }
        if($('.autoComBinarySwName').length > 0) {
            commonAjax.getBatSwNmTags().success(function(data, status, headers, config){
                if(data != null){
                    data.forEach(function(obj){
                        if(obj!=null) {
                            autoComplete.binarySwNameTags.push(obj.softwareName);
                        }
                    })
                }
            });
        }
        if($('.autoComBinarySwNameConf').length > 0) {
            commonAjax.getBatSwNmConfTags().success(function(data, status, headers, config){
                if(data != null){
                    data.forEach(function(obj){
                        if(obj!=null) {
                            autoComplete.binarySwNameConfTags.push(obj.softwareName);
                        }
                    })
                }
            });
        }
        if($('.autoComBinarySwVersion').length > 0) {
            commonAjax.getBatSwVerTags().success(function(data, status, headers, config){
                if(data != null){
                    data.forEach(function(obj){
                        if(obj!=null) {
                            autoComplete.binarySwVersionTags.push(obj.softwareVersion);
                        }
                    })
                }
            });
        }
        if($('.autoComBinaryDivision').length > 0) {
            commonAjax.getBatDivisionTags().success(function(data, status, headers, config){
                if(data != null){
                    data.forEach(function(obj){
                        if(obj!=null) {
                            autoComplete.binaryDivisionTags.push(obj.division);
                        }
                    })
                }
            });
        }
        if($('.autoComCodeNo').length > 0) {
            commonAjax.getCodeNoTags().success(function(data, status, headers, config){
                if(data != null){
                    data.forEach(function(obj){
                        if(obj!=null) {
                            autoComplete.codeNoTags.push(obj.cdNo);
                        }
                    })
                }
            });
        }
        if($('.autoComCodeNm').length > 0) {
            commonAjax.getCodeNmTags().success(function(data, status, headers, config){
                if(data != null){
                    data.forEach(function(obj){
                        if(obj!=null) {
                            autoComplete.codeNmTags.push(obj.cdNm);
                        }
                    })
                }
            });
        }
        //user
        if($('.autoComCreator').length > 0) {
            commonAjax.getCreatorTags().success(function(data, status, headers, config){
                if(data != null){
                    data.forEach(function(obj){
                        if(obj!=null) {
                            autoComplete.creatorTag.push(obj.userName);
                        }
                    })
                }
            });
        }
        if($('.autoComReviewer').length > 0) {
            commonAjax.getReviewerTags().success(function(data, status, headers, config){
                if(data != null){
                    data.forEach(function(obj){
                        if(obj!=null) {
                            autoComplete.reviewerTag.push(obj.userName);
                        }
                    })
                }
            });
        }
        if($(".autoComCreatorDivision").length > 0) {
            commonAjax.getCreatorDivisionTags().success(function(data, status, headers, config){
                if(data != null){
                    var tag = "";
                    data.forEach(function(obj){
                        if(obj!=null) {
                            tag = {
                                value : obj.userName,
                                label : obj.userName,
                                division : obj.division,
                                id : obj.userId
                            }

                            autoComplete.creatorDivisionTag.push(tag);
                        }
                    });
                }
            });
        }
    },
    init : function(){
        $(".autoComLicense").autocomplete({source: autoComplete.licenseTags, minLength: 0, //delay: 500,
            open: function() { $(this).attr('state', 'open'); },close: function () { $(this).attr('state', 'closed'); }
        })
            .focus(function() {if ($(this).attr('state') != 'open') {$(this).autocomplete("search");}})
            .autocomplete( "instance" )._renderItem = function( ul, item ) {
            return $( "<li>" ).append( "<div>" + item.label + "<strong> (" + item.type + ") </strong>" + item.obligation + item.restriction + "</div>" ).appendTo( ul );
        };

        //hklee 2016 11 14
        $(".autoComLicenseLong").autocomplete({source: autoComplete.licenseLongTags, minLength: 0, //delay: 500,
            open: function() { $(this).attr('state', 'open'); },close: function () { $(this).attr('state', 'closed'); }
        })
            .focus(function() {if ($(this).attr('state') != 'open') {$(this).autocomplete("search");}})
            .autocomplete( "instance" )._renderItem = function( ul, item ) {
            return $( "<li>" ).append( "<div>" + item.label + "<strong> (" + item.type + ") </strong>" + item.obligation + "</div>" ).appendTo( ul );
        };

        $(".autoComOss").autocomplete({source: autoComplete.ossTags, minLength: 3,open: function() { $(this).attr('state', 'open'); },close: function () { $(this).attr('state', 'closed'); }})
            .focus(function() {if ($(this).attr('state') != 'open') {$(this).autocomplete("search");}});

        $(".autoComProjectNm").autocomplete({source: autoComplete.projectNameTags, minLength: 0,open: function() { $(this).attr('state', 'open'); },close: function () { $(this).attr('state', 'closed'); }})
            .focus(function() {if ($(this).attr('state') != 'open') {$(this).autocomplete("search");}});

        $(".autoComProjectNmConf").autocomplete({source: autoComplete.projectNameConfTags, minLength: 0,open: function() { $(this).attr('state', 'open'); },close: function () { $(this).attr('state', 'closed'); }})
            .focus(function() {if ($(this).attr('state') != 'open') {$(this).autocomplete("search");}});

        $(".autoComProjectIdConf").autocomplete({source: autoComplete.projectIdConfTags, minLength: 0,open: function() { $(this).attr('state', 'open'); },close: function () { $(this).attr('state', 'closed'); }})
            .focus(function() {if ($(this).attr('state') != 'open') {$(this).autocomplete("search");}});

        $(".autoComProjectVersion").autocomplete({source: autoComplete.projectVersionTags, minLength: 0,open: function() { $(this).attr('state', 'open'); },close: function () { $(this).attr('state', 'closed'); }})
            .focus(function() {if ($(this).attr('state') != 'open') {$(this).autocomplete("search");}});

        $(".autoComProjectModel").autocomplete({source: autoComplete.projectModelTags, minLength: 3,open: function() { $(this).attr('state', 'open'); },close: function () { $(this).attr('state', 'closed'); }})
            .focus(function() {if ($(this).attr('state') != 'open') {$(this).autocomplete("search");}});

        $(".autoComParty").autocomplete({source: autoComplete.partyNameTags, minLength: 0,open: function() { $(this).attr('state', 'open'); },close: function () { $(this).attr('state', 'closed'); }})
            .focus(function() {if ($(this).attr('state') != 'open') {$(this).autocomplete("search");}});

        $(".autoComConfParty").autocomplete({source: autoComplete.partyConfNameTags, minLength: 0,open: function() { $(this).attr('state', 'open'); },close: function () { $(this).attr('state', 'closed'); }})
            .focus(function() {if ($(this).attr('state') != 'open') {$(this).autocomplete("search");}});

        $(".autoComConfPartyId").autocomplete({source: autoComplete.partyConfIdTags, minLength: 0,open: function() { $(this).attr('state', 'open'); },close: function () { $(this).attr('state', 'closed'); }})
            .focus(function() {if ($(this).attr('state') != 'open') {$(this).autocomplete("search");}});

        $(".autoComSwNm").autocomplete({source: autoComplete.softwareNameTags, minLength: 0,open: function() { $(this).attr('state', 'open'); },close: function () { $(this).attr('state', 'closed'); }})
            .focus(function() {if ($(this).attr('state') != 'open') {$(this).autocomplete("search");}});

        $(".autoComConfSwNm").autocomplete({source: autoComplete.softwareConfNameTags, minLength: 0,open: function() { $(this).attr('state', 'open'); },close: function () { $(this).attr('state', 'closed'); }})
            .focus(function() {if ($(this).attr('state') != 'open') {$(this).autocomplete("search");}});

        $(".autoComSwVer").autocomplete({source: autoComplete.softwareVersionTags, minLength: 0,open: function() { $(this).attr('state', 'open'); },close: function () { $(this).attr('state', 'closed'); }})
            .focus(function() {if ($(this).attr('state') != 'open') {$(this).autocomplete("search");}});

        $(".autoComConfSwVer").autocomplete({source: autoComplete.softwareConfVersionTags, minLength: 0,open: function() { $(this).attr('state', 'open'); },close: function () { $(this).attr('state', 'closed'); }})
            .focus(function() {if ($(this).attr('state') != 'open') {$(this).autocomplete("search");}});

        $(".autoComBinary").autocomplete({source: autoComplete.binaryNameTags, minLength: 0,open: function() { $(this).attr('state', 'open'); },close: function () { $(this).attr('state', 'closed'); }})
            .focus(function() {if ($(this).attr('state') != 'open') {$(this).autocomplete("search");}});

        $(".autoComConfBinary").autocomplete({source: autoComplete.binaryConfNameTags, minLength: 0,open: function() { $(this).attr('state', 'open'); },close: function () { $(this).attr('state', 'closed'); }})
            .focus(function() {if ($(this).attr('state') != 'open') {$(this).autocomplete("search");}});

        $(".autoComBinarySwName").autocomplete({source: autoComplete.binarySwNameTags, minLength: 0,open: function() { $(this).attr('state', 'open'); },close: function () { $(this).attr('state', 'closed'); }})
            .focus(function() {if ($(this).attr('state') != 'open') {$(this).autocomplete("search");}});

        $(".autoComBinarySwVersion").autocomplete({source: autoComplete.binarySwVersionTags, minLength: 0,open: function() { $(this).attr('state', 'open'); },close: function () { $(this).attr('state', 'closed'); }})
            .focus(function() {if ($(this).attr('state') != 'open') {$(this).autocomplete("search");}});

        $(".autoComBinaryDivision").autocomplete({source: autoComplete.binaryDivisionTags, minLength: 0,open: function() { $(this).attr('state', 'open'); },close: function () { $(this).attr('state', 'closed'); }})
            .focus(function() {if ($(this).attr('state') != 'open') {$(this).autocomplete("search");}});

        $(".autoComCodeNo").autocomplete({source: autoComplete.codeNoTags, minLength: 0,open: function() { $(this).attr('state', 'open'); },close: function () { $(this).attr('state', 'closed'); }})
            .focus(function() {if ($(this).attr('state') != 'open') {$(this).autocomplete("search");}});

        $(".autoComCodeNm").autocomplete({source: autoComplete.codeNmTags, minLength: 0,open: function() { $(this).attr('state', 'open'); },close: function () { $(this).attr('state', 'closed'); }})
            .focus(function() {if ($(this).attr('state') != 'open') {$(this).autocomplete("search");}});

        $(".autoComCreator").autocomplete({source: autoComplete.creatorTag, minLength: 0,open: function() { $(this).attr('state', 'open'); },close: function () { $(this).attr('state', 'closed'); }})
            .focus(function() {if ($(this).attr('state') != 'open') {$(this).autocomplete("search");}});

        $(".autoComReviewer").autocomplete({source: autoComplete.reviewerTag, minLength: 0,open: function() { $(this).attr('state', 'open'); },close: function () { $(this).attr('state', 'closed'); }})
            .focus(function() {if ($(this).attr('state') != 'open') {
                $(this).autocomplete("search");}
                if($(".ui-autocomplete").is(':visible')){
                    $(".ui-autocomplete").css("width", parseInt($(this).css("width")) + 20);
                }
            });

        $(".autoComCreatorDivision").autocomplete({source: autoComplete.creatorDivisionTag, minLength: 0,
            open: function() { $(this).attr('state', 'open'); },close: function () { $(this).attr('state', 'closed'); if($(this).parent().find('input[name=creatorNm]').val() == ""){$(this).parent().find('input[name=creator]').val('');}},
            select: function(event,ui){$(this).parent().find('input[name=creator]').val(ui.item.id);}
        })
            .focus(function() {if ($(this).attr('state') != 'open') {$(this).autocomplete("search");}})
            .autocomplete( "instance" )._renderItem = function( ul, item ) {
            if(item.division) {v1
                return $( "<li>" ).append( "<div>" + item.division + ' > ' + item.label + "("+item.id+")"+"</div>" ).appendTo( ul );
            } else {
                return $( "<li>" ).append( "<div>" + item.label + "("+item.id+")"+"</div>" ).appendTo( ul );
            }

        };
    }
}

function replaceWithLink(text){
    return text.replace(LINKREGEXP, findAndReplace);
}

function findAndReplace(match) {
    var prj = /PRJ/i;
    var third = /3rd/i;
    var arrLink = match.split('-');
    var id = arrLink[1];
    var protocol = window.location.protocol;
    var host =  window.location.host;
    var url = protocol + "//" + host;
    if(prj.test(match)) {
        url += "/project/view/" + id;
    } else if(third.test(match)) {
        url += "/partner/view/" + id;
    }
    return "<a href=" + url +" class='urlLink2' target='_blank' onclick='window.open(this.href)'>" +  match + "</a>";
}