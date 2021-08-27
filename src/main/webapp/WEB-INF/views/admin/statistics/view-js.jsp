<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/constants.jsp"%>
<script type="text/javascript" src="https://code.highcharts.com/highcharts.src.js"></script>

<script type="text/javascript">
var divisionList = new Array();
var _popup = null;
var isKeyUp = false;
var projectFlag = "${projectFlag}";
var partnerFlag = "${partnerFlag}";

$(document).ready(function(){	
	common_fn.init();
});

var common_fn = {
	init : function(){
		divisionList = '${ct:getCodeNames(ct:getConstDef("CD_USER_DIVISION"))}';
		
		if(typeof divisionList == "string"){
			divisionList = divisionList.replace(/[\[\]]/g, "").split(",");
		}
		
		common_fn.chartReload();
		common_fn.bindEvent();
	},
	bindEvent : function(){
		$("#divisionalProjectChartSelect").on("change", function(){
			if(common_fn.validation("DIV")){
				chart_fn.divisionalProjectChartOpen();
			}
		});

		$("#mostUsedOssChartDivision, #mostUsedOssChartPieSize").on("change", function(){
			chart_fn.mostUsedOssChartOpen();
		});

		$("#mostUsedLicenseChartDivision, #mostUsedLicenseChartPieSize").on("change", function(){
			chart_fn.mostUsedLicenseChartOpen();
		});

		$("#trdPartyRelatedChartSelect").on("change", function(){
			if(common_fn.validation("TRD")){
				chart_fn.trdPartyRelatedChartOpen();
			}
		});

		$("div[id$=Chart]").on("click", function(){
			var id = $(this).attr("id");			
			var url = "/statistics/statisticspopup?" + chart_fn.makeUrl(id);
			
			if(_popup == null || _popup.closed){
				_popup = window.open(url, "statisticsPopup_"+id, "width=900, height=700, toolbar=no, location=no, left=100, top=100, resizable=no");

				if(!_popup || _popup.closed || typeof _popup.closed=='undefined') {
					alertify.alert('<spring:message code="msg.common.window.allowpopup" />', function(){});
				}
			}else{
				_popup.close();

				_popup = window.open(url, "statisticsPopup_"+id, "width=900, height=700, toolbar=no, location=no, left=100, top=100, resizable=no");
			}
		});
		
		$("#schStartDate").blur(function(){
			$("#startDate").val($(this).val());
			if(common_fn.validation("MAIN")){
				common_fn.chartReload();
			}
		});

		$("#schEndDate").blur(function(){
			$("#endDate").val($(this).val());
			if(common_fn.validation("MAIN")){
				common_fn.chartReload();
			}
		});
	},
	chartReload : function(){
		<c:if test="${projectFlag}">
			chart_fn.divisionalProjectChartOpen();
		</c:if>
		chart_fn.mostUsedOssChartOpen();
		chart_fn.mostUsedLicenseChartOpen();
		chart_fn.updatedOssChartOpen();
		chart_fn.updatedLicenseChartOpen();
		<c:if test="${partnerFlag}">
			chart_fn.trdPartyRelatedChartOpen();
		</c:if>
		chart_fn.userRelatedChartOpen();
	},
	validation : function(type){
		var startDate = $("#startDate").val();
		var endDate = $("#endDate").val();
		
		switch(type){
			case "DIV":
				var divisionType = $("#divisionalProjectChartSelect").val();
				if(startDate.length == 0 || endDate.length == 0){
					alertify.alert("날짜값을 입력하세요.", function(){});
					return false;
				}

				if(+startDate > +endDate){
					alertify.alert("날짜 범위 오류", function(){});
					return false;
				}

				if(divisionType.length == 0){
					alertify.alert("관리자에게 문의하세요.", function(){});
					return false;
				}
				
				break;
			case "TRD":
				var divisionType = $("#trdPartyRelatedChartSelect").val();
				if(startDate.length == 0 || endDate.length == 0){
					alertify.alert("날짜값을 입력하세요.", function(){});
					return false;
				}

				if(+startDate > +endDate){
					alertify.alert("날짜 범위 오류", function(){});
					return false;
				}

				if(divisionType.length == 0){
					alertify.alert("관리자에게 문의하세요.", function(){});
					return false;
				}
				break;
			case "MAIN":
				var diffNum = +startDate - +endDate;
				
				if(diffNum > 0 && endDate > 0){
					alertify.alert('<spring:message code="msg.common.search.check.date" />', function(){});

					return false;
				}
				
				break;
			default:
				break;
		}

		return true;
	}
}

var chart_fn = {
		divisionalProjectChartOpen : function (){
			var params = {};
			params["startDate"] = $("#startDate").val();
			params["endDate"] = $("#endDate").val();
			params["categoryType"] = $("#divisionalProjectChartSelect").val();
			params["isRawData"] = "N";
			
			$.ajax({
				url : '/statistics/divisionProjectChart',
				type : 'GET',
				dataType : 'json',
				cache : false,
				data : params,
				success : function(data){
					var data = data.chartData;
					var obj = new Object();
					var chartData = new Array();
					var idx = 0;
					
					for(var title in data.titleArray){
						var result = {
							name : data.titleArray[idx]
						  , color : data.colorArray[idx]
						  , data : data.dataArray[idx] 
						}

						chartData.push(result);
						
						idx++;
					}

					obj["chartId"] = 'divisionalProjectChart';
					obj["categoryList"] = divisionList;
					obj["chartData"] = chartData;
					obj["legend"] = true;
					
					getBarChart(obj);
				},
				error: function(data){
					alertify.error('<spring:message code="msg.common.valid2" />', 0);
				}
			});
		},
		mostUsedOssChartOpen : function(){
			var params = {};
			params["startDate"] = $("#startDate").val();
			params["endDate"] = $("#endDate").val();
			params["divisionNo"] = $("#mostUsedOssChartDivision").val();
			params["pieSize"] = $("#mostUsedOssChartPieSize").val();
			params["chartType"] = "OSS";
			params["isRawData"] = "N";
			
			$.ajax({
				url : '/statistics/mostUsedChart',
				type : 'GET',
				dataType : 'json',
				cache : false,
				data : params,
				success : function(data){
					var data = data.chartData;
					var obj = new Object();
					var chartData = new Array();
					var idx = 0;
					for(var i in data){
						var result = {
								name: data[i].columnName
							  , y: data[i].columnCnt
						}

						chartData.push(result);
						idx++;
					}
					
					obj["chartId"] = 'mostUsedOssChart';
					obj["chartData"] = chartData;
					
					getPieChart(obj);
				},
				error: function(data){
					alertify.error('<spring:message code="msg.common.valid2" />', 0);
				}
			});
		},
		mostUsedLicenseChartOpen : function(){
			var params = {};
			params["startDate"] = $("#startDate").val();
			params["endDate"] = $("#endDate").val();
			params["divisionNo"] = $("#mostUsedLicenseChartDivision").val();
			params["pieSize"] = $("#mostUsedLicenseChartPieSize").val();
			params["chartType"] = "LICENSE";
			params["isRawData"] = "N";
			
			$.ajax({
				url : '/statistics/mostUsedChart',
				type : 'GET',
				dataType : 'json',
				cache : false,
				data : params,
				success : function(data){
					var data = data.chartData;
					var obj = new Object();
					var chartData = new Array();
					var idx = 0;
					for(var i in data){
						var result = {
							name: data[i].columnName
						  , y: data[i].columnCnt
						}

						chartData.push(result);
						idx++;
					}
					
					obj["chartId"] = 'mostUsedLicenseChart';
					obj["chartData"] = chartData;
					
					getPieChart(obj);
				},
				error: function(data){
					alertify.error('<spring:message code="msg.common.valid2" />', 0);
				}
			});
		},
		updatedOssChartOpen : function(){
			var params = {};
			params["startDate"] = $("#startDate").val();
			params["endDate"] = $("#endDate").val();
			params["categoryType"] = "REV";
			params["chartType"] = "OSS";
			params["isRawData"] = "N";
			
			$.ajax({
				url : '/statistics/updatedChart',
				type : 'GET',
				dataType : 'json',
				cache : false,
				data : params,
				success : function(data){
					var data = data.chartData;
					var obj = new Object();
					var chartData = new Array();
					var idx = 0;
					
					for(var title in data.titleArray){
						var result = {
							name : data.titleArray[idx]
						  , color : data.colorArray[idx]
						  , data : data.dataArray[idx] 
						}
						
						chartData.push(result);
						idx++;
					}

					obj["chartId"] = 'updatedOssChart';
					obj["categoryList"] = data.categoryList;
					obj["chartData"] = chartData;
					obj["legend"] = true;
					
					getBarChart(obj);
				},
				error: function(data){
					alertify.error('<spring:message code="msg.common.valid2" />', 0);
				}
			});
		},
		updatedLicenseChartOpen : function(){
			var params = {};
			params["startDate"] = $("#startDate").val();
			params["endDate"] = $("#endDate").val();
			params["categoryType"] = "REV";
			params["chartType"] = "LICENSE";
			params["isRawData"] = "N";
			
			$.ajax({
				url : '/statistics/updatedChart',
				type : 'GET',
				dataType : 'json',
				cache : false,
				data : params,
				success : function(data){
					var data = data.chartData;
					var obj = new Object();
					var chartData = new Array();
					var idx = 0;
					
					for(var title in data.titleArray){
						var result = {
							name : data.titleArray[idx]
						  , color : data.colorArray[idx]
						  , data : data.dataArray[idx] 
						}
						
						chartData.push(result);
						idx++;
					}

					obj["chartId"] = 'updatedLicenseChart';
					obj["categoryList"] = data.categoryList;
					obj["chartData"] = chartData;
					obj["legend"] = true;
				
					getBarChart(obj);
				},
				error: function(data){
					alertify.error('<spring:message code="msg.common.valid2" />', 0);
				}
			});
		},
		trdPartyRelatedChartOpen : function(){
			var params = {};
			params["startDate"] = $("#startDate").val();
			params["endDate"] = $("#endDate").val();
			params["categoryType"] = $("#trdPartyRelatedChartSelect").val();
			params["isRawData"] = "N";
			
			$.ajax({
				url : '/statistics/trdPartyRelatedChart',
				type : 'GET',
				dataType : 'json',
				cache : false,
				data : params,
				success : function(data){
					var data = data.chartData;
					var obj = new Object();
					var chartData = new Array();
					var idx = 0;
					
					for(var title in data.titleArray){
						var result = {
							name : data.titleArray[idx]
						  , color : data.colorArray[idx]
						  , data : data.dataArray[idx] 
						}

						chartData.push(result);
						idx++;
					}

					obj["chartId"] = 'trdPartyRelatedChart';
					obj["categoryList"] = divisionList;
					obj["chartData"] = chartData;
					obj["legend"] = true;
					
					getBarChart(obj);
				},
				error: function(data){
					alertify.error('<spring:message code="msg.common.valid2" />', 0);
				}
			});
		},
		userRelatedChartOpen : function(){
			var params = {};
			params["startDate"] = $("#startDate").val();
			params["endDate"] = $("#endDate").val();
			params["isRawData"] = "N";
			
			$.ajax({
				url : '/statistics/userRelatedChart',
				type : 'GET',
				dataType : 'json',
				cache : false,
				data : params,
				success : function(data){
					var data = data.chartData;
					var obj = new Object();
					var chartData = new Array();
					var idx = 0;
					
					for(var title in data.titleArray){
						var result = {
							name : data.titleArray[idx]
						  , color : data.colorArray[idx]
						  , data : data.dataArray[idx] 
						}
						
						chartData.push(result);
						idx++;
					}

					obj["chartId"] = 'userRelatedChart';
					obj["categoryList"] = divisionList;
					obj["chartData"] = chartData;
					obj["legend"] = false;
					obj["tooltip"] = {
						pointFormatter : function(){
							var series = this.series;
							var s = series.name == 'Total' ? this.total : this.y;
							
							return '<span style="color:'+series.color+'">'+series.name+'</span>: <b>'+s+'</b><br/>';
						},
						shared: true
					};
					
					getBarChart(obj);
				},
				error: function(data){
					alertify.error('<spring:message code="msg.common.valid2" />', 0);
				}
			});
		},
		makeUrl : function(chartName){
			var url = "chartName="+chartName;
			var index = 0;
			var startDate = $("#startDate").val();
			var endDate = $("#endDate").val();

			url += "&startDate=" + startDate + "&endDate=" + endDate;
			
			switch(chartName){
				case "divisionalProjectChart":
				case "trdPartyRelatedChart":
					var selectData = $("#"+chartName+"Select").val();
					url += "&categoryType=" + selectData;

					break;
				case "mostUsedOssChart":
				case "mostUsedLicenseChart":
					var divisionData = $("#"+chartName+"Division").val();
					var pieSize = $("#"+chartName+"PieSize").val();
					var chartType = chartName.replace(/mostUsed(.+)Chart/, "$1").toUpperCase();
					url += "&divisionNo=" + divisionData + "&pieSize=" + pieSize + "&chartType=" + chartType;

					break;
				case "updatedOssChart":
				case "updatedLicenseChart":
					var chartType = chartName.replace(/updated(.+)Chart/, "$1").toUpperCase();
					url += "&categoryType=REV&chartType="+chartType;

					break;
				default:
					break;
			}
			
			return url;
		},
		downloadExcel : function(){
			var params = chart_fn.makeParams();
			
			$.ajax({
				url : '/exceldownload/getChartExcel',
				type: "GET",
				dataType : 'json',
				data : {"params" : params},
				cache : false,
				contentType : 'application/json',
				success: function (data) {
					if("false" == data.isValid) {
				    	alertify.error('<spring:message code="msg.common.valid2" />', 0);
					} else {
						window.location =  '<c:url value="/exceldownload/getFile?id='+data.validMsg+'"/>';
					}
				},
				error: function(data){
					alertify.error('<spring:message code="msg.common.valid2" />', 0);
				}
			});
		},
		makeParams : function(){
			var params = new Array();
			var obj = {};
			var formData = $("#chartForm").serializeArray();
			
			for(var i in formData) {
			    if(formData[i].name.indexOf("StartDate") > -1) {
				    if(Object.keys(obj).length > 0) {
						obj["isRawData"] = "Y";
				    	params.push(obj);
				    	
				    	obj = {};
				    	obj["startDate"] = formData[i].value;

						if(formData[i].name.indexOf("mostUsedOssChart") > -1) {
							obj["chartType"] = "OSS";
						} else if(formData[i].name.indexOf("mostUsedLicenseChart") > -1) {
							obj["chartType"] = "LICENSE";
						} else if(formData[i].name.indexOf("updatedOssChart") > -1) {
							obj["categoryType"] = "REV";
							obj["chartType"] = "OSS";
						} else if(formData[i].name.indexOf("updatedLicenseChart") > -1) {
							obj["categoryType"] = "REV";
							obj["chartType"] = "LICENSE";
						}
				    } else {
				    	obj["startDate"] = formData[i].value;
					}
					
			    }else if(formData[i].name.indexOf("EndDate") > -1){
			    	obj["endDate"] = formData[i].value;
			    }else if(formData[i].name.indexOf("Select") > -1){
			    	obj["categoryType"] = formData[i].value;
				}else if(formData[i].name.indexOf("Division") > -1){
					obj["divisionNo"] = formData[i].value;
				}else if(formData[i].name.indexOf("PieSize") > -1){
					obj["pieSize"] = formData[i].value;
				}
			}
			
			obj["isRawData"] = "Y";
			
	    	params.push(obj);

	    	return JSON.stringify(params);
		}
}
</script>