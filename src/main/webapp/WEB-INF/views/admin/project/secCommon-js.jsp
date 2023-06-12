<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/constants.jsp"%>
<script type="text/javascript">
//==========================================================================================
//COMMON
//==========================================================================================
var totalMainData = [];
var fixedMainData = [];
var notfixedMainData = [];
var saveFlag = false;

var sec_com_evt = {
	dataInit : function(){
		var url = '<c:url value="${suffixUrl}/project/securityGrid/${project.prjId}/total"/>';
		$.ajax({
			url : url,
			type : 'GET',
			dataType : 'json',
			cache : false,
			contentType : 'application/json',
			success : function(data){
				totalMainData = data.totalGridData;
				fixedMainData = data.fixedGridData;
				notfixedMainData = data.notFixedGridData;
				
				// 리로드 대신 그리드 삭제 후 다시 그리기
				$("#totalList").jqGrid('GridUnload');
				total.loadSecurityGrid();
				
				$("#fixedList").jqGrid('GridUnload');
				fixed.loadSecurityGrid();
				
				$("#notFixedList").jqGrid('GridUnload');
				notFixed.loadSecurityGrid();
				
				if (!saveFlag && data.hasOwnProperty('msg')){
					alertify.alert(data.msg, function(){});
				}
				
				saveFlag = false;
			}
		});
	}
}

var sec_com_fn = {
	sendEditor : function(type){
		//코멘트 저장
		var editorVal = CKEDITOR.instances.editor.getData();
		
		if(!editorVal || editorVal == "") {
			alertify.alert('<spring:message code="msg.project.enter.comment" />', function(){});
			return false;
		}
		
		var param = {referenceId : '${project.prjId}', referenceDiv :'10', contents : editorVal, mailSendType : type};
		
		$.ajax({
			url : '/project/sendComment',
			type : 'POST',
			dataType : 'json',
			cache : false,
			data : param,
			success : function(json){
				if(json.isValid == 'false'){
					alertify.error('<spring:message code="msg.common.valid2" />', 0);
				} else {
					$('.ajs-close').trigger("click");

					alertify.success('<spring:message code="msg.project.sent.comments.success" />');
					resetEditor(CKEDITOR.instances.editor);
					
					$(".commentBtn").trigger("click");
				}
			},
			error : function(){
				alertify.error('<spring:message code="msg.common.valid2" />', 0);
			}
		});
	},
	saveEditor : function(){
		//코멘트 임시저장
		var editorVal = CKEDITOR.instances.editor.getData();
		var register = '${sessUserInfo.userId}';
		var param = {referenceId : '${project.prjId}', referenceDiv :'13', contents : editorVal};
		$.ajax({
			url : '/project/saveComment',
			type : 'POST',
			dataType : 'json',
			cache : false,
			data : param,
			success : function(json){
				if(json.isValid == 'false'){
					alertify.error('<spring:message code="msg.common.valid2" />', 0);
				} else {
					alertify.success('<spring:message code="msg.common.success" />');
					$(".commentBtn").trigger("click");
				}
			},
			error : function(){
				alertify.error('<spring:message code="msg.common.valid2" />', 0);
			}
		});
	},
	displayVulnerabilityLink : function(cellvalue, options, rowObject){
		var url = "";
		if(cellvalue != null && cellvalue !== undefined && "" != cellvalue){
			if (cellvalue.indexOf("vulnpopup?") > -1){
				var ossName = rowObject.ossName;
				var ossVersion = rowObject.ossVersion;
				if ("" == ossVersion){
					ossVersion = "-";
				}
				url = '<a href=\'#\' onclick=\'javascript:sec_com_fn.showVulnViewPage(\"'+encodeURIComponent(ossName)+'\",\"'+encodeURIComponent(ossVersion)+'\")\' class=\'urlLink\'>'+cellvalue+'</a>';
			} else {
				var httpVal = cellvalue;
				if( !(
						cellvalue.toLowerCase().startsWith("http://") 
						|| cellvalue.toLowerCase().startsWith("https://") 
						|| cellvalue.toLowerCase().startsWith("ftp://") 
						|| cellvalue.toLowerCase().startsWith("git://") 
					) ) {
					httpVal = "https://" + cellvalue;
				}
				if (httpVal.indexOf(",") > -1){
					httpVal = httpVal.split(",");
					var cnt = httpVal.length;
					for (var i=0; i < httpVal.length; i++){
						var _href = '<c:url value="'+httpVal[i]+'"/>';
						url += "<a href=\""+_href+"\" class=\"urlLink\" target=\"_blank\">"+httpVal[i]+"</a>";
						if (i < httpVal.length-1){
							url += "\n";
						}
					}
				} else {
					var _href = '<c:url value="'+httpVal+'"/>';
					url = "<a href=\""+_href+"\" class=\"urlLink\" target=\"_blank\">"+cellvalue+"</a>";
				}
			}
		}
		
		return url;
	},
	displayLineBreakData : function(cellvalue, options, rowObject){
		var data = "";
		if(cellvalue != null && cellvalue !== undefined && "" != cellvalue){
			var dataInfo = cellvalue;
			if (dataInfo.indexOf(",") > -1){
				dataInfo = dataInfo.split(",");
				for (var i=0; i < dataInfo.length; i++){
					data += dataInfo[i];
					if (i < dataInfo.length-1){
						data += "<br/>";
					}
				}
			} else {
				data = cellvalue;
			}
		}
		
		return data;
	},
	showVulnViewPage : function(_ossName, _ossVersion){
		var _url = '<c:url value="/vulnerability/vulnpopup?ossName='+_ossName+'&ossVersion='+_ossVersion+'"/>';
		
		if(_popupVuln == null || _popupVuln.closed) {
			_popupVuln = window.open(_url, "vulnViewPopup_"+_ossName, "width=950, height=600, toolbar=no, location=no, left=100, top=100");

			if(!_popupVuln || _popupVuln.closed || typeof _popupVuln.closed=='undefined') {
				alertify.alert('<spring:message code="msg.common.window.allowpopup" />', function(){});
			}
		} else {
			_popupVuln.close();
			
			_popupVuln = window.open(_url, "vulnViewPopup_"+_ossName, "width=900, height=600, toolbar=no, location=no, left=100, top=100");
		}
	},
	customOnCellSelect : function(target, iCol){
		if ("8" == iCol) {
			$("#" + target).setColProp('vulnerabilityResolution', {editable:false});
		} 
		if ("11" == iCol) {
			$("#" + target).setColProp('securityPatchLink', {editable:false});
		}
		if ("13" == iCol) {
			$("#" + target).setColProp('securityComments', {editable:false});
		}
	},
	customOnDblClickRow : function(target, rowid){
		var rowData = $("#" + target).jqGrid('getRowData',rowid);
		if ("Y" == rowData.activateFlag) {
			var gridData = $("#" + target).jqGrid('getGridParam','data').filter(function(a){
		    	if("Y" == a.activateFlag){
					return a;
			    }
	    	});
						
			var msg = '<spring:message code="msg.project.security.check.version" />';
			msg += '<br/><br/>';
			for (var i=0; i<gridData.length; i++){
				msg += '- ' + gridData[i].ossName;
				if (i < gridData.length-1){
					msg += '<br/>';
				}
			}
			alertify.alert(msg, function(){});
			$("#" + target).jqGrid("resetSelection");
			return;
		} else {
			if (rowid && rowid !== lastsel){
				$("#" + target).setColProp('vulnerabilityResolution', {editable:true});
				$("#" + target).setColProp('securityPatchLink', {editable:true});
				$("#" + target).setColProp('securityComments', {editable:true});
				$("#" + target).jqGrid('editRow',rowid);
				lastsel=rowid;
			}
			
			var _securityPatchLink = $('#'+rowid+'_securityPatchLink').val();
			var _securityComments = $('#'+rowid+'_securityComments').val();
			
			if (_securityPatchLink.indexOf("<input") > -1){
				$('#'+rowid+'_securityPatchLink').val("");
			} else if (_securityPatchLink.indexOf("<a href") > -1) {
				var text = _securityPatchLink.replace( /(<([^>]+)>)/ig, '');
				$('#'+rowid+'_securityPatchLink').val(text);
			}
			if (_securityComments.indexOf("<input") > -1){
				$('#'+rowid+'_securityComments').val("");
			}
		}
	},
	identificationTab : function(){
		var prjId = $('input[name=prjId]').val();
		var idx = getTabIndex(prjId+"_Identify");
		
		if(idx != ""){
			changeTabInFrame(idx);
		}else{
			createTabInFrame(prjId+'_Identify', '#<c:url value="/project/identification/'+prjId+'/4"/>');
		}
	},
	packagingTab : function(){
		var prjId = $('input[name=prjId]').val();
		var idx = getTabIndex(prjId+"_Packaging");
		
		if(idx != ""){
			changeTabInFrame(idx);
		}else{
			createTabInFrame(prjId+'_Packaging', '#<c:url value="/project/verification/'+prjId+'"/>');
		}
	},
	distributionTab : function(){
		var prjId = $('input[name=prjId]').val();
		var idx = getTabIndex(prjId+"_Distribute");
		
		if(idx != ""){
			changeTabInFrame(idx);
		}else{
			createTabInFrame(prjId+'_Distribute', '#<c:url value="/project/distribution/'+prjId+'"/>');
		}
	},
	editTab : function(){
		var prjId = $('input[name=prjId]').val();
		var idx = getTabIndex(prjId+"_Project");
		
		if(idx != ""){
			changeTabInFrame(idx);
		}else{
			createTabInFrame(prjId+'_Project', '#<c:url value="/project/edit/'+prjId+'"/>');
		}
	}
}
</script>