<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/constants.jsp"%>
<script type="text/javascript">
// var Party_Load_Data, Party_Last_Data;
// var Src_Load_Data, Src_Last_Data;
// var Bat_Load_Data, Bat_Last_Data;
// bom 저장 여부
var Bom_Save_Flg = true;
var activeTab = "";
var activeTabText = "";
// 서브 그리드 url 전송 설정 
// var subGridUrl = true;
var subGridPostData = "";
// load to list 플레그
var loadBln = false;
var isSort = false;
//2018-08-10 choye 추가
var rdData;

$(document).ready(function () {
	'use strict';
	
	com_evt.init();
	<c:if test="${partnerFlag}">
		party_evt.init();
	</c:if>
	src_evt.init();
	bin_evt.init();
	bom_evt.init();
	//bat_evt.init();
});

//==========================================================================================
// PROJECT 3rd
//==========================================================================================
	
var partMainData;
var partValidData;
var partDiffData;
var partInfoData;
var partSubData;

var party_evt = {
	init : function(){
		party_evt.getPartGridData();
		//Not aplicable
		var aplicable = $('#applicableParty').is(':checked');
		
		if(aplicable){
			$('.partyBtn').hide();
		}
		
		// partner search 클릭
		$('#3rdSearchBtn').on('click', function(e){
			e.preventDefault();
			
			var postData = $('#3rdSearchForm').serializeObject();
			
			$('#_list').jqGrid('setGridParam', {postData:postData}).trigger('reloadGrid');
			$('.three1list').show();
			$('.three2list').hide();
		});
		
		// project search 클릭
		$('#3rdProjectSearchBtn').on('click', function(e){
			e.preventDefault();
			
			var postData = $('#projectSearchForm').serializeObject();
			
			$('#_list-1').jqGrid('setGridParam', {postData:postData}).trigger('reloadGrid');
			$('.three3list').show();
			$('.three4list').hide();
		});
		
		// 그리드 로드
		$('#_list').jqGrid(party_evt.setParamParty1());
		$('#_list2').jqGrid(party_evt.setParamParty2());
		$('#_3rdAddList').jqGrid(party_evt.setParamParty3());
	},
	getPartGridData : function(param){
		$.ajax({
			url : '<c:url value="/project/identificationThird"/>',
			dataType : 'json',
			cache : false,
			data : (param) ? param : {referenceId : '${project.prjId}'},
			contentType : 'application/json',
			success : function(data){
				partMainData = data.rows;
				
				// 리로드 대신 그리드 삭제 후 다시 그리기
				$("#list3").jqGrid('GridUnload');
				part_grid.load();
				cleanErrMsg("list3");
				
				if(data.validData) {
					partValidData = data.validData;
				}
				
				if(data.diffData) {
					partDiffData = data.diffData;
				}
				
				if(data.infoData) {
					partInfoData = data.infoData;
				}
				
				fn_grid_com.addEtcKeyDownEvent($('#list3'), partValidData, partDiffData, partInfoData);
				
				if(partValidData) {
					gridValidMsgNew(partValidData, "list3");
				}
				
				if(partDiffData) {
					gridDiffMsg(partDiffData, "list3");
				}
				
				if(partInfoData) {
					gridInfoMsg(partInfoData, "list3");
				}
			},
			error : function(){
				alertify.error('<spring:message code="msg.common.valid2" />', 0);
			}
		});
	},
	changeSelectOption : function(){
		var value = $('input[name=selectOption]:checked').val();
		
		if(value == 1) {
			$('#3rdSearch').show();
			$('#projectSearch').hide();
			$('.3rdProjectSearch').show();
			$('.3rdProjectSearch2').hide();
			$('#_list').jqGrid(party_evt.setParamParty1());
			$('#_list2').jqGrid(party_evt.setParamParty2());
		} else {
			$('#3rdSearch').hide();
			$('#projectSearch').show();
			$('.3rdProjectSearch').hide();
			$('.3rdProjectSearch2').show();
			$('#_list-1').jqGrid(party_evt.setParamProject1());
			$('#_list-2').jqGrid(party_evt.setParamProject2());
			$('#_list-2').jqGrid('filterToolbar',{stringResult: true, searchOnEnter: true, searchOperators: true, defaultSearch: "cn"});
		}
	},
	loadToList : function(){
		var data = jQuery("#_list2").jqGrid("getRowData");
		var selrow = $('#_list').jqGrid('getGridParam', "selrow" );
		var listData = $('#_list').jqGrid('getRowData',selrow);
		var addListData = $("#_3rdAddList").jqGrid("getRowData");
		
		if(addListData.length > 0){
			for(var i=0;i<addListData.length;i++){
				if(listData.partnerId == addListData[i].partnerId){
			    	alertify.alert('<spring:message code="msg.id.duplicate" />', function(){});

			    	return;
			    }
			}
		}
		
		var cnt = $('#_list2').jqGrid('getGridParam', "records" );
		var ids = $('#_3rdAddList').jqGrid('getDataIDs');
		listData.componentCount = cnt;
		$('#_3rdAddList').jqGrid('addRowData',ids.length+1,listData);
		var reference = "";

		if(data.length > 0){
			reference = data[0].referenceId;
		}
		
		$.ajax({
			url : '<c:url value="/project/3rdOss"/>',
			type : 'GET',
			dataType : 'json',
			cache : false,
			data : {referenceId : reference},
			contentType : 'application/json',
			success : function(data){
				var data = data.rows;
				var _tempRandId = "";
				var newData = {};

				for(var i = 0; i < data.length; i++){
					_tempRandId = $.jgrid.randId();
					
					newData = {
						  gridId : _tempRandId 
						, componentId : null 
						, referenceId : '${project.prjId}'
						, refPartnerName : data[i].refPartnerName
						, referenceDiv : '10'
						, ossId : data[i].ossId
						, ossName : data[i].ossName
						, ossVersion : data[i].ossVersion
						, downloadLocation : data[i].downloadLocation
						, homepage : data[i].homepage
						, filePath : data[i].filePath
						, licenseName : data[i].licenseName
						, copyrightText : data[i].copyrightText
						, refPartnerId :data[i].referenceId
						, refComponentId : data[i].refComponentId
						, excludeYn : data[i].excludeYn
						, cvssScore : data[i].cvssScore
						, cveId : data[i].cveId
					}
					
					$('#list3').jqGrid('addRowData', _tempRandId, newData);
				}
				
				$('.three2list').hide();
			},
			error : function(){
				alertify.error('<spring:message code="msg.common.valid2" />', 0);
			}
		});
	},
	// 3rd part project search에서 load
	loadToList2 : function(){
		var data = jQuery("#_list-2").jqGrid("getRowData");
		var reference = data[0].referenceId;
		
		$.ajax({
			url : '<c:url value="/project/partnerOssFromProject"/>',
			type : 'GET',
			dataType : 'json',
			cache : false,
			data : {referenceId : reference},
			contentType : 'application/json',
			success : function(data){
				var data = data.rows;
				var _tempRandId = "";
				var newData = {};
				
				for(var i = 0; i < data.length; i++){
					_tempRandId = $.jgrid.randId();
					
					newData = {
						gridId : _tempRandId 
						, componentId : null 
						, referenceId : '${project.prjId}'
						, refPartnerName : data[i].refPartnerName
						,  referenceDiv : '10'
						, ossId : data[i].ossId
						, ossName : data[i].ossName
						, ossVersion : data[i].ossVersion
						, downloadLocation : data[i].downloadLocation
						, homepage : data[i].homepage
						, filePath : data[i].filePath
						, licenseName : data[i].licenseName
						, copyrightText : data[i].copyrightText
						, refPartnerId :data[i].refPartnerId
						, refComponentId : data[i].refComponentId
						, excludeYn : data[i].excludeYn
						, cvssScore : data[i].cvssScore
						, cveId : data[i].cveId
					}
					
					$('#list3').jqGrid('addRowData', _tempRandId, newData);
				}
				
				$('.three3list').hide();
				$('.three4list').hide();
			},
			error : function(){
				alertify.error('<spring:message code="msg.common.valid2" />', 0);
			}
		});
	},	
	reset : function(){
 		alertify.confirm('<spring:message code="msg.common.confirm.reset" />', function (e) {
			if (e) {
				$("#list3").jqGrid('clearGridData');
				$("#_3rdAddList").jqGrid('clearGridData');
			} else {
				return false;
			}
		});
	},
	save : function(){
		if (com_fn.checkStatus()){
			alertify.confirm('<spring:message code="msg.common.confirm.save" />', function (e) {
				if (e) {
					var mainData = $('#list3').jqGrid('getGridParam','data');
					var thirdPartyDiv = $('#_3rdAddList').jqGrid('getRowData');
					var finalData = {
						referenceId : '${project.prjId}',
						identificationSubStatusPartner : $("#applicableParty:checked").val(),
						mainData : JSON.stringify(mainData),
						thirdPartyData : JSON.stringify(thirdPartyDiv)
					}
					
					$.ajax({
						url : '<c:url value="/project/save3rd"/>',
						type : 'POST',
						dataType : 'json',
						cache : false,
						data : JSON.stringify(finalData),
						contentType : 'application/json',
						success : function(data){
							alertify.success('<spring:message code="msg.common.success" />');
							var param = {referenceId : '${project.prjId}'}
							party_evt.getPartGridData(param);
							$("#_3rdAddList").jqGrid('setGridParam', {postData:param}).trigger('reloadGrid');
							$("#mergeYn").val("N");
							
							if(curIdenStatus == ""){
								curIdenStatus = "PROG";
								com_fn.btnCtl(userRole, curIdenStatus);
							}
						},
						error : function(){
							alertify.error('<spring:message code="msg.common.valid2" />', 0);
						}
					});
				} else {
					return false;
				}
			});
		}else {
			alertify.alert('<spring:message code="msg.project.warn.third.party.status" />', function(){});
		}
	},
	setParamParty1 : function(){
		return {
			url: '<c:url value="/project/getPartnerList"/>',
			datatype: 'json',
			jsonReader:{
				repeatitems: false,
				id: 'partnerId',
				root:function(obj){return obj.rows;},
				page:function(obj){return obj.page;},
				total:function(obj){return obj.total;},
				records:function(obj){return obj.records;}
			},
			colNames: ['ID','3rd party Name','Software Name','Software Version', 'Delivery Form','Description'],
			colModel: [
				{name: 'partnerId', index: 'partnerId', width: 42, align: 'center', key:true},
				{name: 'partnerName', index: 'partnerName', width: 240, align: 'left'},
				{name: 'softwareName', index: 'softwareName', width: 200, align: 'left'},
				{name: 'softwareVersion', index: 'softwareVersion', width: 110, align: 'left'},
				{name: 'deliveryForm', index: 'deliveryForm', width: 110, align: 'center'},
				{name: 'description', index: 'description', width: 249, align: 'left'},
			],
			onSelectRow: function(id){
				var postData = {referenceId : id};
				$('#_list2').jqGrid('setGridParam', {postData:postData}).trigger('reloadGrid');
				$('.three2list').show();
			},
			autoencode: true,
			rowNum: 20,
			rowList: [20, 40, 60],
			pager: '#pager',
			autowidth: true,
			gridview: true,
			sortable: function(permutation){
			},
			sortname: 'partnerId',
			viewrecords: true,
			sortorder: 'desc',
			loadonce: false,
			height: 'auto',
			mtype: 'GET',
			loadComplete: function(data){
				if(data != null){
					data = data.rows;
					for(var i=0; i<data.length; i++){
						if(data[i].deliveryForm){
							if(data[i].deliveryForm == 'SRC'){
								$("#_list").jqGrid("setCell", data[i].partnerId, "deliveryForm",'source form');
							}else if(data[i].deliveryForm == 'BIN'){
								$("#_list").jqGrid("setCell", data[i].partnerId, "deliveryForm",'binary form');
							}
						}
					}					
				}
			}
		}
	},
	setParamParty2 : function(){
		return {
			url: '<c:url value="/project/3rdOss"/>',
			datatype: 'json',
			jsonReader:{
				repeatitems: false,
				id: 'componentId',
			},
			colNames: ['ID_KEY','ID','ReferenceId','OSSID','OSS Name','OSS Version','Download Location', 'Homepage','License', 'Copyright Text', 'Path'],
			colModel: [
				{name: 'componentId', index: 'componentId', width: 50, align: 'center', key:true, hidden:true},
				{name: 'componentIdx', index: 'componentIdx', width: 50, align: 'center'},
				{name: 'referenceId', index: 'referenceId', width: 50, align: 'center', hidden:true},
				{name: 'ossId', index: 'ossId', width: 50, align: 'center', hidden:true},
				{name: 'ossName', index: 'ossName', width: 220, align: 'left'},
				{name: 'ossVersion', index: 'ossVersion', width: 80, align: 'center'},
				{name: 'downloadLocation', index: 'downloadLocation', width: 80, align: 'left', formatter: fn_grid_com.displayUrl, unformat: fn_grid_com.unDisplayUrl},
				{name: 'homepage', index: 'homepage', width: 80, align: 'left', formatter: fn_grid_com.displayUrl, unformat: fn_grid_com.unDisplayUrl},
				{name: 'licenseName', index: 'licenseName', width: 130, align: 'left',formatter: fn_grid_com.displayLicense},
				{name: 'copyrightText', index: 'copyrightText', width: 108, align: 'left',formatter: fn_grid_com.displayLicense},
				{name: 'filePath', index: 'filePath', width: 113, align: 'left'},
			],
			autoencode: true,
			onSelectRow: function(id){
			},
			autowidth: true,
			gridview: true,
			sortable: function(permutation){
			},
			sortname: 'componentId',
			viewrecords: true,
			sortorder: 'desc',
			loadonce: false,
			height: 'auto',
			mtype: 'GET',
			loadComplete: function(data){
				
			}
		}
	},
	setParamProject1 : function(){
		return {
			url: '<c:url value="/project/identificationProject/10"/>',
			datatype: 'json',
			jsonReader:{
				repeatitems: false,
				id: 'prjId',
				root:function(obj){return obj.rows;},
				page:function(obj){return obj.page;},
				total:function(obj){return obj.total;},
				records:function(obj){return obj.records;}
			},
			colNames: ['ID','Project Name','Project Version','Distribution Type', 'Creator','Created Date', 'Comment'],
			colModel: [
				{name: 'prjId', index: 'prjId', width: 42, align: 'center', sorttype: 'int', key:true},
				{name: 'prjName', index: 'prjName', width: 240, align: 'left'},
				{name: 'prjVersion', index: 'prjVersion', width: 200, align: 'left'},
				{name: 'distributionType', index: 'distributionType', width: 110, align: 'left'},
				{name: 'creator', index: 'creator', width: 110, align: 'center'},
				{name: 'createdDate', index: 'createdDate', width: 110, align: 'center', formatter:'date', formatoptions: {srcformat: 'Y-m-d H:i:s.t', newformat: 'Y-m-d'}},
				{name: 'comment', index: 'comment', width: 139, align: 'left', formatter: fn_grid_com.displayComment, unformat: fn_grid_com.unFormatter}
			],
			onSelectRow: function(id){
				party_evt.identificationProjectSearch(id);
				
				$('.three4list').show();
			},
			autoencode: true,
			autowidth: true,
			gridview: true,
			sortable: function(permutation){
			},
			rowNum: 20,
			rowList: [20, 40, 60],
			pager: '#pager',
			sortname: 'prjId',
			viewrecords: true,
			sortorder: 'desc',
			loadonce: false,
			height: 'auto',
			mtype: 'GET',
			loadComplete: function(data){
				$('.commentDiv').find('p').css('margin','0px 0px');
			}
		}
	},
	setParamProject2 : function(){
		return {
			datatype: 'local',
			data : rdData,
			colNames: ['ID_KEY','ID','ReferenceId','Binary Name or Source Path','OSS Name','OSS Version','License', 'Path','Vulnerability','Exclude'],
			colModel: [
				{name: 'componentId', index: 'componentId', width: 40, align: 'center', key:true, hidden:true},
				{name: 'componentIdx', index: 'componentIdx', width: 40, align: 'center', sorttype: 'int', search: false},
				{name: 'referenceId', index: 'referenceId', width: 40, align: 'center', hidden:true},
				{name: 'filePath', index: 'filePath', width: 168, align: 'left', template: searchStringOptions},
				{name: 'ossName', index: 'ossName', width: 200, align: 'left', template: searchStringOptions},
				{name: 'ossVersion', index: 'ossVersion', width: 150, align: 'center', template: searchStringOptions},
				{name: 'licenseName', index: 'licenseName', width: 150, align: 'left', formatter: fn_grid_com.displayLicense, template: searchStringOptions},
				{name: 'binaryName', index: 'binaryName', width: 150, align: 'left', template: searchStringOptions},
				{name: 'cvssScore', index: 'cvssScore', width: 98, align: 'center', formatter:fn_grid_com.displayVulnerability, unformatter:fn_grid_com.unformatter, sortable : true, sorttype:'float', template: searchNumberOptions},
				{name: 'excludeYn', index: 'excludeYn', width: 40, align: 'center', hidden:true}
			],
			onSelectRow: function(id){},
			autoencode: true,
			autowidth: true,
			gridview: true,
			sortable: function(permutation){},
			rowNum: 10000,
			rowTotal: -1,
			scroll: 1,
 			sortname: 'componentIdx',
			viewrecords: true,
			loadonce: true,
			height: '500',
			mtype: 'GET',
			loadComplete: function(data){},
			gridComplete:function (){
				//2018-08-10 choye 추가
				var target = $("#_list-2");
				var data = target.jqGrid("getRowData");
				for(var i=0; i<data.length; i++){
					if(data[i].excludeYn=="Y"){
						target.jqGrid('setRowData', data[i].componentId, false, { background:'gray'});
					}
				}
			}
		}
	},
	setParamParty3 : function(){
		return {
			url: '<c:url value="/project/get3rdMap"/>',
			datatype: 'json',
			postData : {prjId : '${project.prjId}'},
			jsonReader:{
				repeatitems: false,
				id: 'partnerId',
				root:function(obj){return obj.rows;},
				page:function(obj){return obj.page;},
				total:function(obj){return obj.total;},
				records:function(obj){return obj.records;}
			},
			colNames: ['ID','3rd party Name','Software Name','Software Version', 'Component Count', 'userRole', 'Delete'],
			colModel: [
				{name: 'partnerId', index: 'partnerId', width: 57, align: 'center', key:true},
				{name: 'partnerName', index: 'partnerName', width: 327, align: 'left'},
				{name: 'softwareName', index: 'softwareName', width: 262, align: 'left'},
				{name: 'softwareVersion', index: 'softwareVersion', width: 120, align: 'left'},
				{name: 'componentCount', index: 'componentCount', width: 110, align: 'center'},
				{name: 'userRole', index: 'userRole', width: 110, align: 'center', hidden:true},
				{name: 'delete', index: 'delete', width: 75, align: 'center', formatter: com_fn.displayDelete}
			],
			onSelectRow: function(id){},
			autoencode: true,
			gridview: true,
			sortname: 'partnerId',
			viewrecords: true,
			sortorder: 'desc',
			loadonce: false,
			height: 'auto',
			mtype: 'GET',
			loadComplete: function(data){
				_mainLastsel = -1;
				
				if(data.rows.length > 0) {
					var multRowIds = []; 
					var rowIdx = 0, rows = this.rows, rowsCount = rows.length, row, rowid, rowData, className;
					
					for(var _idx=0;_idx<rowsCount;_idx++) {
						row = rows[_idx];
						className = row.className;
						
						if (className.indexOf('jqgrow') !== -1) {
							rowid = row.id;
							rowData = data.rows[rowIdx++];
							
							if(rowData.userRole == "N" && className.indexOf('excludeRow') === -1) {
								className= className + ' excludeRow';
							}
							
							row.className = className;
						} else if(className.indexOf('ui-subgrid') !== -1){
							rowIdx++;
						}
					}
				}
			},
			ondblClickRow: function(rowid,iRow,iCol,e) {
				//해당 3rd party tab을 연다
				if(rowid) {
					var _partnerId = $('#_3rdAddList').jqGrid('getCell',rowid,'partnerId');
					var _userRole = $('#_3rdAddList').jqGrid('getCell',rowid,'userRole');
					
					if(_partnerId && _userRole != "N") {
						createTabInFrame(_partnerId+'_3rdParty', '#<c:url value="/partner/edit/'+_partnerId+'"/>');
					}
				}
			}
		}
	},
	identificationProjectSearch : function(id){
		//2018-07-25 choye 추가
		$("#_list-2").jqGrid('clearGridData');
		var postData = $("#_list-2").jqGrid('getGridParam', 'postData');
		postData.referenceId = id;

	    $.ajax({
	    	url : '<c:url value="/project/identificationProjectSearch/10"/>',
            type : 'GET',
            dataType : 'json',
            data : postData,
            cache : false,
            success : function(data){
				rdData = data.rows;
				var target = $("#_list-2");
				
				target.jqGrid('setGridParam', {data:rdData}).trigger('reloadGrid');
            },
            error : function(xhr, ajaxOptions, thrownError){
                alertify.error('<spring:message code="msg.common.valid2" />', 0);
            }
        });
    },
    projectToAddList : function(){
    	var data = jQuery("#_list-2").jqGrid("getRowData");
		var reference = data[0].referenceId;
		$.ajax({
			url : '<c:url value="/project/projectToAddList"/>',
			type : 'GET',
			dataType : 'json',
			cache : false,
			data : {prjId : reference},
			contentType : 'application/json',
			success : function(data){
				var rows = data.rows
				  , _tempRandId = ""
				  , newData = {}
				  , addListData = $("#_3rdAddList").jqGrid("getRowData")
				  , partnerIds = [];
				
				for(var i = 0; i < rows.length; i++){
					if(addListData.length > 0){
						partnerIds = $("#_3rdAddList").jqGrid("getRowData").map(function(a){return a.partnerId});
						
						if(partnerIds.indexOf(rows[i].partnerId) > -1){
					    	alertify.alert('<spring:message code="msg.id.duplicate" />', function(){}); // 기등록된 정보는 제외

					    	continue;
					    }
					}
					
					_tempRandId = $.jgrid.randId();
					
					newData = {
							partnerId : rows[i].partnerId
						  , partnerName : rows[i].partnerName
						  , softwareName : rows[i].softwareName
						  , softwareVersion : rows[i].softwareVersion
						  , componentCount : rows[i].componentCount
					}
					
					$('#_3rdAddList').jqGrid('addRowData', _tempRandId, newData);
				}
				
				party_evt.loadToList2(); // addList 등록후 list3에 등록 처리함.
			},
			error : function(){
				alertify.error('<spring:message code="msg.common.valid2" />', 0);
			}
		});
    },
    removeList : function(removeRow, refPartnerId){
    	$('#_3rdAddList').jqGrid('delRowData', removeRow); // Loaded List - delete row
    	var mainData = $('#list3').jqGrid(('getGridParam','data'));
    	
    	mainData
	    	.filter(function(a){ 
	    		return a.refPartnerId == refPartnerId 
	    	})
	    	.forEach(function(cur,idx){
	    		$('#list3').jqGrid('delRowData', cur.gridId);
	    	});
    }
}

var part_grid = {
		load: function(){
			var currentOssName = "";
			var ondblClickRowBln = false;
			var partList = $("#list3");
			
			partList.jqGrid({
				datatype: 'local',
				data : partMainData,
				colNames: ['gridId','ID_KEY','ID','3rd Party', 'Binary Name or Source Path','ReferenceId','RefPartnerId','RefPrjId','ReferenceDiv', 'OssId','OSS Name','OSS Version','License','Download Location'
							, 'Homepage','LicenseId','Copyright Text', 'Vulnera<br/>bility', 'CVE ID'	
							, '<input type="checkbox" onclick="fn_grid_com.onCboxClickAll(this,\'list3\');">Exclude', 'refComponentId'],
				colModel: [
					{name: 'gridId', index: 'gridId', key:true, editable:false, hidden:true},
					{name: 'componentId', index: 'componentId', width: 40, align: 'center', hidden:true},
					{name: 'componentIdx', index: 'componentIdx', width: 40, align: 'center', sorttype: 'int', search: false},
					{name: 'refPartnerId', index: 'refPartnerId', width: 150, align: 'left', formatter:fn.partnerFormat},
					{name: 'filePath', index: 'filePath', width: 190, align: 'left'},
					{name: 'referenceId', index: 'referenceId', width: 29, align: 'center', hidden:true},
					{name: 'refPartnerId', index: 'refPartnerId', width: 29, align: 'center', hidden:true},
					{name: 'refPrjId', index: 'refPrjId', width: 29, align: 'center', hidden:true},
					{name: 'referenceDiv', index: 'referenceDiv', width: 29, align: 'center', hidden:true},
					{name: 'ossId', index: 'ossId', width: 29, align: 'center', editable:true, hidden:true},
					{name: 'ossName', index: 'ossName', width: 150, align: 'left'},
					{name: 'ossVersion', index: 'ossVersion', width: 50, align: 'left'},
					{name: 'licenseName', index: 'licenseName', width: 150, align: 'left', edittype:'text',formatter: fn_grid_com.displayLicense},
					{name: 'downloadLocation', index: 'downloadLocation', width: 100, align: 'left', formatter: fn_grid_com.displayUrl, unformat: fn_grid_com.unDisplayUrl},
					{name: 'homepage', index: 'homepage', width: 100, align: 'left', formatter: fn_grid_com.displayUrl, unformat: fn_grid_com.unDisplayUrl},
					{name: 'licenseId', index: 'licenseId', width: 29, align: 'center', edittype:'text',formatter: fn_grid_com.displayLicense, hidden:true},
					{name: 'copyrightText', index: 'copyrightText', width: 105, align: 'left', edittype:"textarea", editoptions:{rows:"2",cols:"24"},formatter: fn_grid_com.displayLicense},
					{name: 'cvssScore', index: 'cvssScore', width: 50, align: 'center', formatter:fn_grid_com.displayVulnerability, unformatter:fn_grid_com.unformatter, sortable : false},
					{name: 'cveId', index: 'cveId', editable:false, hidden:true},
					{name: 'excludeYn', index: 'excludeYn', width: 60, align: 'center', formatter: fn_grid_com.cboxFormatter, unformat: fn_grid_com.cboxUnFormatter, sortable : false},
					{name: 'refComponentId', index: 'refComponentId', width: 40, align: 'center', hidden:true},
				],
				autoencode: true,
				editurl:'clientArray',
	 			autowidth: true,
				height: 'auto',
				gridview: true,
			   	pager: '#pager3',
			   	pgbuttons: false,
			   	pgtext: false,
			   	pginput:false,
			   	rowNum:${ct:getConstDef("DISP_PAGENATION_MAX")},
				loadonce:true,
				cellEdit : true,
				cellsubmit : 'clientArray',
				ignoreCase: true,
			    onSortCol: function (index, columnIndex, sortOrder) {},
				loadComplete: function(data) {
					com_fn.exeLoadToList("list3","10");
					
					// exclude에 따른 음영설정
					fn_grid_com.checkExclude(data, 'list3');
				},
				onCellSelect: function(rowid,iCol,cellcontent,e) {
					if(iCol=="2") fn_grid_com.showOssViewPage(partList, rowid);
				},
				gridComplete : function() {
					cleanErrMsg("list3");
					
					if(partValidData) {
						gridValidMsgNew(partValidData, "list3");
					}
					
					if(partDiffData) {
						gridDiffMsg(partDiffData, "list3");
					}
					
					if(partInfoData) {
						gridInfoMsg(partInfoData, "list3");
					}				
				}
			});
			$("#list3").jqGrid('filterToolbar',{stringResult: true, searchOnEnter: true, searchOperators: true, defaultSearch: "cn"});
			$("#list3").jqGrid('navGrid',"#pager3",{add:false,edit:false,del:false,search:false,refresh:false});
		},
	}
	
var fn = {
	partnerFormat : function(cellvalue, options, rowObject){
		var partnerId = rowObject.refPartnerId;
		var partnerName = rowObject.refPartnerName;
		
		return "("+partnerId+") " + partnerName;
	}
}
</script>
