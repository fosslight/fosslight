var lastTab = -1;	//이전 탭 기억변수
var selectTab = -1;	//현재 탭 기억변수
var deleteFlag = false;
var LINKREGEXP = /PRJ-\d+(?!.*\<\/a\>)|3rd-\d+(?!.*\<\/a\>)/gi;

$( document ).ajaxSend(function( event, jqxhr, settings ) {
  jqxhr.setRequestHeader("AJAX", true);
});

$( document ).ajaxError(function( event, jqxhr, settings, thrownError  ) {
  if ( jqxhr != undefined && (jqxhr.status == 401 || jqxhr.status == 403) ) {
      alert("Your session has expired. Please log in again");
      location.href = "/";
  }
});

var _popupComment = null;
var onAjaxLoadingHide = false;
// 하기 flag사용시에는 반드시 초기화까지 설정해줘양함
var doNotUseAutoLoadingHideFlag = 'N';
var gRowCnt = "";

$(document).ready(function (){
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
	
	$('.headerHandle').click(function(){
		$('#wrapBack').toggleClass('headerHide');
	});

	$('.btn_acc').click(function(){
		$('.accpop').show();
		$('#blind_wrap').show();
		$('html, body').animate({scrollTop: $("#wrap").offset().top}, 0);
		$('html').css('overflow','hidden');
	});//팝업 열기

	$('.pclose').click(function(){
		$('.pop').hide();
		$('#blind_wrap').hide();
		$('html').css('overflow','auto');
	}); // 팝업 닫기

	// datepicker format setting
	$(function(){
		$( '.cal' ).datepicker({
			dateFormat:"yymmdd"
		});
	}); // 달력

	var select = $(".selectSet select");
	
	select.change(function(){
		var select_name = $(this).children("option:selected").text();
		$(this).siblings("strong").text(select_name);
	});
	
	for (i=0; i<$(".selectSet select").length; i++){
		var select_ = $(".selectSet select").get(i);
		if($(select_).is(":disabled")) $(select_).siblings('strong').css('opacity','0.5');
	} // select disabled opacity
	
	$("#btnRejectNotice").click(function() {
		if(projectStatus == "COMP") {
			alertify.warning('If you need to modify, please click a "request to open" button in the basic information tab of the project.');
		} else {
			alertify.warning('If you need modify, please leave a comment on FOSSLight team.');
		}
		
		return false;
	});

	function initTab(){
		var query = window.location.search;
		var param = new URLSearchParams(query);
		var id = param.get("id");
		var prjFlag = param.get("project");
		if(id != null && prjFlag != null) {
			createTab(prjFlag == 'true' ? id + "_Project" : id + "_3rdParty",prjFlag == 'true' ? "#/project/edit/" + id : "#/partner/edit/" + id);
		} else {
			var _defaultTabStr = $("#defaultTabAnchorArr").val()||"";

			$.each(_defaultTabStr.split(","), function(idx, val){
				var _gnbHref = $("#header > div > div.gnb a[href$='"+val+"']");

				if(_gnbHref && _gnbHref.length == 1) {
					$("#header > div > div.gnb a[href$='"+val+"']").trigger("click");
				}
			});
		}
	}
	
	function tabExitHide(){
		var $tabs = $('#nav-tabs').tabs();
		var tab_length = $tabs.find('.nav-tab-menu li span').length;
		
		if(tab_length <= 2){
			$tabs.find('.nav-tab-menu li input').hide();
		} else {
			$tabs.find('.nav-tab-menu li input').show();
		}
	}
	
	/* Create new tab function */
	$(function newTab() {
		//initTab();
		var $tabs = $('#nav-tabs').tabs();
		
		$('.add-tab').click(function (e) {
			e.preventDefault();
		    $tabs.find( ".ui-tabs-nav" ).sortable({
		      axis: "x",
		      stop: function() {
		        $tabs.tabs( "refresh" );
		      }
		    });
		    
			var tabName = $(this).text(),
				tabLink = $(this).attr('href'),
				tabAnchor = $(this).attr('href').replace(/#/g, ''),
				tabNumber = -1;
			
			//2018-08-16 choye 추가
			var aTitle = $(this).attr('title');
			
			if(aTitle!=null && aTitle!="" && aTitle!=undefined){
				tabName = aTitle;
			}
			
			var tab_length = $tabs.find('.nav-tab-menu li span').length;

			if(tabLink.indexOf("?") != -1){
				var tmp = tabLink.split('?');
				
				tabLink = tmp[0];
			}
			
			if(tabAnchor.indexOf("?") != -1){
				var tmp = tabAnchor.split('?');
				
				tabAnchor = tmp[0];
			}
			
			$tabs.find('.nav-tab-menu li span').each(function (i) {
				if ($(this).text() == tabName) {
					tabNumber = i;
				}
			});
			
			if (tabNumber >= 0) {
				$tabs.tabs('option', 'active', tabNumber-1);
				reloadTab(tabAnchor,"create");
				lastTab = selectTab;
				selectTab = tabNumber-1;
			} else {
				var appendIFrame = "<iframe src='" + tabAnchor + "' style='width:100%; height:100%;' scrolling='yes' marginwidth='0' marginheight='0' frameborder='0' vspace='0' hspace='0'></iframe>";
				$(".contents").append("<div id=" + tabAnchor.replace(/\//g, '-') + " class='contentsBack'>" + appendIFrame + "</div>");
				$("<li><span><a class=" + tabAnchor.replace(/\//g, '-') + " href=" + tabLink.replace(/\//g, '-') + ">" + tabName + "</a></span><input type='button' value='x' class='ui-icon ui-icon-close' /></li>")
					.appendTo(".nav-tab-menu");
				$("#nav-tabs").tabs("refresh");
				$('#nav-tabs').tabs('option', 'active', -1);
				
				lastTab = selectTab;
				
				selectTab = $("#nav-tabs").tabs('option','active');
			}	
			
			// 화면갱신
			viewRefresh();
			
			return false;
		});
		
		initTab();
	});
	
	/* Delete tab function */
	$(document).on('click', '.ui-icon-close', function (event) {
		viewRefresh();
		
		var parent = $(this).parent(),
			index = parent.index(),
			tabs = $(this).closest(".ui-tabs"),
			panel = tabs.children().eq(index + 1),
			tabLink = parent.find("a").attr("href");
			$(tabLink).remove();
			parent.remove();
			panel.remove();
			
		if(lastTab > selectTab) lastTab--;
			
		$("#nav-tabs").tabs("refresh").tabs('option', 'active', lastTab > -1 ? lastTab : 0 );
		
		selectTab = lastTab;
	});
	
	/* tab click event */
	$(document).on('click', '.ui-tabs-anchor', function(event) {
		lastTab = selectTab;
		selectTab = $("#nav-tabs").tabs('option','active');
		
		viewRefresh();
	});
	
	
	$(window).bind('resize', function() {
		// 브라우저 창 크기에 따라 jqGrid Width 자동 조절
		tableRefresh();
		
		// 화면갱신
		viewRefresh();
	}).trigger('resize');
	
	$( ".commentBtn").click(function() {
		$( this ).toggleClass( "open" );
		$('.commentEditor').toggle();
		$('.projectContents').toggleClass( "pt255" );
	});

	$( ".btnExpand").click(function() {
		$( this ).toggleClass( "on" );
		$('.adminSearch').toggle();
	});
	
	$( ".btnHiddenExpand").click(function() {
		$( this ).toggleClass( "on" );
		$('.hiddenSearch').toggle();
	});

	$( ".btnToggle").click(function() {
		$( this ).toggleClass( "on" );
		$('.editSearchUp, .threeRdSearch').toggle();
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

/* iFrame에서 호출 (보안문제로 broadcast하여 호출) */
function receiveMessage(event) {
	var data = JSON.parse(event.data);
	
	switch(data.action){
		case 'create':
			createTab(data.tabData[0], data.tabData[1]);
			
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
			reloadTab(data.tabData[0],data.action);
			
			break;
		case 'change':
			changeTab(data.tabData[0]);
			
			break;
	}
}

if ('addEventListener' in window){
	window.addEventListener('message', receiveMessage, false);
} else if ('attachEvent' in window){ //IE
	window.attachEvent('onmessage', receiveMessage);
}

/* 부모창의 createTab 함수 호출 */
function createTabInFrame(tabNm, tabLk){
	var tabData = [tabNm, tabLk];
	var data = {
		tabData:tabData,
		action:'create'
	};
	
	parent.postMessage(JSON.stringify(data),"*");
	
}
function createTabInFrameWithCondition(tabNm, tabLk, sesKey, sesVal){
	$.ajax({	type: 'GET',url:'/sessionKeyValSave/'+sesKey+'/'+sesVal,async:false,data:{'sesKey':sesKey, 'sesVal':sesVal},headers: {'Content-Type': 'application/json'}});
	
	var tabData = [tabNm, tabLk];
	var data = {
		tabData:tabData,
		action:'create'
	};
	
	parent.postMessage(JSON.stringify(data),"*");
}

/* 부모창의 deleteTab 함수 호출 */
var deleteTabInFrame = function(tabLk){
	var tabData = [];
	tabData[0] = tabLk;
	
	var data = {
		tabData:tabData,
		action:'delete'
	};
	
	parent.postMessage(JSON.stringify(data),"*");
}
/* 부모창의 deleteTab 함수 호출 */
var activeDeleteTabInFrame = function(){
	var data = {
		action:'activeDelete'
	};
	
	parent.postMessage(JSON.stringify(data),"*");
}
var allDeleteTabInFrame = function(){
	var data = {
		action:'allDelete'
	};
	
	parent.postMessage(JSON.stringify(data),"*");
}
var returnTabInFrame = function(){
	var data = {
		action:'return'
	};
	
	parent.postMessage(JSON.stringify(data),"*");
}
/* 부모창의 reloadTab 함수 호출 */
var reloadTabInframe = function(tabLk){
	var tabData = [];
	tabData[0] = tabLk;
	
	var data = {
		tabData:tabData,
		action:'reload'
	};
	
	parent.postMessage(JSON.stringify(data),"*");
}
var changeTabInFrame = function(tabNumber){
	var tabData = [];
	tabData[0] = tabNumber;
	
	var data = {
			tabData:tabData,
			action:'change'
	};
	
	parent.postMessage(JSON.stringify(data),"*");
}

var activeTabInFrameList = function(targetTab){
	var tabName = "";
	
	if("PROJECT" == targetTab) {
		tabName = "Project List";
	} else if("PARTNER" == targetTab) {
		tabName = "3rd Party List";
	} else if("OSS" == targetTab) {
		
	} else if("LICENSE" == targetTab) {
		
	}
	
	if(tabName != "") {
		var _prjTabIdx = getTabIndex(tabName);
		if(_prjTabIdx != "") {
			var tabData = [];
			tabData[0] = _prjTabIdx;
			
			var data = {
					tabData:tabData,
					action:'change'
			};
			
			parent.postMessage(JSON.stringify(data),"*");
		}
	}
}

/* iFrame에서 호출한 createTab */
var createTab = function(tabNm, tabLk){
	var $tabs = $('#nav-tabs').tabs();
	var tab_length = $tabs.find('.nav-tab-menu li span').length;
	var tabName = tabNm,
		tabArr = tabLk.split('?'),
		tabLink = tabArr[0],
		tabAnchor = tabArr[0].replace(/#/g, ''),
		tabNumber = -1;
	
	if(tab_length > 20){
		alertify.error('Can not exceed 20 pages.', 0);
		
		return;
	}
	
	oldTabName = '';
	$tabs.find('.nav-tab-menu li span').each(function (i) {
		if ($(this).text() == tabName) {
			oldTabName = $(this).children().attr('href').replace(/-/g, '/');
			tabNumber = i;
		}
	});
	
	if(tabName != 'Project Editor'){
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
	
	if(checkProjIden[1] == 'oss'){
		frameSrc = tabLk.replace('#','');
	}
	
	var appendIFrame = "<iframe src='" + frameSrc + "' style='width:100%; height:100%;' scrolling='yes' marginwidth='0' marginheight='0' frameborder='0' vspace='0' hspace='0'></iframe>";
	
	$(".contents").append("<div id=" + tabAnchor.replace(/\//g, '-') + " class='contentsBack'>" + appendIFrame + "</div>");
	$("<li><span><a class=" + tabAnchor.replace(/\//g, '-') + " href=" + tabLink.replace(/\//g, '-') + ">" + tabName + "</a></span><input type='button' value='x' class='ui-icon ui-icon-close' /></li>")
	.appendTo(".nav-tab-menu");
	
	$("#nav-tabs").tabs("refresh");
	
	//마지막 탭으로 이동
	$('#nav-tabs').tabs('option', 'active', -1);
	
	//현태탭 변수에 저장
	if(!deleteFlag) {
		lastTab = selectTab;
	} else {
		deleteFlag = false;
	}
	
	selectTab = $("#nav-tabs").tabs('option','active');
	
	// 화면갱신
	viewRefresh();
	
	//iframe에 focus 2017.03.22
	var iframe = $("#"+tabAnchor.replace(/\//g, '-'))[0];
	var iframe_html = $(iframe).find('iframe')[0];
	
	body_html = $(iframe_html).focus();
}
var deleteTabByName = function(tabLk){
	var tabLink 	= tabLk.replace(/\//g,'-')
	  , tabAnchor 	= tabLink.replace(/#/g, '.')	
	  , parent 		= $(tabAnchor).parent().parent()	
	  , panel 		= $(tabAnchor).parent().next();
	
	$(tabLink).remove();	//div
	parent.remove();		//li
	panel.remove();			//input
	
	$("#nav-tabs").tabs("refresh");
	$('#nav-tabs').tabs('option', 'active', lastTab);
}

var deleteTab = function(tabLk){	
	var tabs = $("#nav-tabs").tabs();
	var panel = tabs.find( ".ui-state-default:eq("+selectTab+")" );
	
	panel.find(".ui-icon-close").trigger("click");
}

var activeDeleteTab = function(){
	var tabs = $("#nav-tabs").tabs();
	var tabNumber = $("#nav-tabs").tabs('option','active');
	var panelId = tabs.find( ".ui-tabs-active" ).remove().attr( "aria-controls" );
    $( "#" + panelId ).remove();
  
    $('#nav-tabs').tabs('option', 'active', selectTab+1);
    
    if(lastTab > selectTab) {
    	lastTab--;
    }
    
    tabs.tabs( "refresh" );
    $(document).focus();
}

var allDeleteTab = function(){
	var tabs = $("#nav-tabs").tabs();
	
	$.each(tabs.find( ".ui-state-default" ),function(i,panel){
		var panelId = $(panel).attr( "aria-controls" );
		$(panel).remove();
		$( "#" + panelId ).remove();
	});
	
	tabs.tabs( "refresh" );
	$(document).focus();
}

var reloadTab = function(tabLk,act){
	$('iframe').each(function(){
		var url = $(this).attr('src');
		
		if(url.indexOf("?") != -1){
			var tmp = url.split('?');
			url = tmp[0];
		}
		
		if(url == tabLk){
			if(act == "create"){
				$(this).attr('src', url);
			}else if(act == "reload"){
				$(this).attr('src', url+"?gnbF=Y");
			}
		}
	});
}

var returnTab = function(){
	$('#nav-tabs').tabs('option', 'active', lastTab);
	
	var temp = 0;
	temp = lastTab;
	lastTab = selectTab;
	selectTab = temp;
}
var changeTab = function(pos){
	var tabs = $("#nav-tabs");
	var tabNumber = tabs.tabs('option','active');
	var tab_length = tabs.find('.nav-tab-menu li span').length;
	
	if(isNaN(pos)) {
		if(pos=='right') {
			tabNumber++;
		} else if(pos=='left') {
			tabNumber--;
		} else {
			tabNumber = pos;
		}
	} else {
		tabNumber = pos;
	}

	if(tabNumber > tab_length - 2) {
	} if(tabNumber < 0) {		
	} else {
		$('#nav-tabs').tabs('option', 'active', tabNumber);
		lastTab = selectTab;
		selectTab = tabNumber;
	}
}

// error msg
function makeErrMsg(msg) {
	return '<div class="retxt">'+ msg +'</div>';
}

function cleanErrMsg(gridStr, rowId) {
	// 그리드 _로우 클래스 일경우
	if(typeof(gridStr)!='undefined' && typeof(rowId)!='undefined') {
		$("div.retxt."+gridStr+"_"+rowId).remove();
		$("div.retxtb."+gridStr+"_"+rowId).remove();
		$("div.retxtg."+gridStr+"_"+rowId).remove();
	} else if(typeof(gridStr)!='undefined' && typeof(rowId) == 'undefined') {
		$("#"+gridStr+" div.retxt").remove();
		$("#"+gridStr+" div.retxtb").remove();
		$("#"+gridStr+" div.retxtg").remove();
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
	$("#"+gridStr+" div.retxt").remove();
	$("#"+gridStr+" div.retxtb").remove();
	$("#"+gridStr+" div.retxtg").remove();
}

// 그리드 체크 메세지( gridStr 그리드 문자열 )
function gridValidMsg(msgData,gridStr) {
	var target = $("#"+gridStr);
	var mainIds = target.jqGrid('getDataIDs');
	
	$.each(msgData,function(key,value) {
		if("isValid" != key) {
			var seqSuffix = key.split(".");
			var rowId = seqSuffix[1];
			var subIds = "";
			var gridId = "";
			var subGrid;
			
			// gridId와 실제 rowId 가 다를 경우 처리
			for(var i in mainIds){
				gridId = target.jqGrid('getCell',mainIds[i],'gridId');
				
				if(rowId==gridId && mainIds[i]!=gridId){
					rowId = mainIds[i];
					
					break;
				}
				
				subGrid = $("#"+gridStr+"_"+mainIds[i]+"_t");
				subIds = subGrid.jqGrid('getDataIDs');
				
				for(var y in subIds){
					gridId = subGrid.jqGrid('getCell',subIds[y],'gridId');
					
					if(rowId==gridId && subIds[y]!=gridId){
						rowId = subIds[y];
						
						break;
					}
				}
			}
			
			var colName = $("#"+gridStr+" #"+rowId).parents("table").attr("id")+"_"+seqSuffix[0];
			
			// 그리드 메세지 그리기
			$("#"+gridStr+" #"+rowId+" td[aria-describedby=\""+colName+"\"]").append('<div class=\"'+gridStr+"_"+rowId+' retxt\">'+ value +'</div>');
		}
	});
}

// 성능개선 버전
function gridValidMsgNew(msgData,gridStr, type) {
	type = type || "NORMAL";
	var target = $("#"+gridStr);
	var mainIds = target.jqGrid('getDataIDs');
	
	if(msgData) {
		$.each(msgData,function(key,value) {
			if("isValid" != key && "validMsg" != key && "resultData" != key && "externalData" != key && "externalData2" != key) {
				var seqSuffix = key.split(".");
				if(seqSuffix.length  > 1) {
					var rowId = seqSuffix[1];
					
					if(seqSuffix[1].indexOf("-") > -1 && type == "NORMAL"){
						rowId = seqSuffix[1].replace(/(\d+)(?=\-)(.+)/g, "$1"); // rowId가 multi License라서 a to b 라면 a값만 가져옴.
					}
					
					var errRow = $("#"+rowId+" > td[aria-describedby='"+gridStr + "_" + seqSuffix[0]+"']");
					
					// 그리드 메세지 그리기
					if(errRow) {
						errRow.append('<div class=\"'+gridStr+"_"+rowId+' retxt\">'+ value +'</div>');
					}
				}
			}
		});		
	}
}

function gridValidMsg2(msgData,gridStr) {
	var mainIds = $("#"+gridStr).jqGrid('getDataIDs');
	
	$.each(msgData,function(key,value) {
		if("isValid" != key) {
			var seqSuffix = key.split(".");
			var rowId = seqSuffix[1];
			var subIds = "";
			var gridId = "";
			
			// gridId와 실제 rowId 가 다를 경우 처리
			for(var i in mainIds){
				gridId = $("#"+gridStr).jqGrid('getCell',mainIds[i],'componentId');
				
				if(rowId==gridId && mainIds[i]!=gridId){
					rowId = mainIds[i];
					break;
				}
				
				subIds = $("#"+gridStr+"_"+mainIds[i]+"_t").jqGrid('getDataIDs');
				
				for(var y in subIds){
					gridId = $("#"+gridStr+"_"+mainIds[i]+"_t").jqGrid('getCell',subIds[y],'gridId');
					
					if(rowId==gridId && subIds[y]!=gridId){
						rowId = subIds[y];
						
						break;
					}
				}
			}
			
			var colName = $("#"+gridStr+" #"+rowId).parents("table").attr("id")+"_"+seqSuffix[0];
			
			// 그리드 메세지 그리기
			$("#"+gridStr+" #"+rowId+" td[aria-describedby=\""+colName+"\"]").append('<div class=\"'+gridStr+"_"+rowId+' retxt\">'+ value +'</div>');
		}
	});
}
		
function createValidMsgComplex(msgData){
	hideErrMsg();
	
	//닉네임, 그리드데이터, 일반 인풋 Validation 체크
	$.each(msgData,function(key,value) {
		if("isValid" != key && "validMsg" != key) {
			if(key.indexOf(".") > -1 ){
				var seqSuffix = key.split(".");
				var targetId = seqSuffix[1] + "_" + seqSuffix[0];
				
				if(seqSuffix[0]=='licenseNicknames'){
					$('input[name=licenseNicknames]:eq('+(Number(seqSuffix[1])-1)+')').parent().next("span.retxt").html(value).show();
				}
				
				if($('input[id='+targetId+']').length > 0) {
					$('input[id='+targetId+']').after(makeErrMsg(value)).show();
				} else if($('textarea[id='+targetId+']').length > 0) {
					$('textarea[id='+targetId+']').after(makeErrMsg(value)).show();
				} else if($('select[id='+targetId+']').length > 0) {
					$('select[id='+targetId+']').after(makeErrMsg(value)).show();
				}
			} else {
				if(key == 'licenseNicknames'){
					$('input[name=licenseNicknames]').parent().next("span.retxt").html(value).show();
				}

				if(key == 'validMsgModelList') {
					$('#validMsgModelList').html(value).show();
				}
				
				if($('input[name='+key+']').length > 0) {
					$('input[name='+key+']').next("span.retxt,div.retxt").html(value).show();
				} else if($('textarea[name='+key+']').length > 0) {
					$('textarea[name='+key+']').next("span.retxt,div.retxt").html(value).show();
				} else if($('select[name='+key+']').length > 0) {
					$('select[name='+key+']').parent().siblings("span.retxt,div.retxt").html(value).show();
				}
			}
		}
	});
}

// grid 관련
// grid pager 미표시
function hidePageNav(pagerId) {
	if($("#"+pagerId+"_center")) {
		$("#"+pagerId+"_center").hide();
	}
	
	if($("#"+pagerId+"_right")) {
		$("#"+pagerId+"_right").hide();
	}	
}

function gridListBulkEdit(listId, fn) {
    var grid = $("#"+listId);
    var ids = grid.jqGrid('getDataIDs');

    for (var i = 0; i < ids.length; i++) {
    	if(fn) {
    		grid.jqGrid('editRow',ids[i], true, fn);
    	} else {
    		grid.jqGrid('editRow',ids[i], true);
    	}
    }
}

var jsonOptions = {
	type : "POST",
	contentType : "application/json; charset=utf-8",
	dataType : "json"
};

function createJSON(postdata) {
	if (postdata.id === '_empty') {
		postdata.id = null; // rest api expects int or null
	}
		
	return JSON.stringify(postdata)
}
//비교 및 연산
function changeObjectToArray(obj){
	if(obj == '' || obj == null) {
		obj = [];
	} else if(typeof obj =='string') {
		obj = [obj];
	}
	
	return obj;
}

//자동완성용 공통 AJAX
var commonAjax = {
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
	return $.ajax({	type: 'GET',url:CTX_PATH+url,data:data,headers: {'Content-Type': 'application/json'}});
}

var autoComplete = {
	licenseTags:[],
	licenseLongTags:[],
	ossTags:[],
	//project
	projectNameTags:[], projectNameConfTags:[], projectVersionTags:[],projectModelTags:[],
	//partner
	partyNameTags:[], partyConfNameTags:[], softwareNameTags:[], softwareConfNameTags:[],softwareVersionTags:[], softwareConfVersionTags:[],
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
							autoComplete.projectNameTags.push(obj.prjName);
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

	    $(".autoComProjectVersion").autocomplete({source: autoComplete.projectVersionTags, minLength: 0,open: function() { $(this).attr('state', 'open'); },close: function () { $(this).attr('state', 'closed'); }})
	    .focus(function() {if ($(this).attr('state') != 'open') {$(this).autocomplete("search");}});
	    
	    $(".autoComProjectModel").autocomplete({source: autoComplete.projectModelTags, minLength: 0,open: function() { $(this).attr('state', 'open'); },close: function () { $(this).attr('state', 'closed'); }})
	    .focus(function() {if ($(this).attr('state') != 'open') {$(this).autocomplete("search");}});

	    $(".autoComParty").autocomplete({source: autoComplete.partyNameTags, minLength: 0,open: function() { $(this).attr('state', 'open'); },close: function () { $(this).attr('state', 'closed'); }})
	    .focus(function() {if ($(this).attr('state') != 'open') {$(this).autocomplete("search");}});
	    
	    $(".autoComConfParty").autocomplete({source: autoComplete.partyConfNameTags, minLength: 0,open: function() { $(this).attr('state', 'open'); },close: function () { $(this).attr('state', 'closed'); }})
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
	    	if(item.division) {
	    		return $( "<li>" ).append( "<div>" + item.division + ' > ' + item.label + "("+item.id+")"+"</div>" ).appendTo( ul );
	    	} else {
	    		return $( "<li>" ).append( "<div>" + item.label + "("+item.id+")"+"</div>" ).appendTo( ul );
	    	}
	    	
	    };
	}
}

// 날짜 유효성검사 및 포맷변환
function validationDate() {
	var days = [31,28,31,30,31,30,31,31,30,31,30,31];
	
	$(".cal").each(function(i,v){
		if(v.value != ""){
			var value = v.value;
			var pattenDate = /\d{4}-\d{2}-\d{2}/g; // 포맷 검사 패턴 0000.00.00
			var pattenDot = /\./g; // . 유무 검사
			
			if(!pattenDate.test(value)) {
				
				value = value.replace(/\.+/g,".");
				
				var matchDot = new Array();
				var temp = ""
					
				for(var i=0; (matchDot[i] = pattenDot.exec(value)) != null;i++);
				
				var matchLen = matchDot.length-1;
				
				if(matchLen != null && matchLen > 0 && matchLen <= 2) {					
					if(matchLen == 1) {
						var year = value.substring(0,matchDot[0].index);
						var month = value.substring(matchDot[0].index+1,(value.length - matchDot[0].index) == 1 ? matchDot[0].index+2:matchDot[0].index+3);
						
						if(month > 0 && month < 13) {
							var tempDate = value.substring(0,4)+"."+ (month.length == 1?"0":"") + month;
							
							v.value=tempDate;
						} else {
							return false;
						}
					} else if(matchLen == 2) {
						var year = value.substring(0,matchDot[0].index);
						var month = value.substring(matchDot[0].index+1,matchDot[1].index);
						var day = value.substring(matchDot[1].index+1,(value.length - matchDot[1].index) == 1 ? matchDot[1].index+2:matchDot[1].index+3)
						
						if(month > 0 && month <= 12) {
							var maxDay = days[parseInt(month)-1];
							
							if(month == '02') { 
								if(parseInt(year) % 4 == 0) {
									maxDay += 1; 
								}
							} // 윤년 2월일경우 일수를 29일로
							
							if(day > 0 && day <= maxDay) {
								var tempDate = year+"."+ (month.length == 1?"0":"") +month+"."+ (day.length == 1?"0":"") +day;
								v.value=tempDate;
							} else {
								return false;
							}
						} else {
							return false;
						}
					}
				} else {				
					value=value.replace(/\./gi, ''); // . 제거
					var len = value.length;
					
					if(len == 4) {
						v.value=value;
					} else if(len == 5) {
						var tempDate = value.substring(0,4)+".0"+value.substring(4,5);
						
						v.value=tempDate;
					} else if(len == 6) {
						var month = value.substring(4,6);
						
						if(month > 0 && month < 13) {
							var tempDate = value.substring(0,4)+"."+month;
							
							v.value=tempDate;
						} else {
							return false;
						}
					} else if(len == 8) {
						var year = value.substring(0,4);
						var month = value.substring(4,6);
						var day = value.substring(6,8);
						
						if(month > 0 && month <= 12) {
							var maxDay = days[parseInt(month)-1];
							
							if(month == '02') { 
								if(parseInt(year) % 4 == 0) {
									maxDay += 1; 
								}
							} // 윤년 2월일경우 일수를 29일로
							
							if(day > 0 && day <= maxDay) {
								var tempDate = year+"."+month+"."+day;
								
								v.value=tempDate;
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
function viewRefresh(){
	if($('.tabMenu ul').height() != 0){
		// tab 이동시 스크롤 없어지는 에러 처리
		// 컨텐츠 크기를 강제로 변경하여 스크롤이 생기도록 처리함
		var val = $('.tabMenu ul').height()/30;

		var agt = navigator.userAgent.toLowerCase();
		
		if(agt.indexOf("chrome") > -1) {
			$('.contents').css('top', (((42*val)-(parseInt(val)*10.5))-(0.9/(parseInt(val)+1)))+'px');
		}
		
		setTimeout(function(){
			var cHeight = $('.nav-tab-menu').height();
			$('.contentsFixBack').height(cHeight+9);
			$('.contents').css('top', ((42*val)-(parseInt(val)*10.5))+'px');
		}, 10);
	}
}

// 브라우저 창 크기에 따라 jqGrid Width 자동 조절
function tableRefresh(){
	var width = $('#wrapIframe').width();
	
	$('.ui-jqgrid-btable').each(function(){
		var id =  $(this).attr('id');
		
		if(id.indexOf('_') == -1){
			$(this).jqGrid('setGridWidth', 0, true);
			$(this).jqGrid('setGridWidth', width, true);			
		}
	})
}

// UI BLOCK FOR ELEMENT
function uiBlock(objArr, bln){
	// default
	$.blockUI.defaults.overlayCSS.backgroundColor = '#ffffff';
	$.blockUI.defaults.overlayCSS.opacity = 0.1;
	$.blockUI.defaults.overlayCSS.cursor = 'default'; 
	
	// bln : Block or unBlock
	for(var o in objArr){
		if(bln) {
			objArr[o].closest(".ui-jqgrid").block({
			    message: null
			});
		} else {
			objArr[o].closest(".ui-jqgrid").unblock();
		}
	}
}

function openNVD(cveId) {
	if (typeof cveId =="undefined" || cveId == undefined || cveId.trim() == "undefined") {
		return false;
	}

	if(cveId != "") {
		window.open("https://web.nvd.nist.gov/view/vuln/detail?vulnId="+cveId.trim(), "_blank");
	}
}

function openNVD2(_ossName, _url){
	if(_popupVuln == null || _popupVuln.closed) {
		_popupVuln = window.open(_url, "vulnViewPopup_"+_ossName, "width=900, height=600, toolbar=no, location=no, left=100, top=100, scrollbars=yes, resizeable=yes");
		
		if(!_popupVuln || _popupVuln.closed || typeof _popupVuln.closed=='undefined') {
			alertify.alert('<spring:message code="msg.common.window.allowpopup" />', function(){});
		}
	} else {
		_popupVuln.close();
		
		_popupVuln = window.open(_url, "vulnViewPopup_"+_ossName, "width=900, height=600, toolbar=no, location=no, left=100, top=100, scrollbars=yes, resizeable=yes");
	}
}

function openCommentHistory(_url) {	
	if(_popupComment == null || _popupComment.closed){
		_popupComment = window.open(_url, "commentPopup", "width=900, height=600, toolbar=no, location=no, left=100, top=100, scrollbars=yes, resizeable=yes");
		
		if(!_popupComment || _popupComment.closed || typeof _popupComment.closed=='undefined') {
			alertify.alert('<spring:message code="msg.common.window.allowpopup" />', function(){});
		}
	} else {
		_popupComment.close();
		
		_popupComment = window.open(_url, "commentPopup", "width=900, height=600, toolbar=no, location=no, left=100, top=100, scrollbars=yes, resizeable=yes");
	}
}

function gridValidMsgChk(rowId,idName,MsgData,target) {
	var chkMsgData = new Object();
	var chkKey = idName+"."+rowId;

	$.each(MsgData,function(key,value) {
		if(chkKey != key) {
			chkMsgData[key]=value;
		}
	});
	
	if(target == "srcList"){
		window.srcValidMsgData = chkMsgData;
	} else if(target == "binAndroidList") {
		window.binAndroidValidMsgData = chkMsgData;
	} else if(target == "binList") {
		window.binValidMsgData = chkMsgData;
	} else if(target == "batList") {
		window.batValidMsgData = chkMsgData;
	} else if(target == "list") {
		window.partyValidMsgData_e = chkMsgData;
	} else if(target == "batList_e") {
		window.batValidMsgData_e = chkMsgData;
	}
}

function gridDiffMsgChk(rowId,idName,MsgData,target) {
	var chkMsgData = new Object();
	var chkKey = idName+"."+rowId;

	$.each(MsgData,function(key,value) {
		if(chkKey != key) {
			chkMsgData[key]=value;
		}
	});
	
	if(target == "srcList"){
		window.srcDiffMsgData = chkMsgData;
	} else if(target == "binAndroidList") {
		window.binAndroidDiffMsgData = chkMsgData;
	} else if(target == "binList") {
		window.binDiffMsgData = chkMsgData;
	} else if(target == "batList") {
		window.batDiffMsgData = chkMsgData;
	} else if(target == "list") {
		window.partyDiffMsgData_e = chkMsgData;
	} else if(target == "batList_e") {
		window.batDiffMsgData_e = chkMsgData;
	}
}


function gridInfoMsgChk(rowId,idName,MsgData,target) {
	var chkMsgData = new Object();
	var chkKey = idName+"."+rowId;

	$.each(MsgData,function(key,value) {
		if(chkKey != key) {
			chkMsgData[key]=value;
		}
	});
	
	if(target == "binAndroidList") {
		window.binAndroidInfoMsgData = chkMsgData;
	} else if(target == "binList") {
		window.binInfoMsgData = chkMsgData;
	}
}

function gridValidMsgRowId(msgData,gridStr,selRowId) {
	if(msgData) {
		$.each(msgData,function(key,value) {
			var seqSuffix = key.split(".");
			
			if(seqSuffix.length  > 1) {
				var rowId = seqSuffix[1];
				var errRow = "";
				
				if(selRowId == rowId){
					errRow = $("#"+rowId+" > td[aria-describedby='"+gridStr + "_" + seqSuffix[0]+"']");
				}
				
				if(errRow) {
					errRow.append('<div class=\"'+gridStr+"_"+rowId+' retxt\">'+ value +'</div>');
				}
			}
		});
	}
}

// diff
function gridDiffMsgRowId(msgData,gridStr,selRowId) {
	if(msgData) {
		$.each(msgData,function(key,value) {
			var seqSuffix = key.split(".");
			
			if(seqSuffix.length  > 1) {
				var rowId = seqSuffix[1];
				var errRow = "";
				
				if(selRowId == rowId){
					errRow = $("#"+rowId+" > td[aria-describedby='"+gridStr + "_" + seqSuffix[0]+"']");
				}
				
				if(errRow) {
					errRow.append('<div class=\"'+gridStr+"_"+rowId+' retxtb\">'+ value +'</div>');
				}
			}
		});
	}
}

//diff
function gridDiffMsg(msgData,gridStr, type) {
	type = type || "NORMAL";
	var target = $("#"+gridStr);
	var mainIds = target.jqGrid('getDataIDs');
	
	if(msgData){
		$.each(msgData,function(key,value) {
			if("isValid" != key && "validMsg" != key && "resultData" != key && "externalData" != key && "externalData2" != key && "externalData3" != key) {
				var seqSuffix = key.split(".");
				
				if(seqSuffix.length  > 1) {
					var rowId = seqSuffix[1];
					var diffRow;
					
					if(type == "NORMAL"){
						if(rowId.indexOf("-") > -1) {
						
						} else {
							diffRow = $("#"+rowId+" > td[aria-describedby='"+gridStr + "_" + seqSuffix[0]+"']");
						}
					}
					
					if(type == "SELF"){
						diffRow = $("#"+rowId+" > td[aria-describedby='"+gridStr + "_" + seqSuffix[0]+"']");
					}
					
					// 그리드 메세지 그리기
					if(diffRow) {
						diffRow.append('<div class=\"'+gridStr+"_"+rowId+' retxtb\">'+ value +'</div>');
					}
				}
			}
		});
	}
}

// info message
function gridInfoMsgRowId(msgData,gridStr,selRowId) {
	if(msgData) {
		$.each(msgData,function(key,value) {
			var seqSuffix = key.split(".");
			
			if(seqSuffix.length  > 1) {
				var rowId = seqSuffix[1];
				var errRow = "";
				
				if(selRowId == rowId){
					errRow = $("#"+rowId+" > td[aria-describedby='"+gridStr + "_" + seqSuffix[0]+"']");
				}
				
				if(errRow) {
					errRow.append('<div class=\"'+gridStr+"_"+rowId+' retxtg\">'+ value +'</div>');
				}
			}
		});
	}
}
function gridInfoMsg(msgData,gridStr) {
	var target = $("#"+gridStr);
	var mainIds = target.jqGrid('getDataIDs');
	
	$.each(msgData,function(key,value) {
		if("isValid" != key && "validMsg" != key && "resultData" != key && "externalData" != key && "externalData2" != key && "externalData3" != key) {
			var seqSuffix = key.split(".");
			
			if(seqSuffix.length  > 1) {

				var rowId = seqSuffix[1];
				var diffRow;
				
				if(rowId.indexOf("-") > -1) {
				} else {
					diffRow = $("#"+rowId+" > td[aria-describedby='"+gridStr + "_" + seqSuffix[0]+"']");
				}
				
				// 그리드 메세지 그리기
				if(diffRow) {
					diffRow.append('<div class=\"'+gridStr+"_"+rowId+' retxtg\">'+ value +'</div>');
				}
			}
		}
	});
}

var getTabIndex = function(tabNm){ 
	var $id = $('#nav-tabs', parent.document); // parent.document도 가능
	var index = "";
	
	$id.find('.nav-tab-menu li span').each(function (i) {
		if($(this).text() == tabNm){
			index = i-1;
		}
	});
	
	return index;
}

var checkAll = function(targetDiv, taget) {
	var classObj = $("."+targetDiv).find(".sheetNum");
	
	if($(taget).is(":checked")) {
		classObj.prop("checked", true);
	} else {
		classObj.prop("checked", false);
	}
	
	classObj.change(function() {
		var cnt=0;
		classObj.each(function(idx) {
			if(idx != 0 && $(this).is(":checked")) {
				cnt++;
			}
		});
		
		if(cnt == classObj.length-1) {
			$(taget).prop("checked", true);
		} else {
			$(taget).prop("checked", false);
		}
	});
}

function LPAD(s, c, n) {   
    if (! s || ! c || s.length >= n) {
        return s;
    }
 
    var max = (n - s.length)/c.length;
    
    for (var i = 0; i < max; i++) {
        s = c + s;
    }
 
    return s;
}

function RPAD(s, c, n) { 
    if (! s || ! c || s.length >= n) {
        return s;
    }
 
    var max = (n - s.length)/c.length;
    
    for (var i = 0; i < max; i++) {
        s += c;
    }
 
    return s;
}

function getFormData($form){
    var unindexed_array = $form.serializeArray();
    var indexed_array = {};

    $.map(unindexed_array, function(n, i){
        indexed_array[n['name']] = n['value'];
    });
    
    return indexed_array;
}

function getCookie(cname) {
    var name = cname + "=";
    var ca = document.cookie.split(';');
    
    for(var i=0; i<ca.length; i++) {
        var c = ca[i];
        
        while (c.charAt(0)==' ') {
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
    d.setTime(d.getTime() + (exdays*24*60*60*1000));
    var expires = "expires="+d.toUTCString();
    
    document.cookie = cname + "=" + cvalue + "; " + expires;
}

function showHelpLink(id, target){
	var _target = "helpLink";
	
	if(target && target.length > 0) {
		_target = target;
	}
	
	var _showItem = $('#'+_target);
	
	if(_showItem && _showItem.length > 0) {
		$.ajax({
			type: 'GET',
			url: CTX_PATH+"/system/processGuide/getProcessGuide",
			data: {"id":id},
			async:false,
			success : function(data){
				if(data.processGuide){
					var contents = data.processGuide.contents;
					
					if(data.processGuide.useYn == "Y") {
						_showItem.show();
						
						if(contents && contents.trim()) {
							_showItem.attr("title", contents).tooltip({
								content: function () {
									return $(this).prop('title');
								}
							});
						}
						
						if(data.processGuide.url && data.processGuide.url.trim()){
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
function moveTabInFrameByCommentPopup(prjId, stage){
	var tabIdx = "";
	var urlTxt = "";
	
	if(stage == "basicInfoPrj") {
		tabIdx = prjId+"_Project";
		urlTxt = '#/project/edit/'+prjId;
	} else if(stage == "identification") {
		tabIdx = prjId+"_Identify";
		urlTxt = '#/project/identification/'+prjId+'/4';
	} else if(stage == "packaging") {
		tabIdx = prjId+"_Packaging";
		urlTxt = '#/project/verification/'+prjId;
	} else if(stage == "distribution") {
		tabIdx = prjId+"_Distribute";
		urlTxt = '#/project/distribution/'+prjId;
	} else if(stage == "basicInfo3rd") {
		tabIdx = prjId+"_3rdParty";
		urlTxt = '#/partner/edit/'+prjId;
	}
	
	var idx = getTabIndex(tabIdx);
	
	if(idx != "") {
		changeTabInFrame(idx);
	} else {
		createTabInFrame(tabIdx, urlTxt);
	}
}

function calValidation(target, e){
	var result = $(target).val();
	
	if(/\d+/.test(result)) {
		result = result.match(/\d+/g).join("");
		
		$(target).val(result);
	} else {
		$(target).val("");
	}
}

function isMaximumRowCheck(totalRow){
	if(totalRow > +gRowCnt){
		alertify.error(getMsgMaxRowCnt(), 0);
		
		return false;
	}
	
	return true;
}

function setMaxRowCnt(maxRowCnt){
	if(!/.+/.test(gRowCnt)){
		gRowCnt = maxRowCnt;
	}
}

function getMsgMaxRowCnt(){
	var msgGRowCnt = gRowCnt.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",");
	
	return "More than "+msgGRowCnt+" can not be exported.";
}

function getBarChart(obj){
	var tooltip = obj.tooltip;
	
	if(typeof obj.tooltip != "object"){
		tooltip = {
			pointFormat: '<span style="color:{series.color}">{series.name}</span>: <b>{point.y}</b> ({point.percentage:.0f}%)<br/>',
			shared: true
		};		
	};
	
	return new Highcharts.chart(obj.chartId, {
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
		legend : {
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

function getPieChart(obj){
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
			formatter: function(){
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
$(document).ready(function (){
	//alertify Default 설정
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
});

/**
 * 문자열이 빈 문자열인지 체크하여 결과값을 리턴한다.
 * @param str       : 체크할 문자열
 */
function isEmpty(str){
     
    if(typeof str == "undefined" || str == null || str == "")
        return true;
    else
        return false ;
}
 
/**
 * 문자열이 빈 문자열인지 체크하여 기본 문자열로 리턴한다.
 * @param str           : 체크할 문자열
 * @param defaultStr    : 문자열이 비어있을경우 리턴할 기본 문자열
 */
function nvl(str, defaultStr){
     
    if(typeof str == "undefined" || str == null || str == "")
        str = defaultStr ;
     
    return str ;
}

//2018-07-31 choye 추가
var searchStringOptions = {searchoptions:{sopt:['cn','eq','ne','bw','bn','ew','en','nc']}};
var searchNumberOptions = {searchoptions:{sopt:['ge','le','gt','lt','eq']}};
var searchDateOptions = {searchoptions:{sopt:['eq','lt','le','gt','ge']}};


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