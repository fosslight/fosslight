
$(document).ready(function (){	
	/*$('.btn_acc').click(function(){
		$('.accpop').show();
		$('#blind_wrap').show();
		$('html, body').animate({scrollTop: $("#wrap").offset().top}, 0);
		$('html').css('overflow','hidden');
	});//팝업 열기

	$('.pclose').click(function(){
		$('.pop').hide();
		$('#blind_wrap').hide();
		$('html').css('overflow','auto');
	}); // 팝업 닫기*/

	
	$(function(){
		$( '.cal' ).datepicker({
			dateFormat:"yy.mm.dd"
		});
	}); // 달력

	$(function() {
		var availableTags = [
		  "ActionScript",
		  "AppleScript",
		  "AppleScript1",
		  "Asp",
		  "BASIC",
		  "C",
		  "C++",
		  "Clojure",
		  "COBOL",
		  "ColdFusion",
		  "Erlang",
		  "Fortran",
		  "Groovy",
		  "Haskell",
		  "Java",
		  "JavaScript",
		  "Lisp",
		  "Perl",
		  "PHP",
		  "Python",
		  "Ruby",
		  "Scala",
		  "Scheme"
		];
		$( ".autoCom" ).autocomplete({
		  source: availableTags
		});
	}); // 자동완성

	
	var select = $(".selectSet select");
	select.change(function(){
		var select_name = $(this).children("option:selected").text();
		$(this).siblings("strong").text(select_name);
	});
	for (i=0; i<$(".selectSet select").length; i++){
		var select_ = $(".selectSet select").get(i);
		if($(select_).is(":disabled")) $(select_).siblings('strong').css('opacity','0.5');
	} // select disabled opacity

	/* 브라우저 창 크기에 따라 jqGrid Width 자동 조절 */
	$(window).bind('resize', function() {
		if($('.jqGridSet table').length > 0){
			// 그리드의 width 초기화
			$('.jqGridSet table').jqGrid('setGridWidth', 0, true);
			// 그리드의 width를 div 에 맞춰서 적용
			$('.jqGridSet table').jqGrid('setGridWidth', $('.jqGridSet').width(), true);
		}
	}).trigger('resize');

	$( ".commentBtn" ).click(function() {
		$( this ).toggleClass( "open" );
		$('.commentEditor').toggle();
		$('.projectContents').toggleClass( "pt255" );
	});

	$( ".btnExpand" ).click(function() {
		$( this ).toggleClass( "on" );
		$('.adminSearch').toggle();
	});

	$( ".btnToggle" ).click(function() {
		$( this ).toggleClass( "on" );
		$('.editSearchUp, .threeRdSearch').toggle();
	});
	
});
/* 부모창의 createTab 함수 호출 */
var createTabInFrame = function(tabNm, tabLk){
	var tabData = [];
	tabData[0] = tabNm;
	tabData[1] = tabLk;
	
	parent.postMessage(tabData,"*");
	
}
var serializeObject = function (formData) {
		"use strict";
		var result = {};
		var extend = function (i, element) {
			var node = result[element.name];
			// If node with same name exists already, need to convert it to an array as it
			// is a multi-value field (i.e., checkboxes)
	
			if ('undefined' !== typeof node && node !== null) {
				if ($.isArray(node)) {
					node.push(element.value);
				} else {
					result[element.name] = [node, element.value];
				}
			} else {
				result[element.name] = element.value;
			}
		};
		$.each(formData.serializeArray(), extend);
		return result;
}

var makeSelectByCodes = function(typeCodes, element){
	"use strict";
	typeCodes.forEach(function(typeCode){
		$('<option value="'+typeCode.cdDtlNo+'">'+typeCode.cdDtlNm+'</option>').appendTo(element);
	});
	
} 
/** jqGrid함수*/
var jqGridEditor = {
	//jqGrid 생성
	create : function(){
		$('#list').jqGrid({
			datatype: 'json',
			rowNum: 20,
			rowList: [20, 40, 60],
			autowidth: true,
			pager: '#pager',
			gridview: true,
			sortable: function (permutation) {
				//alert ('permutation=' + permutation.join(','));
			},
			loadonce:false,
			height: 'auto',
			viewrecords: true
		})
	},
	//새로고침
	reload : function(){
		$('#list').jqGrid().trigger('reloadGrid');
	},
	//url ==서버 URL
	setUrl : function(param){
		$('#list').jqGrid('setGridParam', {url : param});
	},
	//dataType == 받아오는 데이터의 형태
	setDataType : function(param){
		$('#list').jqGrid('setGridParam', {datatype: param});
	},
	//jsonReader = 기본적으로 그리드를 구성할시에 필요한 파라미터 값
	setJsonReader : function(param){
		$('#list').jqGrid('setGridParam', {
			repeatitems: false,
			id : param,
			root:function(obj){return obj.rows;},
			page:function(obj){return obj.page;},
			total:function(obj){return obj.total;},
			records:function(obj){return obj.records;}
		})
	},
	//colNames ==  각 행의 이름 => ['a','b','c']의 형태로 입력
	setColNames : function(param){
		$('#list').jqGrid('setGridParam', {colNames: param});
	},
	//colModel == 각 행의 값 및 cell의 설정 => [ {},{},{}]의 형태로 입력
	setColModel : function(param){
		$('#list').jqGrid('setGridParam', {colModel: param});
	},
	//rowNum == 한페이지에 표시될 record의 숫자
	setRowNum : function(param){
		$('#list').jqGrid('setGridParam', {rowNum: param});
	},
	//rowList == 한 페이지에 표시 될 record 숫자를 제어 하기 위한 갯수들
	setRowList : function(param){
		$('#list').jqGrid('setGridParam', {rowList: param});
	},
	//autowidth
	setAutoWidth : function(param){
		$('#list').jqGrid('setGridParam', {autowidth: param});
	},
	//pager
	setPager : function(param){
		$('#list').jqGrid('setGridParam', {pager: param});
	},
	//gridview
	setGridView : function(param){
		$('#list').jqGrid('setGridParam', {gridview: param});
	},
	//sortable
	setSortAble : function(param){
		$('#list').jqGrid('setGridParam', {sortable: param});
	},
	//sortname
	setSortName : function(param){
		$('#list').jqGrid('setGridParam', {sortname: param});
	},
	//viewrecords
	setViewRecords : function(param){
		$('#list').jqGrid('setGridParam', {viewrecords: param});
	} ,
	//sortorder
	setSortOrder : function(param){
		$('#list').jqGrid('setGridParam', {sortorder: param});
	},
	//loadonce
	setLoadOnce  : function(param){
		$('#list').jqGrid('setGridParam', {loadonce: param});
	},
	//height
	setHeight : function(param){
		$('#list').jqGrid('setGridParam', {height: param});
	},
	//dblClickRow
	setDblClickRow : function(param){
		$('#list').jqGrid('setGridParam', {ondblClickRow: param});
	},
	//selectRow
	setSelectRow : function(param){
		$('#list').jqGrid('setGridParam', {onSelectRow: param});
	},
	//loadComplete
	setLoadCOmplete : function(param){
		$('#list').jqGrid('setGridParam', {loadComplete: param});
	}
}


//error msg
function makeErrMsg(msg) {
	return '<span class="retxt">'+ msg +'</span>';
}
function cleanErrMsg() {
	$("span.retxt").remove();
}
function showErrMsg() {
	$("span.retxt").show();
}
function hideErrMsg() {
	$("span.retxt").hide();
}
function showValidMsg(msgData) {
	$.each(msgData,function(key,value) {
		if("isValid" != key && "validMsg" != key) {
			if($('input[name='+key+']').length > 0) {
				$('input[name='+key+']').next("span.retxt").html(value);
			} else if($('select[name='+key+']').length > 0) {
				$('select[name='+key+']').parent().next("span.retxt").html(value);
			}
		}
	});
	showErrMsg();
};