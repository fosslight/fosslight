var lastTab = -1;	//이전 탭 기억변수
var selectTab = -1;	//현재 탭 기억변수
var deleteFlag = false;
var LINKREGEXP = /PRJ-\d+(?!.*\<\/a\>)|3rd-\d+(?!.*\<\/a\>)/gi;
var editMode;

$(document).ajaxSend(function (event, jqxhr, settings) {
    jqxhr.setRequestHeader("AJAX", true);
});

$(document).ajaxError(function (event, jqxhr, settings, thrownError) {
    if (jqxhr != undefined && (jqxhr.status == 401 || jqxhr.status == 403)) {
        alert("Your session has expired. Please log in again");
        location.href = "/";
    }
});

var _popupComment = null;
var onAjaxLoadingHide = false;
// 하기 flag사용시에는 반드시 초기화까지 설정해줘양함
var doNotUseAutoLoadingHideFlag = 'N';
var gRowCnt = "";

$(document).ready(function () {
    $(document).keydown(function (e) {
        if (e.target.nodeName != "INPUT" && e.target.nodeName != "TEXTAREA" && (e.target.nodeName == "DIV" && e.target.className.indexOf("note-editable") == -1)) {
            if (e.keyCode === 8) {
                return false;
            }
        }

        if (e.keyCode === 82 && e.altKey && !e.shiftKey) {//Alt + R - 이전 탭으로 가기
            returnTabInFrame();
        }

        if (e.keyCode === 87 && e.altKey && !e.shiftKey) {//Alt + W - 현재 탭 닫기
            activeDeleteTabInFrame();
        }

        if (e.altKey && e.shiftKey && e.keyCode === 87) {//Shift + Alt + W - 전체 탭 닫기
            allDeleteTabInFrame();
        }

        if (e.keyCode === 37 && e.shiftKey && e.altKey) {//Shift + Alt + Arrow Left - 탭 왼쪽으로 이동
            changeTabInFrame('left');
        }

        if (e.keyCode === 39 && e.shiftKey && e.altKey) {//Shift + Alt + Arrow Right - 탭 오른쪽으로 이동
            changeTabInFrame('right');
        }
    });
    $(document).ajaxStart(function (event, jqxhr, settings) {
        var _targetUrl = event.target.URL;

        if (_targetUrl && "function" != typeof (_targetUrl)) {
			loading.show();
			
            onAjaxLoadingHide = false;
        }
    }).ajaxStop(function () {
		loading.hide();
    }).ajaxError(function (event, jqxhr, ssettings, exception) {
        doNotUseAutoLoadingHideFlag = "N";

        loading.hide();
    });

    $('.headerHandle').click(function () {
        $('#wrapBack').toggleClass('headerHide');
    });

    $('.btn_acc').click(function () {
        $('.accpop').show();
        $('#blind_wrap').show();
        $('html, body').animate({scrollTop: $("#wrap").offset().top}, 0);
        $('html').css('overflow', 'hidden');
    });//팝업 열기

    $('.pclose').click(function () {
        $('.pop').hide();
        $('#blind_wrap').hide();
        $('html').css('overflow', 'auto');
    }); // 팝업 닫기

    var select = $(".selectSet select");

    select.change(function () {
        var select_name = $(this).children("option:selected").text();
        $(this).siblings("strong").text(select_name);
    });

    for (i = 0; i < $(".selectSet select").length; i++) {
        var select_ = $(".selectSet select").get(i);
        if ($(select_).is(":disabled")) $(select_).siblings('strong').css('opacity', '0.5');
    } // select disabled opacity

    $("#btnRejectNotice").click(function () {
        if (projectStatus == "COMP") {
            alertify.warning('If you need to modify, please click a "request to open" button in the basic information tab of the project.');
        } else {
            alertify.warning('If you need modify, please leave a comment on FOSSLight team.');
        }

        return false;
    });

    function tabExitHide() {
        var $tabs = $('#nav-tabs').tabs();
        var tab_length = $tabs.find('.nav-tab-menu li span').length;

        if (tab_length <= 2) {
            $tabs.find('.nav-tab-menu li input').hide();
        } else {
            $tabs.find('.nav-tab-menu li input').show();
        }
    }

    /* Create new tab function */
    $(function newTab() {
        // initTab();
        var $tabs = $('#nav-tabs').tabs();

        $('.add-tab').click(function (e) {
            e.preventDefault();
            $tabs.find(".ui-tabs-nav").sortable({
                axis: "x",
                stop: function () {
                    $tabs.tabs("refresh");
                }
            });

            var tabName = $(this).text(),
                tabLink = $(this).attr('href'),
                tabAnchor = $(this).attr('href').replace(/#/g, ''),
                tabNumber = -1;

            //2018-08-16 choye 추가
            var aTitle = $(this).attr('title');

            if (aTitle != null && aTitle != "" && aTitle != undefined) {
                tabName = aTitle;
            }

            var tab_length = $tabs.find('.nav-tab-menu li span').length;

            if (tabLink.indexOf("?") != -1) {
                var tmp = tabLink.split('?');

                tabLink = tmp[0];
            }

            if (tabAnchor.indexOf("?") != -1) {
                var tmp = tabAnchor.split('?');

                tabAnchor = tmp[0];
            }

            $tabs.find('.nav-tab-menu li span').each(function (i) {
                if ($(this).text() == tabName) {
                    tabNumber = i;
                }
            });

            if (tabNumber >= 0) {
                $tabs.tabs('option', 'active', tabNumber - 1);
                reloadTab(tabAnchor, "create");
                lastTab = selectTab;
                selectTab = tabNumber - 1;
            } else {
                var appendIFrame = "<iframe src='" + tabAnchor + "' style='width:100%; height:100%;' scrolling='yes' marginwidth='0' marginheight='0' frameborder='0' vspace='0' hspace='0'></iframe>";
                $(".contents").append("<div id=" + tabAnchor.replace(/\//g, '-') + " class='contentsBack'>" + appendIFrame + "</div>");
                $("<li><span><a class=" + tabAnchor.replace(/\//g, '-') + " href=" + tabLink.replace(/\//g, '-') + ">" + tabName + "</a></span><input type='button' value='x' class='ui-icon ui-icon-close' /></li>")
                    .appendTo(".nav-tab-menu");
                $("#nav-tabs").tabs("refresh");
                $('#nav-tabs').tabs('option', 'active', -1);

                lastTab = selectTab;

                selectTab = $("#nav-tabs").tabs('option', 'active');
            }

            // 화면갱신
            viewRefresh();

            return false;
        });
        
        initTab();
    });
	
	function initTab(){
		var query = window.location.search;
		var param = new URLSearchParams(query);
		var id = param.get("id");
		var prjFlag = param.get("project");
		var viewFlag = param.get("view");
		
		if (id != null && prjFlag != null) {
			if ("false" == viewFlag) {
				createTabNew(prjFlag == 'true' ? id + "_Project" : id + "_3rdParty", prjFlag == 'true' ? "/project/edit/" + id : "/partner/edit/" + id);
			} else {
				createTabNew(prjFlag == 'true' ? id + "_Project" : id + "_3rdParty", prjFlag == 'true' ? "/project/view/" + id : "/partner/view/" + id);
			}
		} else {
//			var _defaultTabStr = $("#defaultTabAnchorArr").val()||"";

//			$.each(_defaultTabStr.split(","), function(idx, val){
//				var _gnbHref = $("#header > div > div.gnb a[href$='"+val+"']");
//
//				if(_gnbHref && _gnbHref.length == 1) {
//					$("#header > div > div.gnb a[href$='"+val+"']").trigger("click");
//				}
//			});
		}
	}
	
	/* Delete tab function */
    $(document).on('click', '.ui-icon-close', function (event) {
        viewRefresh();

        var parent = $(this).parent(),
            index = parent.index(),
            tabs = $(this).closest(".ui-tabs"),
            panel = tabs.children().eq(index + 1),
            tabLink = parent.find("a").attr("href");
        var i = index - 1;
        var iframeDiv = "div:eq(" + i + ")";
        if ($(".contents").children(iframeDiv).find("iframe").contents().find("#loading_wrap").css("display") == "block") {
            alertify.alert('Work in progress now. You cannot close the tab.', function (e) {
            });
        } else {
            $(tabLink).remove();
            parent.remove();
            panel.remove();

            if (lastTab > selectTab) lastTab--;

            $("#nav-tabs").tabs("refresh").tabs('option', 'active', lastTab > -1 ? lastTab : 0);

            selectTab = lastTab;
        }
    });

    /* tab click event */
    $(document).on('click', '.ui-tabs-anchor', function (event) {
        lastTab = selectTab;
        selectTab = $("#nav-tabs").tabs('option', 'active');

        viewRefresh();
    });


    $(window).bind('resize', function () {
        // 브라우저 창 크기에 따라 jqGrid Width 자동 조절
//      tableRefresh();

//		tableRefreshContentsArea();
//		tableRefreshGridArea();

        // 화면갱신
//      viewRefresh();
    }).trigger('resize');

    $(".commentBtn").click(function () {
        $(this).toggleClass("open");
        // $('.commentEditor').toggle();
        fn.handleUserCommentPopupOpen();
        $('.projectContents').toggleClass("pt255");
    });

    $(".btnExpand").click(function () {
        $(this).toggleClass("on");
        $('.adminSearch').toggle();
    });

    $(".btnHiddenExpand").click(function () {
        $(this).toggleClass("on");
        $('.hiddenSearch').toggle();
    });

    $(".btnToggle").click(function () {
        $(this).toggleClass("on");
        $('.editSearchUp, .threeRdSearch').toggle();
    });

    $("#moreInfoBtn").click(function() {
        $("#moreSearchContent").slideToggle();
    });

    $('[data-toggle="tooltip"]').tooltip();

    (function ($) {
        //form data -> json data
        $.fn.serializeObject = function () {
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
                    if (isArrObject) {
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
        $.fn.makeSelectByCodes = function (typeCodes) {
            "use strict";
            var $this = this;

            typeCodes.forEach(function (typeCode) {
                $('<option value="' + typeCode.cdDtlNo + '">' + typeCode.cdDtlNm + '</option>').appendTo($this);
            });
        }

        //element value check
        $.fn.isValueEmpty = function () {
            return this.val() == null || this.val() == '';
        }
    }(jQuery));

    autoComplete.load();
    autoComplete.init();
});

var loading = {
    show: function () {
        if ($('#loading_wrap').css("display") == "none" && !onAjaxLoadingHide) {
            $('#loading_wrap').show();
        }
    },
    hide: function () {
        if ("Y" != doNotUseAutoLoadingHideFlag) {
            $('#loading_wrap').hide();
        }
    }
}

var loadingIden = {
    show: function () {
        if ($('#loading_iden').css("display") == "none") {
            $('#loading_iden').show();
        }
    },
    hide: function () {
        $('#loading_iden').hide();
    }
}

/* iFrame에서 호출 (보안문제로 broadcast하여 호출) */
function receiveMessage(event) {
    if ('undefined' !== typeof event.data && 'string' === typeof event.data) {
        var data = JSON.parse(event.data);

        switch (data.action) {
            case 'create':
                createTabNew(data.tabData[0], data.tabData[1]);

                break;
            case 'delete':
                deleteTab(data.tabData[0]);

                break;
            case 'activeDelete':
                activeDeleteTab();

                break;
            case 'allDelete':
                allDeleteTab();

                break;
            case 'return':
                returnTab();

                break;
            case 'reload':
                reloadTab(data.tabData[0], data.action);

                break;
            case 'change':
                changeTab(data.tabData[0]);

                break;
            case 'create_new':
                createTab_new(data.tabData[0], data.tabData[1], data.tabData[2], data.tabData[3]);

                break;
            case 'delete_new':
                deleteTab_new(data.tabData[0]);

                break;
            case 'reload_new':
                reloadTab_new(data.tabData[0], data.tabData[1], data.action);

                break;
            case 'changeURL':
                changeURL(data.link);

                break;
        }
    }
}

if ('addEventListener' in window) {
    window.addEventListener('message', receiveMessage, false);
} else if ('attachEvent' in window) { //IE
    window.attachEvent('onmessage', receiveMessage);
}

/* 부모창의 createTab 함수 호출 */
function createTabInFrame(tabNm, tabLk) {
    var tabData = [tabNm, tabLk];
    var data = {
        tabData: tabData,
        action: 'create'
    }

    parent.postMessage(JSON.stringify(data), "*");

}

function createTabInFrameWithCondition(tabNm, tabLk, sesKey, sesVal) {
    $.ajax({
        type: 'GET',
        url: '/sessionKeyValSave/' + sesKey + '/' + sesVal,
        async: false,
        data: {'sesKey': sesKey, 'sesVal': sesVal},
        headers: {'Content-Type': 'application/json'}
    });

    var tabData = [tabNm, tabLk];
    var data = {
        tabData: tabData,
        action: 'create'
    };

    parent.postMessage(JSON.stringify(data), "*");
}

/* 부모창의 deleteTab 함수 호출 */
var deleteTabInFrame = function (tabLk) {
    var tabData = [];
    tabData[0] = tabLk;

    var data = {
        tabData: tabData,
        action: 'delete'
    };

    parent.postMessage(JSON.stringify(data), "*");
}
/* 부모창의 deleteTab 함수 호출 */
var activeDeleteTabInFrame = function () {
    var data = {
        action: 'activeDelete'
    };

    parent.postMessage(JSON.stringify(data), "*");
}
var allDeleteTabInFrame = function () {
    var data = {
        action: 'allDelete'
    };

    parent.postMessage(JSON.stringify(data), "*");
}
var returnTabInFrame = function () {
    var data = {
        action: 'return'
    };

    parent.postMessage(JSON.stringify(data), "*");
}
/* 부모창의 reloadTab 함수 호출 */
var reloadTabInframe = function (tabLk) {
    var tabData = [];
    tabData[0] = tabLk;

    var data = {
        tabData: tabData,
        action: 'reload'
    };

    parent.postMessage(JSON.stringify(data), "*");
}
var changeTabInFrame = function (tabNumber) {
    var tabData = [];
    tabData[0] = tabNumber;

    var data = {
        tabData: tabData,
        action: 'change'
    };

    parent.postMessage(JSON.stringify(data), "*");
}

var activeTabInFrameList = function (targetTab) {
    var tabName = "";

    if ("PROJECT" == targetTab) {
        tabName = "Project List";
    } else if ("PARTNER" == targetTab) {
        tabName = "3rd Party List";
    } else if ("OSS" == targetTab) {

    } else if ("LICENSE" == targetTab) {

    }

    if (tabName != "") {
        var _prjTabIdx = getTabIndex(tabName);
        if (_prjTabIdx != "") {
            var tabData = [];
            tabData[0] = _prjTabIdx;

            var data = {
                tabData: tabData,
                action: 'change'
            };

            parent.postMessage(JSON.stringify(data), "*");
        }
    }
}

/* iFrame에서 호출한 createTab */
var createTab = function (tabNm, tabLk) {
    var $tabs = $('#nav-tabs').tabs();
    var tab_length = $tabs.find('.nav-tab-menu li span').length;
    var tabName = tabNm,
        tabArr = tabLk.split('?'),
        tabLink = tabArr[0],
        tabAnchor = tabArr[0].replace(/#/g, ''),
        tabNumber = -1;

    if (tab_length > 20) {
        alertify.error('More than 20 tabs are opened. Please close the tabs.', 0);

        return;
    }

    oldTabName = '';
    $tabs.find('.nav-tab-menu li span').each(function (i) {
        if ($(this).text() == tabName) {
            oldTabName = $(this).children().attr('href').replace(/-/g, '/');
            tabNumber = i;
        }
    });

    if (tabName != 'Project Editor') {
        if (tabNumber >= 0) {
            deleteTabByName(oldTabName);
        }
    }

    /*
	 * 2016.11.03 hk-cho
	 * - '/project/identification'의 경우 path가 아닌 querystring을 받기 때문에 div.contents의 id가 중복되는 현상 발생
	 * /project/identification이 넘어올 경우 id에 식별자를 추가하여 id중복 현상 제거
	 */
    var frameSrc = tabAnchor;
    var checkProjIden = tabAnchor.split('/');

    if (checkProjIden[1] == 'oss') {
        frameSrc = tabLk.replace('#', '');
    }

//	var appendIFrame = "<iframe src='" + frameSrc + "' style='width:100%; height:100%;' scrolling='yes' marginwidth='0' marginheight='0' frameborder='0' vspace='0' hspace='0'></iframe>";
//
//	$(".contents").append("<div id=" + tabAnchor.replace(/\//g, '-') + " class='contentsBack'>" + appendIFrame + "</div>");
//	$("<li><span><a class=" + tabAnchor.replace(/\//g, '-') + " href=" + tabLink.replace(/\//g, '-') + ">" + tabName + "</a></span><input type='button' value='x' class='ui-icon ui-icon-close' /></li>")
//	.appendTo(".nav-tab-menu");

    $("#nav-tabs").tabs("refresh");

    //마지막 탭으로 이동
    $('#nav-tabs').tabs('option', 'active', -1);

    //현태탭 변수에 저장
    if (!deleteFlag) {
        lastTab = selectTab;
    } else {
        deleteFlag = false;
    }

    selectTab = $("#nav-tabs").tabs('option', 'active');

    // 화면갱신
    viewRefresh();

    //iframe에 focus 2017.03.22
    var iframe = $("#" + tabAnchor.replace(/\//g, '-'))[0];
    var iframe_html = $(iframe).find('iframe')[0];

    body_html = $(iframe_html).focus();
}
var deleteTabByName = function (tabLk) {
    var tabLink = tabLk.replace(/\//g, '-')
        , tabAnchor = tabLink.replace(/#/g, '.')
        , parent = $(tabAnchor).parent().parent()
        , panel = $(tabAnchor).parent().next();

    $(tabLink).remove();	//div
    parent.remove();		//li
    panel.remove();			//input

    $("#nav-tabs").tabs("refresh");
    $('#nav-tabs').tabs('option', 'active', lastTab);
}

var deleteTab = function (tabLk) {
    $(".nav-link.active").prev().trigger("click");
}

var activeDeleteTab = function () {
    var activeTabId = $(".nav-link.active").attr("id");
   	var activePanelId = "panel--" + activeTabId.substring(5, activeTabId.length);
   
    $("#" + activeTabId).parent().remove();
    $("#" + activePanelId).remove();
}

var allDeleteTab = function () {
    var tabs = $("#nav-tabs").tabs();

    $.each(tabs.find(".ui-state-default"), function (i, panel) {
        var panelId = $(panel).attr("aria-controls");
        $(panel).remove();
        $("#" + panelId).remove();
    });

    tabs.tabs("refresh");
    $(document).focus();
}

var reloadTab = function (tabLk, act) {
    $('iframe').each(function () {
        var url = $(this).attr('src');

        if (url.indexOf("?") != -1) {
            var tmp = url.split('?');
            url = tmp[0];
        }

        if (url == tabLk) {
            if(act == "create"){
				$(this).attr('src', url);
			} else if(act == "reload"){
				$(this).attr('src', url);
			}
        }
    });
}

var returnTab = function () {
    $('#nav-tabs').tabs('option', 'active', lastTab);

    var temp = 0;
    temp = lastTab;
    lastTab = selectTab;
    selectTab = temp;
}
var changeTab = function (pos) {
    var tabs = $("#nav-tabs");
    var tabNumber = tabs.tabs('option', 'active');
    var tab_length = tabs.find('.nav-tab-menu li span').length;

    if (isNaN(pos)) {
        if (pos == 'right') {
            tabNumber++;
        } else if (pos == 'left') {
            tabNumber--;
        } else {
            tabNumber = pos;
        }
    } else {
        tabNumber = pos;
    }

    if (tabNumber > tab_length - 2) {
    }
    if (tabNumber < 0) {
    } else {
        $('#nav-tabs').tabs('option', 'active', tabNumber);
        lastTab = selectTab;
        selectTab = tabNumber;
    }
}

// error msg
function makeErrMsg(msg) {
    return '<div class="retxt text-danger text-sm">' + msg + '</div>';
}

function cleanErrMsg(gridStr, rowId) {
    // 그리드 _로우 클래스 일경우
    if (typeof (gridStr) != 'undefined' && typeof (rowId) != 'undefined') {
        $("div.retxt." + gridStr + "_" + rowId).remove();
        $("div.retxtb." + gridStr + "_" + rowId).remove();
        $("div.retxtg." + gridStr + "_" + rowId).remove();
    } else if (typeof (gridStr) != 'undefined' && typeof (rowId) == 'undefined') {
        $("#" + gridStr + " div.retxt").remove();
        $("#" + gridStr + " div.retxtb").remove();
        $("#" + gridStr + " div.retxtg").remove();
    } else {
        $("div.retxt").remove();
        $("div.retxtb").remove();
        $("div.retxtg").remove();
    }
}

function showErrMsg() {
    $("div.retxt").show();
    $("span.retxt").show();
}

function hideErrMsg() {
    $("div.retxt").hide();
    $("span.retxt").hide();
}

function gridCleanErrMsg(gridStr) {
    $("#" + gridStr + " div.retxt").remove();
    $("#" + gridStr + " div.retxtb").remove();
    $("#" + gridStr + " div.retxtg").remove();
}

// 그리드 체크 메세지( gridStr 그리드 문자열 )
function gridValidMsg(msgData, gridStr) {
    var target = $("#" + gridStr);
    var mainIds = target.jqGrid('getDataIDs');

    $.each(msgData, function (key, value) {
        if ("isValid" != key) {
            var seqSuffix = key.split(".");
            var rowId = seqSuffix[1];
            var subIds = "";
            var gridId = "";
            var subGrid;

            // gridId와 실제 rowId 가 다를 경우 처리
            for (var i in mainIds) {
                gridId = target.jqGrid('getCell', mainIds[i], 'gridId');

                if (rowId == gridId && mainIds[i] != gridId) {
                    rowId = mainIds[i];

                    break;
                }

                subGrid = $("#" + gridStr + "_" + mainIds[i] + "_t");
                subIds = subGrid.jqGrid('getDataIDs');

                for (var y in subIds) {
                    gridId = subGrid.jqGrid('getCell', subIds[y], 'gridId');

                    if (rowId == gridId && subIds[y] != gridId) {
                        rowId = subIds[y];

                        break;
                    }
                }
            }

            var colName = $("#" + gridStr + " #" + rowId).parents("table").attr("id") + "_" + seqSuffix[0];

            // 그리드 메세지 그리기
            $("#" + gridStr + " #" + rowId + " td[aria-describedby=\"" + colName + "\"]").append('<div class=\"' + gridStr + "_" + rowId + ' retxt\">' + value + '</div>');
        }
    });
}

// 성능개선 버전
function gridValidMsgNew(msgData, gridStr, type) {
    type = type || "NORMAL";
    var target = $("#" + gridStr);
    var mainIds = target.jqGrid('getDataIDs');

    if (msgData) {
        $.each(msgData, function (key, value) {
            if ("isValid" != key && "validMsg" != key && "resultData" != key && "externalData" != key && "externalData2" != key) {
                var seqSuffix = key.split(".");
                if (seqSuffix.length > 1) {
                    var rowId = seqSuffix[1];

                    if (seqSuffix[1].indexOf("-") > -1 && type == "NORMAL") {
                        rowId = seqSuffix[1].replace(/(\d+)(?=\-)(.+)/g, "$1"); // rowId가 multi License라서 a to b 라면 a값만 가져옴.
                    }

                    var errRow = $("#" + rowId + " > td[aria-describedby='" + gridStr + "_" + seqSuffix[0] + "']");

                    // 그리드 메세지 그리기
                    if (errRow) {
                        errRow.append('<div class=\"' + gridStr + "_" + rowId + ' retxt\">' + value + '</div>');
                    }
                }
            }
        });
    }
}

function gridValidMsg2(msgData, gridStr) {
    var mainIds = $("#" + gridStr).jqGrid('getDataIDs');

    $.each(msgData, function (key, value) {
        if ("isValid" != key) {
            var seqSuffix = key.split(".");
            var rowId = seqSuffix[1];
            var subIds = "";
            var gridId = "";

            // gridId와 실제 rowId 가 다를 경우 처리
            for (var i in mainIds) {
                gridId = $("#" + gridStr).jqGrid('getCell', mainIds[i], 'componentId');

                if (rowId == gridId && mainIds[i] != gridId) {
                    rowId = mainIds[i];
                    break;
                }

                subIds = $("#" + gridStr + "_" + mainIds[i] + "_t").jqGrid('getDataIDs');

                for (var y in subIds) {
                    gridId = $("#" + gridStr + "_" + mainIds[i] + "_t").jqGrid('getCell', subIds[y], 'gridId');

                    if (rowId == gridId && subIds[y] != gridId) {
                        rowId = subIds[y];

                        break;
                    }
                }
            }

            var colName = $("#" + gridStr + " #" + rowId).parents("table").attr("id") + "_" + seqSuffix[0];

            // 그리드 메세지 그리기
            $("#" + gridStr + " #" + rowId + " td[aria-describedby=\"" + colName + "\"]").append('<div class=\"' + gridStr + "_" + rowId + ' retxt\">' + value + '</div>');
        }
    });
}

function createValidMsgComplex(msgData) {
    hideErrMsg();
    
    //닉네임, 그리드데이터, 일반 인풋 Validation 체크
    $.each(msgData, function (key, value) {
        if ("isValid" != key && "validMsg" != key) {
            if (key.indexOf(".") > -1) {
                var seqSuffix = key.split(".");
                var targetId = seqSuffix[1] + "_" + seqSuffix[0];

                if (seqSuffix[0] == 'licenseNicknames') {
                    $('input[name=licenseNicknames]:eq(' + (Number(seqSuffix[1]) - 1) + ')').focus().parent().next("span.retxt").html(value).show();
                }

                if ($('input[id=' + targetId + ']').length > 0) {
                    $('input[id=' + targetId + ']').focus().after(makeErrMsg(value)).show();
                } else if ($('textarea[id=' + targetId + ']').length > 0) {
                    $('textarea[id=' + targetId + ']').focus().after(makeErrMsg(value)).show();
                } else if ($('select[id=' + targetId + ']').length > 0) {
                    $('select[id=' + targetId + ']').focus().after(makeErrMsg(value)).show();
                }
            } else {
                if (key == 'licenseNicknames') {
                    $('input[name=licenseNicknames]').focus().parent().next("span.retxt").html(value).show();
                }

                if (key == 'validMsgModelList') {
                    $('#validMsgModelList').html(value).show();
                }
				
				if ('distributeTarget' == key) {
					$('.distributionSiteDiv').addClass("cus-is-invalid");
					$('.distributionSiteDiv').focus().next("span.retxt,div.retxt").html(value).show();
				}
				
				if ('networkServerType' == key) {
					$('.networkServerTypeDiv').addClass("cus-is-invalid");
					$('.networkServerTypeDiv').focus().next("span.retxt,div.retxt").html(value).show();
				}
				
				if ('noticeType' == key) {
					$('.noticeTypeDiv').addClass("cus-is-invalid");
					$('.noticeTypeDiv').focus().next("span.retxt,div.retxt").html(value).show();
				}
				
                if ($('input[name=' + key + ']').length > 0) {
                    $('input[name=' + key + ']').focus().next("span.retxt,div.retxt").html(value).show();
                    $('input[name=' + key + ']').addClass("is-invalid");
                } else if ($('textarea[name=' + key + ']').length > 0) {
                    $('textarea[name=' + key + ']').focus().next("span.retxt,div.retxt").html(value).show();
                    $('textarea[name=' + key + ']').addClass("is-invalid");
                } else if ($('select[name=' + key + ']').length > 0) {
					if ('osType' == key || 'licenseType' == key) {
						$('select[name=' + key + ']').focus().next().next("span.retxt,div.retxt").html(value).show();
					} else {
						$('select[name=' + key + ']').focus().next("span.retxt,div.retxt").html(value).show();
					}
                    
                    $('select[name=' + key + ']').addClass("is-invalid");
                }
            }
        }
    });
}

function hidePageNav(pagerId) {
    if ($("#" + pagerId + "_center")) {
        $("#" + pagerId + "_center").hide();
    }

    if ($("#" + pagerId + "_right")) {
        $("#" + pagerId + "_right").hide();
    }
}

function gridListBulkEdit(listId, fn) {
    var grid = $("#" + listId);
    var ids = grid.jqGrid('getDataIDs');

    for (var i = 0; i < ids.length; i++) {
        if (fn) {
            grid.jqGrid('editRow', ids[i], true, fn);
        } else {
            grid.jqGrid('editRow', ids[i], true);
        }
    }
}

var jsonOptions = {
    type: "POST",
    contentType: "application/json; charset=utf-8",
    dataType: "json"
};

function createJSON(postdata) {
    if (postdata.id === '_empty') {
        postdata.id = null; // rest api expects int or null
    }

    return JSON.stringify(postdata)
}

//비교 및 연산
function changeObjectToArray(obj) {
    if (obj == '' || obj == null) {
        obj = [];
    } else if (typeof obj == 'string') {
        obj = [obj];
    }

    return obj;
}

//자동완성용 공통 AJAX
var commonAjax = {
    getLicenseTags: function (data) {
        return fnBasicAjaxData(data, "/license/autoCompleteAjax");
    },
    getOssTags: function (data) {
        return fnBasicAjaxData(data, "/oss/autoCompleteAjax");
    },
    //project
    getProjectNameTags: function (data) {
        return fnBasicAjaxData(data, "/project/autoCompleteAjax");
    },
    getProjectNameConfTags: function (data) {
        return fnBasicAjaxData(data, "/project/autoCompleteAjax?identificationStatus=CONF");
    },
    getProjectIdConfTags: function (data) {
        return fnBasicAjaxData(data, "/project/autoCompleteIdAjax");
    },
    getProjectVersionTags: function (data) {
        return fnBasicAjaxData(data, "/project/autoCompleteVersionAjax");
    },
    getProjectModelTags: function (data) {
        return fnBasicAjaxData(data, "/project/autoCompleteModelAjax");
    },
    //partner
    getPartnerNmTags: function (data) {
        return fnBasicAjaxData(data, "/partner/autoCompleteNmAjax");
    },
    //partner confirmed
    getPartnerConfNmTags: function (data) {
        return fnBasicAjaxData(data, "/partner/autoCompleteConfNmAjax");
    },
    getPartnerConfIdTags: function (data) {
        return fnBasicAjaxData(data, "/partner/autoCompleteConfIdAjax");
    },
    getPartnerSwNmTags: function (data) {
        return fnBasicAjaxData(data, "/partner/autoCompleteSwNmAjax");
    },
    getPartnerConfSwNmTags: function (data) {
        return fnBasicAjaxData(data, "/partner/autoCompleteConfSwNmAjax");
    },
    getPartnerSwVerTags: function (data) {
        return fnBasicAjaxData(data, "/partner/autoCompleteSwVerAjax");
    },
    getPartnerConfSwVerTags: function (data) {
        return fnBasicAjaxData(data, "/partner/autoCompleteConfSwVerAjax");
    },
    getPartnerModifierTags: function (data) {
        return fnBasicAjaxData(data, "/partner/autoCompleteModifierAjax");
    },
    //binary
    getBatNmTags: function (data) {
        return fnBasicAjaxData(data, "/bat/autoCompleteNmAjax");
    },
    getBatConfNmTags: function (data) {
        return fnBasicAjaxData(data, "/bat/autoCompleteConfNmAjax");
    },
    getBatSwNmTags: function (data) {
        return fnBasicAjaxData(data, "/bat/autoCompleteSwNmAjax");
    },
    getBatSwNmConfTags: function (data) {
        return fnBasicAjaxData(data, "/bat/autoCompleteSwNmAjax?batStatus=60");
    },
    getBatSwVerTags: function (data) {
        return fnBasicAjaxData(data, "/bat/autoCompleteSwVerAjax");
    },
    getBatDivisionTags: function (data) {
        return fnBasicAjaxData(data, "/bat/autoCompleteDivisionAjax");
    },
    //code
    getCodeNoTags: function (data) {
        return fnBasicAjaxData(data, "/system/code/autoCompleteNoAjax");
    },
    getCodeNmTags: function (data) {
        return fnBasicAjaxData(data, "/system/code/autoCompleteNmAjax");
    },
    //Creator
    getCreatorTags: function (data) {
        return fnBasicAjaxData(data, "/system/user/autoCompleteCreatorAjax");
    },
    getReviewerTags: function (data) {
        return fnBasicAjaxData(data, "/system/user/autoCompleteReviewerAjax");
    },
    //Creator & Division
    getCreatorDivisionTags: function (data) {
        return fnBasicAjaxData(data, "/system/user/autoCompleteCreatorDivisionAjax");
    }
};

function fnBasicAjaxData(data, url) {
    return $.ajax({type: 'GET', url: url, data: data, headers: {'Content-Type': 'application/json'}});
}

function xssPreventerUnescape(data) {
    var unescapeData = data;
    if (unescapeData.indexOf("&amp;") > -1) {
        unescapeData = unescapeData.replace("&amp;", "&");
    }
    if (unescapeData.indexOf("&quot;") > -1) {
        unescapeData = unescapeData.replace("&quot;", "\"");
    }
    return unescapeData;
}

var autoComplete = {
    licenseTags: [],
    licenseLongTags: [],
    ossTags: [],
    //project
    projectNameTags: [],
    projectNameConfTags: [],
    projectIdConfTags: [],
    projectVersionTags: [],
    projectModelTags: [],
    //partner
    partyNameTags: [],
    partyConfNameTags: [],
    partyConfIdTags: [],
    softwareNameTags: [],
    softwareConfNameTags: [],
    softwareVersionTags: [],
    softwareConfVersionTags: [],
    //binary
    binaryNameTags: [],
    binaryConfNameTags: [],
    binarySwNameTags: [],
    binarySwNameConfTags: [],
    binarySwVersionTags: [],
    binaryDivisionTags: [],
    //code
    codeNoTags: [],
    codeNmTags: [],
    //user
    creatorTag: [],
    reviewerTag: ['N/A'],
    creatorDivisionTag: [],
    load: function () {
        if ($(".autoComLicense").length > 0) {
            commonAjax.getLicenseTags().success(function (data, status, headers, config) {
                if (data != null) {
                    var tag = "";
                    data.forEach(function (obj) {
                        if (obj != null) {
                            tag = {
                                value: obj.shortIdentifier.length > 0 ? obj.shortIdentifier : obj.licenseName,
                                label: obj.licenseName + (obj.shortIdentifier.length > 0 ? (" (" + obj.shortIdentifier + ")") : ""),
                                type: obj.licenseType,
                                obligation: obj.obligation,
                                obligationChecks: obj.obligationChecks,
                                obligationCode: obj.obligationCode,
                                licenseTypeVal: obj.licenseTypeVal,
                                restriction: obj.restriction
                            }
                            autoComplete.licenseTags.push(tag);
                        }
                    });
                }
            });
        }

        if ($(".autoComLicenseLong").length > 0) {
            commonAjax.getLicenseTags().success(function (data, status, headers, config) {
                if (data != null) {
                    var tag = "";
                    data.forEach(function (obj) {
                        if (obj != null) {
                            tag = {
                                value: obj.licenseName,
                                label: obj.licenseName + (obj.shortIdentifier.length > 0 ? (" (" + obj.shortIdentifier + ")") : ""),
                                type: obj.licenseType,
                                obligation: obj.obligation,
                                obligationChecks: obj.obligationChecks
                            }

                            autoComplete.licenseLongTags.push(tag);
                        }
                    });
                }
            });
        }

        if ($(".autoComOss").length > 0) {
            commonAjax.getOssTags().success(function (data, status, headers, config) {
                if (data != null) {
                    data.forEach(function (obj) {
                        if (obj != null) {
                            autoComplete.ossTags.push(obj.ossName);
                        }
                    })
                }
            });
        }

        if ($('.autoComProjectNm').length > 0) {
            commonAjax.getProjectNameTags().success(function (data, status, headers, config) {
                if (data != null) {
                    data.forEach(function (obj) {
                        if (obj != null) {
                            autoComplete.projectNameTags.push(xssPreventerUnescape(obj.prjName));
                        }
                    })
                }
            });
        }

        if ($('.autoComProjectNmConf').length > 0) {
            commonAjax.getProjectNameConfTags().success(function (data, status, headers, config) {
                if (data != null) {
                    data.forEach(function (obj) {
                        if (obj != null) {
                            autoComplete.projectNameConfTags.push(obj.prjName);
                        }
                    })
                }
            });
        }

        if ($('.autoComProjectVersion').length > 0) {
            commonAjax.getProjectVersionTags().success(function (data, status, headers, config) {
                if (data != null) {
                    data.forEach(function (obj) {
                        if (obj != null) {
                            autoComplete.projectVersionTags.push(obj.prjVersion);
                        }
                    })
                }
            });
        }

        if ($('.autoComProjectIdConf').length > 0) {
            commonAjax.getProjectIdConfTags().success(function (data, status, headers, config) {
                if (data != null) {
                    data.forEach(function (obj) {
                        if (obj != null) {
                            autoComplete.projectIdConfTags.push(obj.prjId);
                        }
                    })
                }
            });
        }

        if ($('.autoComProjectModel').length > 0) {
            commonAjax.getProjectModelTags().success(function (data, status, headers, config) {
                if (data != null) {
                    data.forEach(function (obj) {
                        if (obj != null) {
                            autoComplete.projectModelTags.push(obj.modelName);
                        }
                    })
                }
            });
        }

        if ($('.autoComParty').length > 0) {
            commonAjax.getPartnerNmTags().success(function (data, status, headers, config) {
                if (data != null) {
                    data.forEach(function (obj) {
                        if (obj != null) {
                            autoComplete.partyNameTags.push(obj.partnerName);
                        }
                    })
                }
            });
        }
        if ($('.autoComConfParty').length > 0) {
            commonAjax.getPartnerConfNmTags().success(function (data, status, headers, config) {
                if (data != null) {
                    data.forEach(function (obj) {
                        if (obj != null) {
                            autoComplete.partyConfNameTags.push(obj.partnerName);
                        }
                    })
                }
            });
        }
        if ($('.autoComConfPartyId').length > 0) {
            commonAjax.getPartnerConfIdTags().success(function (data, status, headers, config) {
                if (data != null) {
                    data.forEach(function (obj) {
                        if (obj != null) {
                            autoComplete.partyConfIdTags.push(obj.partnerId);
                        }
                    })
                }
            });
        }
        if ($('.autoComSwNm').length > 0) {
            commonAjax.getPartnerSwNmTags().success(function (data, status, headers, config) {
                if (data != null) {
                    data.forEach(function (obj) {
                        if (obj != null) {
                            autoComplete.softwareNameTags.push(obj.softwareName);
                        }
                    })
                }
            });
        }
        if ($(".autoComConfSwNm").length > 0) {
            commonAjax.getPartnerConfSwNmTags().success(function (data, status, headers, config) {
                if (data != null) {
                    data.forEach(function (obj) {
                        if (obj != null) {
                            autoComplete.softwareConfNameTags.push(obj.softwareName);
                        }
                    })
                }
            });
        }
        if ($('.autoComSwVer').length > 0) {
            commonAjax.getPartnerSwVerTags().success(function (data, status, headers, config) {
                if (data != null) {
                    data.forEach(function (obj) {
                        if (obj != null) {
                            autoComplete.softwareVersionTags.push(obj.softwareVersion);
                        }
                    })
                }
            });
        }
        if ($('.autoComConfSwVer').length > 0) {
            commonAjax.getPartnerConfSwVerTags().success(function (data, status, headers, config) {
                if (data != null) {
                    data.forEach(function (obj) {
                        if (obj != null) {
                            autoComplete.softwareConfVersionTags.push(obj.softwareVersion);
                        }
                    })
                }
            });
        }
        if ($('.autoComBinary').length > 0) {
            commonAjax.getBatNmTags().success(function (data, status, headers, config) {
                if (data != null) {
                    data.forEach(function (obj) {
                        if (obj != null) {
                            autoComplete.binaryNameTags.push(obj.fileName);
                        }
                    })
                }
            });
        }
        if ($('.autoComConfBinary').length > 0) {
            commonAjax.getBatConfNmTags().success(function (data, status, headers, config) {
                if (data != null) {
                    data.forEach(function (obj) {
                        if (obj != null) {
                            autoComplete.binaryConfNameTags.push(obj.fileName);
                        }
                    })
                }
            });
        }
        if ($('.autoComBinarySwName').length > 0) {
            commonAjax.getBatSwNmTags().success(function (data, status, headers, config) {
                if (data != null) {
                    data.forEach(function (obj) {
                        if (obj != null) {
                            autoComplete.binarySwNameTags.push(obj.softwareName);
                        }
                    })
                }
            });
        }
        if ($('.autoComBinarySwNameConf').length > 0) {
            commonAjax.getBatSwNmConfTags().success(function (data, status, headers, config) {
                if (data != null) {
                    data.forEach(function (obj) {
                        if (obj != null) {
                            autoComplete.binarySwNameConfTags.push(obj.softwareName);
                        }
                    })
                }
            });
        }
        if ($('.autoComBinarySwVersion').length > 0) {
            commonAjax.getBatSwVerTags().success(function (data, status, headers, config) {
                if (data != null) {
                    data.forEach(function (obj) {
                        if (obj != null) {
                            autoComplete.binarySwVersionTags.push(obj.softwareVersion);
                        }
                    })
                }
            });
        }
        if ($('.autoComBinaryDivision').length > 0) {
            commonAjax.getBatDivisionTags().success(function (data, status, headers, config) {
                if (data != null) {
                    data.forEach(function (obj) {
                        if (obj != null) {
                            autoComplete.binaryDivisionTags.push(obj.division);
                        }
                    })
                }
            });
        }
        if ($('.autoComCodeNo').length > 0) {
            commonAjax.getCodeNoTags().success(function (data, status, headers, config) {
                if (data != null) {
                    data.forEach(function (obj) {
                        if (obj != null) {
                            autoComplete.codeNoTags.push(obj.cdNo);
                        }
                    })
                }
            });
        }
        if ($('.autoComCodeNm').length > 0) {
            commonAjax.getCodeNmTags().success(function (data, status, headers, config) {
                if (data != null) {
                    data.forEach(function (obj) {
                        if (obj != null) {
                            autoComplete.codeNmTags.push(obj.cdNm);
                        }
                    })
                }
            });
        }
        //user
        if ($('.autoComCreator').length > 0) {
            commonAjax.getCreatorTags().success(function (data, status, headers, config) {
                if (data != null) {
                    data.forEach(function (obj) {
                        if (obj != null) {
                            autoComplete.creatorTag.push(obj.userName);
                        }
                    })
                }
            });
        }
        if ($('.autoComReviewer').length > 0) {
            commonAjax.getReviewerTags().success(function (data, status, headers, config) {
                if (data != null) {
                    data.forEach(function (obj) {
                        if (obj != null) {
                            autoComplete.reviewerTag.push(obj.userName);
                        }
                    })
                }
            });
        }
        if ($(".autoComCreatorDivision").length > 0) {
            commonAjax.getCreatorDivisionTags().success(function (data, status, headers, config) {
                if (data != null) {
                    var tag = "";
                    data.forEach(function (obj) {
                        if (obj != null) {
                            tag = {
                                value: obj.userName,
                                label: obj.userName,
                                division: obj.division,
                                id: obj.userId
                            }

                            autoComplete.creatorDivisionTag.push(tag);
                        }
                    });
                }
            });
        }
    },
    init: function () {
        $(".autoComLicense").autocomplete({
            source: autoComplete.licenseTags, minLength: 0, //delay: 500,
            open: function () {
                $(this).attr('state', 'open');
            }, close: function () {
                $(this).attr('state', 'closed');
            }
        })
            .focus(function () {
                if ($(this).attr('state') != 'open') {
                    $(this).autocomplete("search");
                }
            })
            .autocomplete("instance")._renderItem = function (ul, item) {
            return $("<li>").append("<div>" + item.label + "<strong> (" + item.type + ") </strong>" + item.obligation + item.restriction + "</div>").appendTo(ul);
        };

        //hklee 2016 11 14
        $(".autoComLicenseLong").autocomplete({
            source: autoComplete.licenseLongTags, minLength: 0, //delay: 500,
            open: function () {
                $(this).attr('state', 'open');
            }, close: function () {
                $(this).attr('state', 'closed');
            }
        })
            .focus(function () {
                if ($(this).attr('state') != 'open') {
                    $(this).autocomplete("search");
                }
            })
            .autocomplete("instance")._renderItem = function (ul, item) {
            return $("<li>").append("<div>" + item.label + "<strong> (" + item.type + ") </strong>" + item.obligation + "</div>").appendTo(ul);
        };

        $(".autoComOss").autocomplete({
            source: autoComplete.ossTags, minLength: 3, open: function () {
                $(this).attr('state', 'open');
            }, close: function () {
                $(this).attr('state', 'closed');
            }
        })
            .focus(function () {
                if ($(this).attr('state') != 'open') {
                    $(this).autocomplete("search");
                }
            });

        $(".autoComProjectNm").autocomplete({
            source: autoComplete.projectNameTags, minLength: 0, open: function () {
                $(this).attr('state', 'open');
            }, close: function () {
                $(this).attr('state', 'closed');
            }
        })
            .focus(function () {
                if ($(this).attr('state') != 'open') {
                    $(this).autocomplete("search");
                }
            });

        $(".autoComProjectNmConf").autocomplete({
            source: autoComplete.projectNameConfTags,
            minLength: 0,
            open: function () {
                $(this).attr('state', 'open');
            },
            close: function () {
                $(this).attr('state', 'closed');
            }
        })
            .focus(function () {
                if ($(this).attr('state') != 'open') {
                    $(this).autocomplete("search");
                }
            });

        $(".autoComProjectIdConf").autocomplete({
            source: autoComplete.projectIdConfTags,
            minLength: 0,
            open: function () {
                $(this).attr('state', 'open');
            },
            close: function () {
                $(this).attr('state', 'closed');
            }
        })
            .focus(function () {
                if ($(this).attr('state') != 'open') {
                    $(this).autocomplete("search");
                }
            });

        $(".autoComProjectVersion").autocomplete({
            source: autoComplete.projectVersionTags,
            minLength: 0,
            open: function () {
                $(this).attr('state', 'open');
            },
            close: function () {
                $(this).attr('state', 'closed');
            }
        })
            .focus(function () {
                if ($(this).attr('state') != 'open') {
                    $(this).autocomplete("search");
                }
            });

        $(".autoComProjectModel").autocomplete({
            source: autoComplete.projectModelTags, minLength: 3, open: function () {
                $(this).attr('state', 'open');
            }, close: function () {
                $(this).attr('state', 'closed');
            }
        })
            .focus(function () {
                if ($(this).attr('state') != 'open') {
                    $(this).autocomplete("search");
                }
            });

        $(".autoComParty").autocomplete({
            source: autoComplete.partyNameTags, minLength: 0, open: function () {
                $(this).attr('state', 'open');
            }, close: function () {
                $(this).attr('state', 'closed');
            }
        })
            .focus(function () {
                if ($(this).attr('state') != 'open') {
                    $(this).autocomplete("search");
                }
            });

        $(".autoComConfParty").autocomplete({
            source: autoComplete.partyConfNameTags, minLength: 0, open: function () {
                $(this).attr('state', 'open');
            }, close: function () {
                $(this).attr('state', 'closed');
            }
        })
            .focus(function () {
                if ($(this).attr('state') != 'open') {
                    $(this).autocomplete("search");
                }
            });

        $(".autoComConfPartyId").autocomplete({
            source: autoComplete.partyConfIdTags, minLength: 0, open: function () {
                $(this).attr('state', 'open');
            }, close: function () {
                $(this).attr('state', 'closed');
            }
        })
            .focus(function () {
                if ($(this).attr('state') != 'open') {
                    $(this).autocomplete("search");
                }
            });

        $(".autoComSwNm").autocomplete({
            source: autoComplete.softwareNameTags, minLength: 0, open: function () {
                $(this).attr('state', 'open');
            }, close: function () {
                $(this).attr('state', 'closed');
            }
        })
            .focus(function () {
                if ($(this).attr('state') != 'open') {
                    $(this).autocomplete("search");
                }
            });

        $(".autoComConfSwNm").autocomplete({
            source: autoComplete.softwareConfNameTags, minLength: 0, open: function () {
                $(this).attr('state', 'open');
            }, close: function () {
                $(this).attr('state', 'closed');
            }
        })
            .focus(function () {
                if ($(this).attr('state') != 'open') {
                    $(this).autocomplete("search");
                }
            });

        $(".autoComSwVer").autocomplete({
            source: autoComplete.softwareVersionTags, minLength: 0, open: function () {
                $(this).attr('state', 'open');
            }, close: function () {
                $(this).attr('state', 'closed');
            }
        })
            .focus(function () {
                if ($(this).attr('state') != 'open') {
                    $(this).autocomplete("search");
                }
            });

        $(".autoComConfSwVer").autocomplete({
            source: autoComplete.softwareConfVersionTags,
            minLength: 0,
            open: function () {
                $(this).attr('state', 'open');
            },
            close: function () {
                $(this).attr('state', 'closed');
            }
        })
            .focus(function () {
                if ($(this).attr('state') != 'open') {
                    $(this).autocomplete("search");
                }
            });

        $(".autoComBinary").autocomplete({
            source: autoComplete.binaryNameTags, minLength: 0, open: function () {
                $(this).attr('state', 'open');
            }, close: function () {
                $(this).attr('state', 'closed');
            }
        })
            .focus(function () {
                if ($(this).attr('state') != 'open') {
                    $(this).autocomplete("search");
                }
            });

        $(".autoComConfBinary").autocomplete({
            source: autoComplete.binaryConfNameTags, minLength: 0, open: function () {
                $(this).attr('state', 'open');
            }, close: function () {
                $(this).attr('state', 'closed');
            }
        })
            .focus(function () {
                if ($(this).attr('state') != 'open') {
                    $(this).autocomplete("search");
                }
            });

        $(".autoComBinarySwName").autocomplete({
            source: autoComplete.binarySwNameTags, minLength: 0, open: function () {
                $(this).attr('state', 'open');
            }, close: function () {
                $(this).attr('state', 'closed');
            }
        })
            .focus(function () {
                if ($(this).attr('state') != 'open') {
                    $(this).autocomplete("search");
                }
            });

        $(".autoComBinarySwVersion").autocomplete({
            source: autoComplete.binarySwVersionTags,
            minLength: 0,
            open: function () {
                $(this).attr('state', 'open');
            },
            close: function () {
                $(this).attr('state', 'closed');
            }
        })
            .focus(function () {
                if ($(this).attr('state') != 'open') {
                    $(this).autocomplete("search");
                }
            });

        $(".autoComBinaryDivision").autocomplete({
            source: autoComplete.binaryDivisionTags,
            minLength: 0,
            open: function () {
                $(this).attr('state', 'open');
            },
            close: function () {
                $(this).attr('state', 'closed');
            }
        })
            .focus(function () {
                if ($(this).attr('state') != 'open') {
                    $(this).autocomplete("search");
                }
            });

        $(".autoComCodeNo").autocomplete({
            source: autoComplete.codeNoTags, minLength: 0, open: function () {
                $(this).attr('state', 'open');
            }, close: function () {
                $(this).attr('state', 'closed');
            }
        })
            .focus(function () {
                if ($(this).attr('state') != 'open') {
                    $(this).autocomplete("search");
                }
            });

        $(".autoComCodeNm").autocomplete({
            source: autoComplete.codeNmTags, minLength: 0, open: function () {
                $(this).attr('state', 'open');
            }, close: function () {
                $(this).attr('state', 'closed');
            }
        })
            .focus(function () {
                if ($(this).attr('state') != 'open') {
                    $(this).autocomplete("search");
                }
            });

        $(".autoComCreator").autocomplete({
            source: autoComplete.creatorTag, minLength: 0, open: function () {
                $(this).attr('state', 'open');
            }, close: function () {
                $(this).attr('state', 'closed');
            }
        })
            .focus(function () {
                if ($(this).attr('state') != 'open') {
                    $(this).autocomplete("search");
                }
            });

        $(".autoComReviewer").autocomplete({
            source: autoComplete.reviewerTag, minLength: 0, open: function () {
                $(this).attr('state', 'open');
            }, close: function () {
                $(this).attr('state', 'closed');
            }
        })
            .focus(function () {
                if ($(this).attr('state') != 'open') {
                    $(this).autocomplete("search");
                }
                if ($(".ui-autocomplete").is(':visible')) {
                    $(".ui-autocomplete").css("width", parseInt($(this).css("width")) + 20);
                }
            });

        $(".autoComCreatorDivision").autocomplete({
            source: autoComplete.creatorDivisionTag, minLength: 0,
            open: function () {
                $(this).attr('state', 'open');
            }, close: function () {
                $(this).attr('state', 'closed');
                if ($(this).parent().find('input[name=creatorNm]').val() == "") {
                    $(this).parent().find('input[name=creator]').val('');
                }
            },
            select: function (event, ui) {
                $(this).parent().find('input[name=creator]').val(ui.item.id);
            }
        })
            .focus(function () {
                if ($(this).attr('state') != 'open') {
                    $(this).autocomplete("search");
                }
            })
            .autocomplete("instance")._renderItem = function (ul, item) {
            if (item.division) {
                return $("<li>").append("<div>" + item.division + ' > ' + item.label + "(" + item.id + ")" + "</div>").appendTo(ul);
            } else {
                return $("<li>").append("<div>" + item.label + "(" + item.id + ")" + "</div>").appendTo(ul);
            }

        };
        
        if ($("#accordionBtn").length) {
			$("#accordionBtn").on("click", function() { $(this).toggleClass("active"); });
		}
    }
}

// 날짜 유효성검사 및 포맷변환
function validationDate() {
    var days = [31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31];

    $(".cal").each(function (i, v) {
        if (v.value != "") {
            var value = v.value;
            var pattenDate = /\d{4}-\d{2}-\d{2}/g; // 포맷 검사 패턴 0000.00.00
            var pattenDot = /\./g; // . 유무 검사

            if (!pattenDate.test(value)) {

                value = value.replace(/\.+/g, ".");

                var matchDot = new Array();
                var temp = ""

                for (var i = 0; (matchDot[i] = pattenDot.exec(value)) != null; i++) ;

                var matchLen = matchDot.length - 1;

                if (matchLen != null && matchLen > 0 && matchLen <= 2) {
                    if (matchLen == 1) {
                        var year = value.substring(0, matchDot[0].index);
                        var month = value.substring(matchDot[0].index + 1, (value.length - matchDot[0].index) == 1 ? matchDot[0].index + 2 : matchDot[0].index + 3);

                        if (month > 0 && month < 13) {
                            var tempDate = value.substring(0, 4) + "." + (month.length == 1 ? "0" : "") + month;

                            v.value = tempDate;
                        } else {
                            return false;
                        }
                    } else if (matchLen == 2) {
                        var year = value.substring(0, matchDot[0].index);
                        var month = value.substring(matchDot[0].index + 1, matchDot[1].index);
                        var day = value.substring(matchDot[1].index + 1, (value.length - matchDot[1].index) == 1 ? matchDot[1].index + 2 : matchDot[1].index + 3)

                        if (month > 0 && month <= 12) {
                            var maxDay = days[parseInt(month) - 1];

                            if (month == '02') {
                                if (parseInt(year) % 4 == 0) {
                                    maxDay += 1;
                                }
                            } // 윤년 2월일경우 일수를 29일로

                            if (day > 0 && day <= maxDay) {
                                var tempDate = year + "." + (month.length == 1 ? "0" : "") + month + "." + (day.length == 1 ? "0" : "") + day;
                                v.value = tempDate;
                            } else {
                                return false;
                            }
                        } else {
                            return false;
                        }
                    }
                } else {
                    value = value.replace(/\./gi, ''); // . 제거
                    var len = value.length;

                    if (len == 4) {
                        v.value = value;
                    } else if (len == 5) {
                        var tempDate = value.substring(0, 4) + ".0" + value.substring(4, 5);

                        v.value = tempDate;
                    } else if (len == 6) {
                        var month = value.substring(4, 6);

                        if (month > 0 && month < 13) {
                            var tempDate = value.substring(0, 4) + "." + month;

                            v.value = tempDate;
                        } else {
                            return false;
                        }
                    } else if (len == 8) {
                        var year = value.substring(0, 4);
                        var month = value.substring(4, 6);
                        var day = value.substring(6, 8);

                        if (month > 0 && month <= 12) {
                            var maxDay = days[parseInt(month) - 1];

                            if (month == '02') {
                                if (parseInt(year) % 4 == 0) {
                                    maxDay += 1;
                                }
                            } // 윤년 2월일경우 일수를 29일로

                            if (day > 0 && day <= maxDay) {
                                var tempDate = year + "." + month + "." + day;

                                v.value = tempDate;
                            } else {
                                return false;
                            }
                        } else {
                            return false;
                        }
                    } else {
                        return false;
                    }
                }
            } // //패턴검사
        } // //입력값공백
    }); // //반복

    return true;
}

// 화면갱신
function viewRefresh() {
    if ($('.tabMenu ul').height() != 0) {
        // tab 이동시 스크롤 없어지는 에러 처리
        // 컨텐츠 크기를 강제로 변경하여 스크롤이 생기도록 처리함
        var val = $('.tabMenu ul').height() / 30;

        var agt = navigator.userAgent.toLowerCase();

        if (agt.indexOf("chrome") > -1) {
            $('.contents').css('top', (((42 * val) - (parseInt(val) * 10.5)) - (0.9 / (parseInt(val) + 1))) + 'px');
        }

        setTimeout(function () {
            var cHeight = $('.nav-tab-menu').height();
            $('.contentsFixBack').height(cHeight + 9);
            $('.contents').css('top', ((42 * val) - (parseInt(val) * 10.5)) + 'px');
        }, 10);
    }
}

// 브라우저 창 크기에 따라 jqGrid Width 자동 조절
function tableRefresh() {
    $('.ui-jqgrid-btable').each(function () {
        var id = $(this).attr('id');
		
        if (id.indexOf('_') == -1) {
			$(this).jqGrid('setGridWidth', 0, true);
            $(this).jqGrid('setGridWidth', $(".wrapper").width() - 20, true);
        }
    });
}

var contentsIds = ["_list", "_list2", "_list-1", "_list-2", "_3rdAddList", "_depProjectList1", "_depProjectList2", "_depAddList", "_srcProjectList1", "_srcProjectList2", "_srcAddList"
	, "_binProjectList1", "_binProjectList2", "_binAddList", "_binAndroidProjectList1", "_binAndroidProjectList2", "_binaryFileList"
];
function tableRefreshContentsArea () {
	var width = $(".wrapper").width();
	
	$('.ui-jqgrid-btable').each(function () {
        var id = $(this).attr('id');
		
        if (contentsIds.includes(id)) {
			$(this).jqGrid('setGridWidth', 0, true);
			if (typeof editMode !== "undefined") {
				if ("Y" != editMode) {
					$(this).jqGrid('setGridWidth', width - 925, true);
				} else {
					$(this).jqGrid('setGridWidth', width - 70, true);
				}
			}
        }
    });
}

function tableRefreshEditMode() {
	var width = $(".wrapper").find(".contents-area").width() - 115;
	
	setTimeout(function () {
		$('.ui-jqgrid-btable').each(function () {
        	var id = $(this).attr('id');
		
        	if (contentsIds.includes(id)) {
				$(this).jqGrid('setGridWidth', 0, true);
				$(this).jqGrid('setGridWidth', width, true);
        	}
    	});
	}, 200);
}

function tableRefreshCommentArea (flag, targetId, editMode) {
	var width = $(".wrapper .contents-area").width();
	if ("entire" == flag) {
        if ("Y" == editMode) {
            width = width - 90;
        } else {
            width = width - 430;
        }
    } else {
        if ("Y" == editMode) {
            width = width - 115;
        } else {
            width = width - 320;
        }
    }
    
	setTimeout(function () {
		$('.ui-jqgrid-btable').each(function () {
        	var id = $(this).attr('id');
		
        	if (id == targetId) {
				$(this).jqGrid('setGridWidth', 0, true);
				$(this).jqGrid('setGridWidth', width, true);
        	}
    	});
	}, 200);
}

function tableRefreshContentsAreaEdit (target) {
	var width = $(".wrapper").find(".contents-area").width() - 20;
	
	$('.ui-jqgrid-btable').each(function () {
        var id = $(this).attr('id');
		
        if (id == target) {
			$(this).jqGrid('setGridWidth', 0, true);
			$(this).jqGrid('setGridWidth', width, true);
        }
    });
}

var gridIds = ["list", "list3", "depList", "srcList", "binList", "binAndroidList", "bomList", "totalList", "fixedList", "notFixedList"];
function tableRefreshGridArea () {
	var width = $(".wrapper").find(".contents-area").width() - 20;
	
	$('.ui-jqgrid-btable').each(function () {
        var id = $(this).attr('id');
		
		if (gridIds.includes(id)) {
			$(this).jqGrid('setGridWidth', 0, true);
			$(this).jqGrid('setGridWidth', width, true);
        }
    });
}

function tableRefreshGridArea (flag) {
	var width = $(".wrapper").find(".contents-area").width() - 20;
	
	$('.ui-jqgrid-btable').each(function () {
        var id = $(this).attr('id');
		
		if (gridIds.includes(id)) {
			$(this).jqGrid('setGridWidth', 0, true);
			$(this).jqGrid('setGridWidth', width, true);
        }
    });
}

function tableRefreshAlert() {
	$('.ui-jqgrid-btable').each(function () {
        var id = $(this).attr('id');
		
        if ("_lastStepList" == id) {
			$(this).jqGrid('setGridWidth', 0, true);
			$(this).jqGrid('setGridWidth', 800, true);
        }
    });
}

// UI BLOCK FOR ELEMENT
function uiBlock(objArr, bln) {
    // default
    $.blockUI.defaults.overlayCSS.backgroundColor = '#ffffff';
    $.blockUI.defaults.overlayCSS.opacity = 0.1;
    $.blockUI.defaults.overlayCSS.cursor = 'default';

    // bln : Block or unBlock
    for (var o in objArr) {
        if (bln) {
            objArr[o].closest(".ui-jqgrid").block({
                message: null
            });
        } else {
            objArr[o].closest(".ui-jqgrid").unblock();
        }
    }
}

function openNVD(cveId) {
    if (typeof cveId == "undefined" || cveId == undefined || cveId.trim() == "undefined") {
        return false;
    }

    if (cveId != "") {
        window.open("https://web.nvd.nist.gov/view/vuln/detail?vulnId=" + cveId.trim(), "_blank");
    }
}

function openNVD2(_ossName, _url) {
    if (_popupVuln == null || _popupVuln.closed) {
        _popupVuln = window.open(_url, "vulnViewPopup_" + _ossName, "width=900, height=600, toolbar=no, location=no, left=100, top=100, scrollbars=yes, resizeable=yes");

        if (!_popupVuln || _popupVuln.closed || typeof _popupVuln.closed == 'undefined') {
            alertify.alert('<spring:message code="msg.common.window.allowpopup" />', function () {
            });
        }
    } else {
        _popupVuln.close();

        _popupVuln = window.open(_url, "vulnViewPopup_" + _ossName, "width=900, height=600, toolbar=no, location=no, left=100, top=100, scrollbars=yes, resizeable=yes");
    }
}

function openCommentHistory(_url) {
    if (_popupComment == null || _popupComment.closed) {
        _popupComment = window.open(_url, "commentPopup", "width=900, height=600, toolbar=no, location=no, left=100, top=100, scrollbars=yes, resizeable=yes");

        if (!_popupComment || _popupComment.closed || typeof _popupComment.closed == 'undefined') {
            alertify.alert('[[ #{msg.common.window.allowpopup} ]]', function () {
            });
        }
    } else {
        _popupComment.close();

        _popupComment = window.open(_url, "commentPopup", "width=900, height=600, toolbar=no, location=no, left=100, top=100, scrollbars=yes, resizeable=yes");
    }
}

function gridValidMsgChk(rowId, idName, MsgData, target) {
    var chkMsgData = new Object();
    var chkKey = idName + "." + rowId;

    $.each(MsgData, function (key, value) {
        if (chkKey != key) {
            chkMsgData[key] = value;
        }
    });

    if (target == "srcList") {
        window.srcValidMsgData = chkMsgData;
    } else if (target == "binAndroidList") {
        window.binAndroidValidMsgData = chkMsgData;
    } else if (target == "binList") {
        window.binValidMsgData = chkMsgData;
    } else if (target == "batList") {
        window.batValidMsgData = chkMsgData;
    } else if (target == "list") {
        window.partyValidMsgData_e = chkMsgData;
    } else if (target == "batList_e") {
        window.batValidMsgData_e = chkMsgData;
    }
}

function gridDiffMsgChk(rowId, idName, MsgData, target) {
    var chkMsgData = new Object();
    var chkKey = idName + "." + rowId;

    $.each(MsgData, function (key, value) {
        if (chkKey != key) {
            chkMsgData[key] = value;
        }
    });

    if (target == "srcList") {
        window.srcDiffMsgData = chkMsgData;
    } else if (target == "binAndroidList") {
        window.binAndroidDiffMsgData = chkMsgData;
    } else if (target == "binList") {
        window.binDiffMsgData = chkMsgData;
    } else if (target == "batList") {
        window.batDiffMsgData = chkMsgData;
    } else if (target == "list") {
        window.partyDiffMsgData_e = chkMsgData;
    } else if (target == "batList_e") {
        window.batDiffMsgData_e = chkMsgData;
    }
}


function gridInfoMsgChk(rowId, idName, MsgData, target) {
    var chkMsgData = new Object();
    var chkKey = idName + "." + rowId;

    $.each(MsgData, function (key, value) {
        if (chkKey != key) {
            chkMsgData[key] = value;
        }
    });

    if (target == "binAndroidList") {
        window.binAndroidInfoMsgData = chkMsgData;
    } else if (target == "binList") {
        window.binInfoMsgData = chkMsgData;
    }
}

function gridValidMsgRowId(msgData, gridStr, selRowId) {
    if (msgData) {
        $.each(msgData, function (key, value) {
            var seqSuffix = key.split(".");

            if (seqSuffix.length > 1) {
                var rowId = seqSuffix[1];
                var errRow = "";

                if (selRowId == rowId) {
                    errRow = $("#" + rowId + " > td[aria-describedby='" + gridStr + "_" + seqSuffix[0] + "']");
                }

                if (errRow) {
                    errRow.append('<div class=\"' + gridStr + "_" + rowId + ' retxt\">' + value + '</div>');
                }
            }
        });
    }
}

// diff
function gridDiffMsgRowId(msgData, gridStr, selRowId) {
    if (msgData) {
        $.each(msgData, function (key, value) {
            var seqSuffix = key.split(".");

            if (seqSuffix.length > 1) {
                var rowId = seqSuffix[1];
                var errRow = "";

                if (selRowId == rowId) {
                    errRow = $("#" + rowId + " > td[aria-describedby='" + gridStr + "_" + seqSuffix[0] + "']");
                }

                if (errRow) {
                    errRow.append('<div class=\"' + gridStr + "_" + rowId + ' retxtb\">' + value + '</div>');
                }
            }
        });
    }
}

//diff
function gridDiffMsg(msgData, gridStr, type) {
    type = type || "NORMAL";
    var target = $("#" + gridStr);
    var mainIds = target.jqGrid('getDataIDs');

    if (msgData) {
        $.each(msgData, function (key, value) {
            if ("isValid" != key && "validMsg" != key && "resultData" != key && "externalData" != key && "externalData2" != key && "externalData3" != key) {
                var seqSuffix = key.split(".");

                if (seqSuffix.length > 1) {
                    var rowId = seqSuffix[1];
                    var diffRow;

                    if (type == "NORMAL") {
                        if (rowId.indexOf("-") > -1) {

                        } else {
                            diffRow = $("#" + rowId + " > td[aria-describedby='" + gridStr + "_" + seqSuffix[0] + "']");
                        }
                    }

                    if (type == "SELF") {
                        diffRow = $("#" + rowId + " > td[aria-describedby='" + gridStr + "_" + seqSuffix[0] + "']");
                    }

                    // 그리드 메세지 그리기
                    if (diffRow) {
                        diffRow.append('<div class=\"' + gridStr + "_" + rowId + ' retxtb\">' + value + '</div>');
                    }
                }
            }
        });
    }
}

// info message
function gridInfoMsgRowId(msgData, gridStr, selRowId) {
    if (msgData) {
        $.each(msgData, function (key, value) {
            var seqSuffix = key.split(".");

            if (seqSuffix.length > 1) {
                var rowId = seqSuffix[1];
                var errRow = "";

                if (selRowId == rowId) {
                    errRow = $("#" + rowId + " > td[aria-describedby='" + gridStr + "_" + seqSuffix[0] + "']");
                }

                if (errRow) {
                    errRow.append('<div class=\"' + gridStr + "_" + rowId + ' retxtg\">' + value + '</div>');
                }
            }
        });
    }
}

function gridInfoMsg(msgData, gridStr) {
    var target = $("#" + gridStr);
    var mainIds = target.jqGrid('getDataIDs');

    $.each(msgData, function (key, value) {
        if ("isValid" != key && "validMsg" != key && "resultData" != key && "externalData" != key && "externalData2" != key && "externalData3" != key) {
            var seqSuffix = key.split(".");

            if (seqSuffix.length > 1) {

                var rowId = seqSuffix[1];
                var diffRow;

                if (rowId.indexOf("-") > -1) {
                } else {
                    diffRow = $("#" + rowId + " > td[aria-describedby='" + gridStr + "_" + seqSuffix[0] + "']");
                }

                // 그리드 메세지 그리기
                if (diffRow) {
                    diffRow.append('<div class=\"' + gridStr + "_" + rowId + ' retxtg\">' + value + '</div>');
                }
            }
        }
    });
}

var getTabIndex = function (tabNm) {
    var $id = $('#nav-tabs', parent.document); // parent.document도 가능
    var index = "";

    $id.find('.nav-tab-menu li span').each(function (i) {
        if ($(this).text() == tabNm) {
            index = i - 1;
        }
    });

    return index;
}

var checkAll = function (targetDiv, taget) {
    var classObj = $("." + targetDiv).find(".sheetNum");

    if ($(taget).is(":checked")) {
        classObj.prop("checked", true);
    } else {
        classObj.prop("checked", false);
    }

    classObj.change(function () {
        var cnt = 0;
        classObj.each(function (idx) {
            if (idx != 0 && $(this).is(":checked")) {
                cnt++;
            }
        });

        if (cnt == classObj.length - 1) {
            $(taget).prop("checked", true);
        } else {
            $(taget).prop("checked", false);
        }
    });
}

function LPAD(s, c, n) {
    if (!s || !c || s.length >= n) {
        return s;
    }

    var max = (n - s.length) / c.length;

    for (var i = 0; i < max; i++) {
        s = c + s;
    }

    return s;
}

function RPAD(s, c, n) {
    if (!s || !c || s.length >= n) {
        return s;
    }

    var max = (n - s.length) / c.length;

    for (var i = 0; i < max; i++) {
        s += c;
    }

    return s;
}

function getFormData($form) {
    var unindexed_array = $form.serializeArray();
    var indexed_array = {};

    $.map(unindexed_array, function (n, i) {
        indexed_array[n['name']] = n['value'];
    });

    return indexed_array;
}

function getCookie(cname) {
    var name = cname + "=";
    var ca = document.cookie.split(';');

    for (var i = 0; i < ca.length; i++) {
        var c = ca[i];

        while (c.charAt(0) == ' ') {
            c = c.substring(1);
        }

        if (c.indexOf(name) != -1) {
            return c.substring(name.length, c.length);
        }
    }

    return "";
}

function setCookie(cname, cvalue, exdays) {
    var d = new Date();
    d.setTime(d.getTime() + (exdays * 24 * 60 * 60 * 1000));
    var expires = "expires=" + d.toUTCString();

    document.cookie = cname + "=" + cvalue + "; " + expires;
}

function showHelpLink(id, target) {
    var _target = "helpLink";

    if (target && target.length > 0) {
        _target = target;
    }

    var _showItem = $('#' + _target);

    if (_showItem && _showItem.length > 0) {
        $.ajax({
            type: 'GET',
            url: "/system/processGuide/getProcessGuide",
            data: {"id": id},
            async: false,
            success: function (data) {
                if (data.processGuide) {
                    var contents = data.processGuide.contents;

                    if (data.processGuide.useYn == "Y") {
                        _showItem.show();

                        if (contents && contents.trim()) {
                            _showItem.attr("title", contents).tooltip({
                                content: function () {
                                    return $(this).prop('title');
                                }
                            });
                        }

                        if (data.processGuide.url && data.processGuide.url.trim()) {
                            _showItem.attr("href", data.processGuide.url);
                            _showItem.attr("target", "_blank");
                        }
                    } else {
                        _showItem.hide();
                    }
                }
            }
        });
    }
}

// 2018.6.29 코멘트 팝업에서 부모창에 각 스테이지 별 tab 생성
function moveTabInFrameByCommentPopup(prjId, stage) {
    var tabIdx = "";
    var urlTxt = "";

    if (stage == "basicInfoPrj") {
        tabIdx = prjId + "_Project";
        urlTxt = '#/project/edit/' + prjId;
    } else if (stage == "identification") {
        tabIdx = prjId + "_Identify";
        urlTxt = '#/project/identification/' + prjId + '/5';
    } else if (stage == "packaging") {
        tabIdx = prjId + "_Packaging";
        urlTxt = '#/project/verification/' + prjId;
    } else if (stage == "distribution") {
        tabIdx = prjId + "_Distribute";
        urlTxt = '#/project/distribution/' + prjId;
    } else if (stage == "basicInfo3rd") {
        tabIdx = prjId + "_3rdParty";
        urlTxt = '#/partner/edit/' + prjId;
    }

    var idx = getTabIndex(tabIdx);

    if (idx != "") {
        changeTabInFrame(idx);
    } else {
        createTabInFrame(tabIdx, urlTxt);
    }
}

function moveTabInFrameByCommentPopup2(prjId, stage) {
    let tabIdx = "";
    let urlTxt = "";
    let uniqueName = "";

    if (stage == "basicInfoPrj") {
        tabIdx = prjId + "_Project";
        urlTxt = '/project/edit/' + prjId;
        uniqueName = "pjt-edit" + prjId;
    } else if (stage == "identification") {
        tabIdx = prjId + "_Identify";
        urlTxt = '/project/identification/' + prjId + '/5';
        uniqueName = "pjt-identification" + prjId;
    } else if (stage == "packaging") {
        tabIdx = prjId + "_Packaging";
        urlTxt = '/project/verification/' + prjId;
        uniqueName = "pjt-packaging" + prjId;
    } else if (stage == "distribution") {
        tabIdx = prjId + "_Distribute";
        urlTxt = '/project/distribution/' + prjId;
        uniqueName = "pjt-distribution" + prjId;
    } else if (stage == "basicInfo3rd") {
        tabIdx = prjId + "_3rdParty";
        urlTxt = '/partner/edit/' + prjId;
        uniqueName = "pjg-basicInfo3rd" + prjId;
    }

    callCreateTabInFrame(tabIdx, urlTxt, uniqueName, true);
}

function calValidation(target, e) {
    var result = $(target).val();

    if (/\d+/.test(result)) {
        result = result.match(/\d+/g).join("");

        $(target).val(result);
    } else {
        $(target).val("");
    }
}

function isMaximumRowCheck(totalRow) {
    if (totalRow > +gRowCnt) {
        alertify.error(getMsgMaxRowCnt(), 0);

        return false;
    }

    return true;
}

function setMaxRowCnt(maxRowCnt) {
    if (!/.+/.test(gRowCnt)) {
        gRowCnt = maxRowCnt;
    }
}

function getMsgMaxRowCnt() {
    var msgGRowCnt = gRowCnt.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",");

    return "More than " + msgGRowCnt + " can not be exported.";
}

function getBarChart(obj) {
    var tooltip = obj.tooltip;

    if (typeof obj.tooltip != "object") {
        tooltip = {
            pointFormat: '<span style="color:{series.color}">{series.name}</span>: <b>{point.y}</b> ({point.percentage:.0f}%)<br/>',
            shared: true
        };
    }
    ;

    return Highcharts.chart(obj.chartId, {
        chart: {
            type: 'column'
        },
        title: {
            text: ''
        },
        xAxis: {
            categories: obj.categoryList
        },
        yAxis: {
            allowDecimals: false,
            min: 0,
            title: {
                text: ''
            }
        },
        legend: {
            enabled: obj.legend
        },
        tooltip: tooltip,
        plotOptions: {
            column: {
                stacking: 'normal'
            }
        },
        series: obj.chartData
    });
}

function getPieChart(obj) {
    return Highcharts.chart(obj.chartId, {
        chart: {
            plotBackgroundColor: null,
            plotBorderWidth: null,
            plotShadow: false,
            type: 'pie'
        },
        title: {
            text: ''
        },
        tooltip: {
            formatter: function () {
                return this.point.name + '</br>Count: ' + this.point.y + ' (' + Math.round(this.point.percentage) + '%)';
            }
        },
        plotOptions: {
            pie: {
                allowPointSelect: true,
                cursor: 'pointer',
                dataLabels: {
                    enabled: true,
                    distance: 20,
                    format: '<b>{point.name}</b>: {point.y}'
                }
            }
        },
        series: [{
            name: 'Count',
            colorByPoint: true,
            data: obj.chartData
        }]
    });
}

$(document).ready(function () {
    //alertify Default 설정
    alertify.defaults = {
        // dialogs defaults
        autoReset: true,
        basic: false,
        closable: true,
        closableByDimmer: false,
        frameless: false,
        maintainFocus: false, // <== global default not per instance, applies to all dialogs
        maximizable: true,
        modal: true,
        movable: true,
        moveBounded: false,
        overflow: true,
        padding: true,
        pinnable: true,
        pinned: true,
        preventBodyShift: false, // <== global default not per instance, applies to all dialogs
        resizable: true,
        startMaximized: false,
        transition: 'fade',

        // notifier defaults
        notifier: {
            // auto-dismiss wait time (in seconds)
            delay: 3,
            // default position
            position: 'bottom-right'
        },

        // language resources
        glossary: {
            // dialogs default title
            title: 'FOSSLight Hub',
            // ok button text
            ok: 'OK',
            // cancel button text
            cancel: 'Cancel'
        },

        // theme settings
        theme: {
            // class name attached to prompt dialog input textbox.
            input: 'ajs-input',
            // class name attached to ok button
            ok: 'ajs-ok',
            // class name attached to cancel button
            cancel: 'ajs-cancel'
        }
    };
});

/**
 * 문자열이 빈 문자열인지 체크하여 결과값을 리턴한다.
 * @param str       : 체크할 문자열
 */
function isEmpty(str) {

    if (typeof str == "undefined" || str == null || str == "")
        return true;
    else
        return false;
}

/**
 * 문자열이 빈 문자열인지 체크하여 기본 문자열로 리턴한다.
 * @param str           : 체크할 문자열
 * @param defaultStr    : 문자열이 비어있을경우 리턴할 기본 문자열
 */
function nvl(str, defaultStr) {

    if (typeof str == "undefined" || str == null || str == "")
        str = defaultStr;

    return str;
}

//2018-07-31 choye 추가
var searchStringOptions = {searchoptions: {sopt: ['cn', 'eq', 'ne', 'bw', 'bn', 'ew', 'en', 'nc']}};
var searchNumberOptions = {searchoptions: {sopt: ['ge', 'le', 'gt', 'lt', 'eq']}};
var searchDateOptions = {searchoptions: {sopt: ['eq', 'lt', 'le', 'gt', 'ge']}};


function replaceWithLink(text) {
    return text.replace(LINKREGEXP, findAndReplace);
}

function findAndReplace(match) {
    var prj = /PRJ/i;
    var third = /3rd/i;
    var arrLink = match.split('-');
    var id = arrLink[1];
    var protocol = window.location.protocol;
    var host = window.location.host;
    var url = protocol + "//" + host;
    if (prj.test(match)) {
        url += "/project/shareUrl/" + id;
    } else if (third.test(match)) {
        url += "/partner/shareUrl/" + id;
    }
    return "<a href=" + url + " class='urlLink2' target='_blank' onclick='window.open(this.href)'>" + match + "</a>";
}

function popUpHelpGuide(id, _step) {
	$.ajax({
		type: 'GET',
		url: "/system/processGuide/getProcessGuide",
		data: {"id":id},
		async:false,
		success : function(data){
			if(data.processGuide) {
				var height = 680;
				if ("D" == _step) height = 515;
				var contents = data.processGuide.contents;
				alertify.alert().destroy();
				alertify.alert(contents).set('resizable',true).resizeTo(1150,height);
			}
		}
	});
}

function openHelpGuideLink(id) {
	$.ajax({
		type: 'GET',
		url: "/system/processGuide/getProcessGuide",
		data: {"id":id},
		async:false,
		success : function(data){
			if(data.processGuide) {
				var url = data.processGuide.url;
				window.open(url, '_blank');
			}
		}
	});
}

/* UI 변경 시 추가 기능 */
var createTabNew = function (tabNm, tabLk) {
    var pattern = /\s/g;
    var tabName = tabNm.replace(pattern, '-');
	
    if ($(".content-wrapper.iframe-mode").children(".nav").children(".navbar-nav").find('#tab--' + tabName).length > 0) {
    	if ("BOM_Compare" == tabName || "History" == tabName) {
			deleteTabNew(tabName);
			createTabFnc(tabNm, tabName, tabLk);
		} else {
			$("#tab--" + tabName).trigger("click");
       	 	$("#tab--" + tabName).focus();
		}
    } else {
		createTabFnc(tabNm, tabName, tabLk);
    }

    if ($(".content-wrapper.iframe-mode").children(".nav").children(".navbar-nav").hasClass("ui-tabs") === true) {
        $(".content-wrapper.iframe-mode").children(".nav").children(".navbar-nav").removeClass("ui-tabs ui-widget ui-widget-content ui-corner-all");
    }

}

var createTabFnc = function (tabNm, tabName, tabLk) {
	var $tabs = $(".content-wrapper.iframe-mode").children(".nav").children(".navbar-nav").tabs();
   	var tab_length = $tabs.find('.nav-item').length;

  	var tabArr = tabLk.split('?'),
      	tabLink = tabArr[0],
      	tabAnchor = tabArr[0].replace(/#/g, '');

	if (tab_length > 20) {
      	alertify.error('More than 20 tabs are opened. Please close the tabs.', 0);
   		return;
	}

  	$tabs.find('.nav-item').removeClass('active');
   	$tabs.find('.nav-link').removeClass('active');

   	var frameSrc = tabAnchor;
   	var checkProjIden = tabAnchor.split('/');

   	$('.tab-pane').removeClass('show').removeClass('active');

  	let navItem = '<li class="nav-item" role="presentation">';
   	navItem += '<a href="#" class="btn-iframe-close" data-widget="iframe-close" data-type="only-this"><i class="fas fa-times"></i></a>';
   	navItem += '<a class="nav-link" data-toggle="row" id="tab--' + tabName + '" href="#panel--' + tabName + '" role="tab" aria-controls="panel--' + tabName + '"> ' + tabNm + ' </a></li>';
 	$(".content-wrapper.iframe-mode").children(".nav").children(".navbar-nav").append(navItem);

 	const iframe = '<div class="tab-pane fade" id="panel--' + tabName + '" role="tabpanel" aria-labelledby="tab--' + tabName + '"><iframe src="' + frameSrc + '"></iframe></div>';
 	$(".content-wrapper.iframe-mode").children(".tab-content").append(iframe);
	
	$("#tab--" + tabName).trigger("click");
}

var deleteTabNew = function (tabNm) {
	$('#tab--' + tabNm).parent().remove();
	$('#panel--' + tabNm).remove();
}

var existsTabName = function (tabNm) {
    var existsTab = false;
    if ($(".content-wrapper.iframe-mode").children(".nav").children(".navbar-nav").find('#tab--' + tabNm).length > 0) {
        existsTab = true;
    }
    return existsTab;
}

/* 2023-12-06 */
function stringToArray(str) {
    return arr = str.split(",").map(Number);
}

function onError(data, status) {
    alertify.error(String('[[ #{msg.common.valid} ]]'), 0);
}

function onError2(data, status) {
    alertify.error(String('[[ #{msg.common.valid2} ]]'), 0);
}

function onSuccess(data, status) {
    alertify.error(String('[[ #{msg.common.success} ]]'), 0);
}

/**
 * Ajax Utility
 * handle different types of AJAX requests (GET/POST)
 * */
function fnBasicAjaxData(data, url) {
    return $.ajax({type: 'GET', url: url, data: data, headers: {'Content-Type': 'application/json'}});
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
        cache: false,
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
        cache: false,
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
        cache: false,
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

function createTab_new(title, link, uniqueName, autoOpen) {
    const existingPanel = document.querySelector('.tab-pane[aria-labelledby="tab--' + uniqueName + '"]') || null;
    
    let tabId = `panel--${uniqueName}`
    let navId = `tab--${uniqueName}`
    
    if (existingPanel) {
        activateTab(uniqueName);
        $(`${tabId}`).focus();
        return;
    }

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

function deleteTab_new(uniqueName) {
    $("#tab--" + uniqueName).parent().find('.btn-iframe-close').trigger("click");
}

function reloadTab_new(link, uniqueName, act) {
    $('iframe').each(function () {
        var url = $(this).attr('src');

        if (url.indexOf("?") != -1) {
            var tmp = url.split('?');
            url = tmp[0];
        }

        if (url == link) {
            if (act === "create" || act === "reload") {
                $(this).attr('src', url + "?gnbF=Y");
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

    const iframeInActiveTab = activeTab.querySelector('iframe');
    if (iframeInActiveTab) {
        iframeInActiveTab.contentWindow.location.reload(true);
    }
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
        action: 'create_new'
    };

    parent.postMessage(JSON.stringify(data), "*");

}

var callReloadTabInframe = function (link, uniqueName) {
    var tabData = [];
    tabData[0] = link;
    tabData[1] = uniqueName;

    var data = {
        tabData: tabData,
        action: 'reload_new'
    };

    parent.postMessage(JSON.stringify(data), "*");
}

var callDeleteTabInFrame = function (uniqueName) {
    var tabData = [];
    tabData[0] = uniqueName;

    var data = {
        tabData: tabData,
        action: 'delete_new'
    };

    parent.postMessage(JSON.stringify(data), "*");
}

function changeURL(url) {
    window.location.href = url;
}

$(window).load(function () {
    // resizingJqGidSet();
    // resizingInnerJqGidSet();
    // resizingOuterJqGidSet();
    // /* 브라우저 창 크기에 따라 jqGrid Width 자동 조절 */
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
            $(window).innerWidth() - 40,
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
        class: "external-event pl-3",
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
        class: "external-event pl-3",
        name: id
    });

    const hiddenInput = $('<input>', {
        type: 'hidden',
        name: id,
        value: word
    });

    const deleteIcon = $('<i>', {
        class: 'fas fa-times text-blue-gray float-right mt-1',
    }).click(function () {
        deleteTag(this);
    });

    const content = $("<span/>", {text: word});

    tagContainer.append(hiddenInput, deleteIcon, content);
    gridDiv.append(tagContainer);
    $("#appendArea_" + id).prepend(gridDiv);
    inputElement.val(null).trigger("change");
}

function deleteTag(el) {
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

    if (start != -1) {
        start += cookieName.length;

        let end = cookieData.indexOf(';', start);
        if (end == -1) {
            end = cookieData.length;
        }
        cookieValue = cookieData.substring(start, end);
    }

    return unescape(cookieValue);
}

function setCookie(cookieName, value, exdays) {
    var exdate = new Date();
    exdate.setDate(exdate.getDate() + exdays);

    var cookieValue = escape(value) + ((exdays == null) ? "" : "; expires=" + exdate.toGMTString());
    document.cookie = cookieName + "=" + cookieValue;
}

function deleteCookie(cookieName) {
    var expireDate = new Date();
    expireDate.setDate(expireDate.getDate() - 1);

    document.cookie = cookieName + "= " + "; expires=" + expireDate.toGMTString();
}

function selectLang(lang) {
    window.location.replace('?lang=' + lang);
}

function callAutoCompleteFnc(value) {
	var autoCompleteTags = [];
	var target = "." + value;
	
	if ("autoComConfPartyId" == value) {
		if (autoComplete.partyConfIdTags.length == 0) {
			commonAjax.getPartnerConfIdTags().success(function (data, status, headers, config) {
                if (data != null) {
                    data.forEach(function (obj) {
                        if (obj != null) {
                            autoCompleteTags.push(obj.partnerId);
                        }
                    })
                }
            });
		} else {
			autoCompleteTags = autoComplete.partyConfIdTags;
		}
	} else if ("autoComConfSwNm" == value) {
		if (autoComplete.softwareConfNameTags.length == 0) {
			commonAjax.getPartnerConfSwNmTags().success(function (data, status, headers, config) {
                if (data != null) {
                    data.forEach(function (obj) {
                        if (obj != null) {
                            autoCompleteTags.push(obj.softwareName);
                        }
                    })
                }
            });
    	} else {
			autoCompleteTags = autoComplete.softwareConfNameTags;
		}
	} else if ("autoComConfParty" == value) {
		if (autoComplete.partyConfNameTags.length == 0) {
			commonAjax.getPartnerConfNmTags().success(function (data, status, headers, config) {
                if (data != null) {
                    data.forEach(function (obj) {
                        if (obj != null) {
                            autoCompleteTags.push(obj.partnerName);
                        }
                    })
                }
            });
		} else {
			autoCompleteTags = autoComplete.partyConfNameTags;
		}
	} else if ("autoComProjectNmConf" == value) {
		if (autoComplete.projectNameConfTags.length == 0) {
			commonAjax.getProjectNameConfTags().success(function (data, status, headers, config) {
                if (data != null) {
                    data.forEach(function (obj) {
                        if (obj != null) {
                            autoCompleteTags.push(obj.prjName);
                        }
                    })
                }
            });
		} else {
			autoCompleteTags = autoComplete.projectNameConfTags;
		}
	} else if ("autoComProjectIdConf" == value) {
		if (autoComplete.projectIdConfTags.length == 0) {
			commonAjax.getProjectIdConfTags().success(function (data, status, headers, config) {
                if (data != null) {
                    data.forEach(function (obj) {
                        if (obj != null) {
                            autoCompleteTags.push(obj.prjId);
                        }
                    })
                }
            });
		} else {
			autoCompleteTags = autoComplete.projectIdConfTags;
		}
	}
	
	$(target).autocomplete({
    	source: autoCompleteTags, minLength: 0, open: function () {
        	$(this).attr('state', 'open');
   		}, close: function () {
      		$(this).attr('state', 'closed');
 		}
 	})
	.focus(function () {
   		if ($(this).attr('state') != 'open') {
        	$(this).autocomplete("search");
     	}
	});
}

function resetForm(el) {
    $("#" + el).each(function() {
        $(this).find('input, textarea, select').each(function() {
            var $field = $(this);
            var name = $field.attr("name");
            if(name !== 'act' && name !== "defaultSearchType") {
                if ($field.is(':checkbox') || $field.is(':radio')) {
                    $field.prop('checked', false)
                } else {
                    $field.val('');
                }
            }
        });

        $(this).find('.select').each(function () {
            var $select = $(this);
            $select.val(null).trigger('change');
        })

        $(this).find('.select2').each(function () {
            var $select = $(this);
            $select.find('option:selected').remove();
            $select.val(null).trigger('change');
        })
    });
}

function updateSearchCondition(el){
    // multiple checkbox values to comma separate
    var chkElArr = ["restrictions", "statuses", "status"];
    $.each(chkElArr, function(index, item){
        var selectedValues  = "";
        $('#' + el + ' select[name="'+item+'"]').find(':selected').each(function() {
            if (selectedValues.length === 0) {
                selectedValues += $(this).val();
            } else {
                selectedValues += "," + $(this).val();
            }
        });

        if (selectedValues.length > 0) {
            appendFormCheckboxValuesEl(el, item, selectedValues);
        }
    });

    var formData = $("#" + el + " .save-value").serialize();

    if (formData) {
		loading.show();

        // name Match (ex. copyrigths -> copyright)
        formData = formData.replace(/copyrights/g, 'copyright');

        // publicYn value change (on/off -> N/Y)
        var publicYnIndex = formData.indexOf("publicYn=");
        if (publicYnIndex !== -1) {
            var publicYnValue = formData.substring(publicYnIndex + 9, formData.indexOf("&", publicYnIndex));
            if (publicYnValue === "on") {
                formData = formData.replace(/publicYn=on/, 'publicYn=N');
            }
        } else {
            formData += '&publicYn=Y';
        }

        $.ajax({
            url: "/configuration/updateDefaultSearchCondition",
            type: 'POST',
            dataType: "json",
            data: formData, // 선택된 요소들의 데이터를 전송합니다.
            cache: false,
            success: fn.onUpdateSuccess,
            error: fn.onError
        });
    }
}

function appendFormCheckboxValuesEl(el, item, selectedValues) {
    $('#' + el + ' input[name="chk_'+item+'"]').remove();
    var addEl = '<input class="save-value" type="hidden" name="chk_'+item+'" value="'+ selectedValues +'" />';
    $('#' + el).append(addEl);
}

let division;
let defaultColNames =[];
let savedColNames = [];

function createDropdownButton() {
    var newButton = document.createElement('button');
    newButton.setAttribute('type', 'button');
    newButton.classList.add('btn', 'btn-sm', 'btn-grid-light-gray', 'float-left', 'mr-1');
    newButton.setAttribute('data-toggle', 'dropdown');
    newButton.setAttribute('id', 'setUpColumnButton');
    newButton.setAttribute('aria-expanded', 'false');
    newButton.innerHTML = '<i class="fas fa-lg fa-cog"></i>';
    return newButton;
}

function createDropdownMenu(options) {
    const { _division, _totalColInfos, _defaultColNames, _savedColNames } = options;
    division = _division;
    defaultColNames = _defaultColNames;
    savedColNames = _savedColNames;

    const titleArea = document.createElement('div');
    titleArea.className = "text-lg-gray pl-3"
    titleArea.textContent = "Columns Setting"

    const dropdownMenu = document.createElement('div');
    dropdownMenu.classList.add('dropdown-menu', 'col-local');
    dropdownMenu.setAttribute('id', 'setUpColumnMenu');

    dropdownMenu.appendChild(titleArea);
    dropdownMenu.appendChild(createDivider());
    dropdownMenu.appendChild(createMenuItem('Restore Defaults', '#', 'dropdown-item', 'restoreDefaults()'))

    const dropdownItemArea = document.createElement('div');
    dropdownItemArea.style.height = '300px';
    dropdownItemArea.style.overflowY = 'scroll';

    _totalColInfos.forEach(colInfo => {
        const label = Object.keys(colInfo)[0];
        const id = colInfo[label];
        dropdownItemArea.appendChild(createCheckboxItem(label, id));
    });

    dropdownMenu.appendChild(dropdownItemArea);
    dropdownMenu.appendChild(createDivider());
    dropdownMenu.appendChild(createButtonArea());

    return dropdownMenu;
}

function createMenuItem(text, href, className, onClickFunction) {
    const menuItem = document.createElement('a');
    menuItem.className = className;
    menuItem.href = href;
    menuItem.textContent = text;
    menuItem.onclick = function(event) {
        event.preventDefault();
        onClickFunction();
    };
    menuItem.setAttribute("onclick", onClickFunction);
    return menuItem;
}

function createDivider(className) {
    const hr = document.createElement('div');
    hr.className = 'dropdown-divider';
    return hr;
}

function createCheckboxItem(label, id) {
    const dropdownItem = document.createElement('span');
    dropdownItem.className = 'dropdown-item';

    const divCustomCheckbox = document.createElement('div');
    divCustomCheckbox.className = 'custom-control custom-checkbox ml-1';

    const inputCheckbox = document.createElement('input');
    inputCheckbox.className = 'custom-control-input';
    inputCheckbox.type = 'checkbox';
    inputCheckbox.id = 'col_option_'+ id;

    if (defaultColNames.includes(id)) {
        inputCheckbox.checked = true;
        inputCheckbox.disabled = true;
    } else {
        if (savedColNames.includes(id)) {
            inputCheckbox.checked = true;
        }
    }

    const labelCheckbox = document.createElement('label');
    labelCheckbox.className = 'custom-control-label';
    labelCheckbox.htmlFor = 'col_option_'+ id;
    labelCheckbox.style.paddingTop = '2px';
    labelCheckbox.style.fontWeight = '400';
    labelCheckbox.textContent = label;

    divCustomCheckbox.appendChild(inputCheckbox);
    divCustomCheckbox.appendChild(labelCheckbox);

    dropdownItem.appendChild(divCustomCheckbox);

    return dropdownItem;
}

function saveColumnLocalization() {
    // Save to database using AJAX


    // Update grid display in the view
    const checkedIds = [];
    const checkboxes = document.querySelectorAll('.custom-control-input');
    checkboxes.forEach(function(checkbox) {
        if (checkbox.checked) {
            checkedIds.push(checkbox.id);
        }
    });

    const checkedColNames = [];
    checkedIds.forEach(function(id) {
        var colName = id.replace('col_option_', '');
        checkedColNames.push(colName);
    });

    const grid = $("#list");
    const allColNames = grid.jqGrid('getGridParam', 'colModel');
    allColNames.forEach(function(colName, index) {
        let _colName = colName.name
        if(!defaultColNames.includes(_colName)) {
            if (checkedColNames.includes(_colName)) {
                grid.jqGrid('showCol', _colName);
            } else {
                grid.jqGrid('hideCol', _colName);
            }
        }
    });

    resizingJqGidSet();
}

function createButtonArea() {
    var buttonArea = document.createElement("div");
    buttonArea.appendChild(createButton('Save', 'btn btn-ivory float-right mr-1 text-sm', 'saveColumnLocalization()'));
    buttonArea.appendChild(createButton('Cancel', 'btn btn-default float-right mr-1 text-sm', 'removeDropdownMenu()'));
    return buttonArea;
}

function createButton(text, className, clickFunction) {
    var button = document.createElement("button");
    button.className = className;
    button.textContent = text;
    button.setAttribute("onclick", clickFunction);
    return button;
}

function restoreDefaults() {
    event.preventDefault();
    const checkboxes = document.querySelectorAll('.custom-control-input');
    checkboxes.forEach(function(checkbox) {
        const id = checkbox.id.replace('col_option_', '');
        if (defaultColNames.includes(id)) {
            checkbox.checked = true;
        } else {
            checkbox.checked = false;
        }
    });
}

function removeDropdownMenu() {
    $("#setUpColumnMenu").removeClass("show");
}

function initPromise(event) {
    return new Promise((resolve, reject) => {
        event.init(() => {
            resolve();
        });
    });
}