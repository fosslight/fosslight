let LINKREGEXP = /PRJ-\d+(?!.*\<\/a\>)|3rd-\d+(?!.*\<\/a\>)/gi;
let onAjaxLoadingHide = false;
let doNotUseAutoLoadingHideFlag = 'N';
let gRowCnt = '';

$( document ).ajaxSend(function( event, jqxhr, settings ) {
    jqxhr.setRequestHeader("AJAX", true);
});

$( document ).ajaxError(function( event, jqxhr, settings, thrownError  ) {
    if ( jqxhr != undefined && (jqxhr.status == 401 || jqxhr.status == 403) ) {
        alert("Your session has expired. Please log in again");
        location.href = "/";
    }
});

/**
 * alertify Default setting */
$(document).ready(function (){
    if(typeof alertify !== 'undefined') {
        alertify.defaults = {
            // dialogs defaults
            autoReset:true,
            basic:false,
            closable:true,
            closableByDimmer:false,
            frameless:false,
            maintainFocus:false, // <== global default not per instance, applies to all dialogs
            maximizable:true,
            modal:true,
            movable:true,
            moveBounded:false,
            overflow:true,
            padding: true,
            pinnable:true,
            pinned:true,
            preventBodyShift:false, // <== global default not per instance, applies to all dialogs
            resizable:true,
            startMaximized:false,
            transition:'fade',

            // notifier defaults
            notifier:{
                // auto-dismiss wait time (in seconds)
                delay:3,
                // default position
                position:'bottom-right'
            },

            // language resources
            glossary:{
                // dialogs default title
                title:'FOSSLight Hub',
                // ok button text
                ok: 'OK',
                // cancel button text
                cancel: 'Cancel'
            },

            // theme settings
            theme:{
                // class name attached to prompt dialog input textbox.
                input:'ajs-input',
                // class name attached to ok button
                ok:'ajs-ok',
                // class name attached to cancel button
                cancel:'ajs-cancel'
            }
        };
    }

});

$( document ).ajaxSend(function( event, jqxhr, settings ) {
    jqxhr.setRequestHeader("AJAX", true);
});

$( document ).ajaxError(function( event, jqxhr, settings, thrownError  ) {
    if ( jqxhr != undefined && (jqxhr.status == 401 || jqxhr.status == 403) ) {
        alert("Your session has expired. Please log in again");
        location.href = "/";
    }
});

$( document ).ready(function (){
    $(document).keydown(function(e){
        if(e.target.nodeName != "INPUT" && e.target.nodeName != "TEXTAREA") {
            if(e.keyCode === 8) {
                return false;
            }
        }

        if(e.keyCode===82 && e.altKey && !e.shiftKey){//Alt + R - 이전 탭으로 가기
            returnTabInFrame();
        }

        if(e.keyCode===87 && e.altKey && !e.shiftKey){//Alt + W - 현재 탭 닫기
            activeDeleteTabInFrame();
        }

        if(e.altKey && e.shiftKey && e.keyCode===87){//Shift + Alt + W - 전체 탭 닫기
            allDeleteTabInFrame();
        }

        if(e.keyCode===37 && e.shiftKey && e.altKey){//Shift + Alt + Arrow Left - 탭 왼쪽으로 이동
            changeTabInFrame('left');
        }

        if(e.keyCode===39 && e.shiftKey && e.altKey){//Shift + Alt + Arrow Right - 탭 오른쪽으로 이동
            changeTabInFrame('right');
        }
    });

    $(window).ajaxStart(function(event, jqxhr, settings){
        var _targetUrl = event.target.URL;

        if(_targetUrl && "function" != typeof(_targetUrl)) {
            loading.show();

            onAjaxLoadingHide = false;
        }
    }).ajaxStop(function(){
        loading.hide();
    }).ajaxError(function(event, jqxhr, ssettings, exception) {
        doNotUseAutoLoadingHideFlag = "N";

        loading.hide();
    });

    (function($){
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

        //create combobox by code
        $.fn.makeSelectByCodes = function(typeCodes){
            "use strict";
            var $this = this;

            typeCodes.forEach(function(typeCode){
                $('<option value="'+typeCode.cdDtlNo+'">'+typeCode.cdDtlNm+'</option>').appendTo($this);
            });
        }

        //element value check
        $.fn.isValueEmpty = function(){
            return this.val() == null || this.val() == '';
        }
    }(jQuery));

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

function stringToArray (str) {
    return arr = str.split(",").map(Number);
}

function setMaxRowCnt(maxRowCnt){
    if(!/.+/.test(gRowCnt)){
        gRowCnt = maxRowCnt;
    }
}

var loading = {
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