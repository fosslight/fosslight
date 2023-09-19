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
var prevEditSel;

var sec_com_evt = {
	dataInit : function(){
		var url = '<c:url value="/project/securityGrid/${project.prjId}/total"/>';
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
	customOnCellSelect : function(target, iCol){
		$("#" + target).jqGrid('saveRow', prevEditSel);
		lastsel = -1;
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
				$("#" + target).jqGrid('editRow',rowid);
				lastsel=rowid;
			}
		}
		
		prevEditSel = rowid;
	},
	setSaveGridData : function(targetStr) {
		var target = $("#"+targetStr);
		var rowDatas = target.jqGrid("getRowData");
		var totalGridData = target.jqGrid('getGridParam','data');
		var rowDatasLength = rowDatas.length;
		var totalGridDataLength = totalGridData.length;
		
		for (var i=0; i<rowDatasLength; i++) {
			for (var j=0; j<totalGridDataLength; j++) {
				if (rowDatas[i].gridId == totalGridData[j].gridId) {
					totalGridData[j].vulnerabilityResolution = rowDatas[i].vulnerabilityResolution;
					continue;
				}
			}
		}
		
		return totalGridData;
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
	editTab : function(){
		var prjId = $('input[name=prjId]').val();
		var idx = getTabIndex(prjId+"_Project");
		
		if(idx != ""){
			changeTabInFrame(idx);
		}else{
			createTabInFrame(prjId+'_Project', '#<c:url value="/project/edit/'+prjId+'"/>');
		}
	},
	bulkEdit : function(tab){
        var gridList;
        var targetGird = "";
        
        switch(tab){
            case 'total' : 
                gridList = $("#totalList"); targetGird = "totalList";
                break;
            case 'fixed' : 
                gridList = $("#fixedList"); targetGird = "fixedList";
                break;
            case 'notFixed' : 
                gridList = $("#notFixedList"); targetGird = "notFixedList";
                break;
        }

        var selarrrow = gridList.jqGrid("getGridParam", "selarrrow");
        var rowCheckedArr = [];
        var rowGirdIdArr = [];
        var versionEmptyFlag = false;
        
        for(var i=0; i<selarrrow.length; i++){
        	if($("input:checkbox[id='jqg_" + targetGird + "_" + selarrrow[i] + "']").is(":checked")){
        		var rowData = gridList.jqGrid("getRowData", selarrrow[i]);
        		if ("" != rowData["ossVersion"]) {
        			rowCheckedArr.push(selarrrow[i]);
        			rowGirdIdArr.push(rowData["gridId"]);
        		}
			}
        }
		
        if (rowCheckedArr.length > 0) {
            fn_grid_com.totalGridSaveMode(targetGird);
			var _popupFlag = false;
			var _popup = null;
			
            if(_popup == null || _popup.closed){
            	_popup = window.open("", "bulkEditViewSecurityPopup", "width=725, height=220, toolbar=no, location=no, left=150, top=150, resizable=yes");

            	$("#secBulkEditForm > input[name=rowId]").val(rowCheckedArr);
            	$("#secBulkEditForm > input[name=gridId]").val(rowGirdIdArr);
    			$("#secBulkEditForm > input[name=target]").val(targetGird);
    			$("#secBulkEditForm").submit();
            	
            	if(!_popup || _popup.closed || typeof _popup.closed=='undefined') {
                    alertify.alert('<spring:message code="msg.common.window.allowpopup" />', function(){});
                }
            } else {
                _popup.close();
                _popup = window.open("", "bulkEditViewSecurityPopup", "width=725, height=220, toolbar=no, location=no, left=150, top=150, resizable=yes");

                $("#secBulkEditForm > input[name=rowId]").val(rowCheckedArr);
                $("#secBulkEditForm > input[name=gridId]").val(rowGirdIdArr);
				$("#secBulkEditForm > input[name=target]").val(targetGird);
				$("#secBulkEditForm").submit();
            }
        } else {
        	alertify.alert('<spring:message code="msg.oss.select.ossTable" />', function(){});
        	return false;
        }
    },
    bulkEditDataInfo : function(obj, flag){
		var editFlag = false;
	   	try{
    		var ossArr = [];
            var gridId = obj["gridId"];
            var target = obj["target"];
            var gridParam = $('#'+target).jqGrid('getGridParam','data');
            var customParam = [];
            var param = [];
            
            var limit = $('.ui-pg-selbox option:selected').val();
            var page = $('.ui-pg-input').val();
            var start = (parseInt(page) * parseInt(limit)) - parseInt(limit);
            
            if (parseInt(start) > 0) {
            	for (var i=0; i<start; i++){
            		customParam.push(gridParam[i]);
            	}
            }
            
            for (var i=start; i<gridParam.length; i++) {
            	param.push(gridParam[i]);
            }
            
            if (gridId.indexOf(",") > -1) {
                var gridIdArr = gridId.split(",");
                
                for (var idx in gridIdArr) {
                	for (var i=0; i<param.length; i++) {
                    	if (param[i]["gridId"] == gridIdArr[idx]) {
                            for (var key in obj) {
                                if ("rowId" != key && "target" != key && "gridId" != key) {
                                	param[i][key] = obj[key];
                                }
                            }
                        }
                    }
                }
            } else {
            	for (var i=0; i<param.length; i++) {
                    if (param[i]["gridId"] == gridId) {
                        for (var key in obj) {
                        	if ("rowId" != key && "target" != key && "gridId" != key) {
                        		param[i][key] = obj[key];
                            }
                        }
                    }
                }
            }
            
            var reloadParam = [];
            if (customParam.length > 0) {
            	for (var i in customParam) {
            		reloadParam.push(customParam[i]);
            	}
            }
            
            for (var i in param) {
        		reloadParam.push(param[i]);
        	}
            
            $("#"+target).jqGrid('setGridParam', {data:reloadParam}).trigger('reloadGrid');
    	} catch(e) {
    		alertify.error('<spring:message code="msg.common.valid2" />', 0);
    		editFlag = true;
    	} finally {
        	if(!editFlag){
        		alertify.success('<spring:message code="msg.common.success" />');
            }
       	}
    }
}
</script>