<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/constants.jsp"%>
<script type="text/javascript">
//<![CDATA[
	/*global $ */
	/*jslint browser: true, nomen: true */
	const dashboardFlag = "${dashboardFlag}";
	const statisticsFlag = "${statisticsFlag}";
	const projectFlag = "${projectFlag}";
	const partnerFlag = "${partnerFlag}";
	const batFlag = "${batFlag}";
	const bianrydbFlag = "${binarydbFlag}";
	const complianceStatusFlag = "${complianceStatusFlag}";
	const externalLinkFlag = "${externalLinkFlag}";
	
	$(document).ready(function () {
		'use strict';
		config_fn.init();
	});	
	
	// 함수
	var config_fn = {
		init : function(){
			//var addBtn = '<input id="urlAdd" type="button" value="Add" class="btnCLight gray">';
			//var delBtn = '<input id="urlDel" type="button" value="Delete" class="btnCLight gray">';
			//$("#config925 > .detailArea > dl").after(addBtn +'&nbsp;' + delBtn);
			$("#projectConfig > .detailArea").show();
			config_evt.init();
			config_fn.viewConfigSetting();

			/*
			$("dd[id*=config]").each(function(idx,cur){
			    var id = $(cur).attr("id");
			    if(id.indexOf(projectDetailCd) == -1){
			        $(cur).hide();
			    }
			});			
			*/
		},
		viewConfigSetting : function(){
			$(".mainCategory").each(function(idx,cur){
				var checked = $(cur).prop("checked");
			    if(checked){
			        $(cur).parent(".checkSet").next(".detailArea").fadeToggle(300);
			    }
			});
		},
		makeParams : function(){
			var params = {};
			
			// Login Setting
			var loginFlag = $("#loginFlag").prop("checked");
			
			if(loginFlag){
				params["loginFlag"] = "Y";
				
				var loginDetail = {};
			<c:forEach var="code" items="${ct:getCodeValues(ct:getConstDef('CD_LOGIN_SETTING'))}" varStatus="status">
				loginDetail["${code[0]}"] = $("#ldap${code[0]}").val();
			</c:forEach>
				
				params["loginDetail"] = loginDetail;
			} else {
				params["loginFlag"] = "N";
			}
			
			// SMTP Setting
			var smtpFlag = $("#smtpFlag").prop("checked");

			if(smtpFlag){
				params["smtpFlag"] = "Y";

				var smtpDetail = {};
			<c:forEach var="code" items="${ct:getCodeValues(ct:getConstDef('CD_SMTP_SETTING'))}" varStatus="status">
				smtpDetail["${code[0]}"] = $("#smtp${code[0]}").val();
			</c:forEach>
				
				params["smtpDetail"] = smtpDetail;
			} else {
				params["smtpFlag"] = "N";
			}

			// External Service Setting
			var externalServiceFlag = $("#externalServiceFlag").prop("checked");

			if(externalServiceFlag){
				params["externalServiceFlag"] = "Y";

				var externalServiceDetail = {};
			<c:forEach var="code" items="${ct:getCodeValues(ct:getConstDef('CD_EXTERNAL_SERVICE_SETTING'))}" varStatus="status">
				externalServiceDetail["${code[0]}"] = $("#external${code[0]}").val();
			</c:forEach>
				params["externalServiceDetail"] = externalServiceDetail;

			} else {
				params["externalServiceFlag"] = "N";
			}

            // External Analysis Setting
            var externalAnalysisFlag = $("#externalAnalysisFlag").prop("checked");
            if(externalAnalysisFlag){
                params["externalAnalysisFlag"] = "Y";
                var externalAnalysisDetail = {};
                <c:forEach var="code" items="${ct:getCodeValues(ct:getConstDef('CD_EXTERNAL_ANALYSIS_SETTING'))}" varStatus="status">
                externalAnalysisDetail["${code[0]}"] = $("#externalAnalysis${code[0]}").val();
                </c:forEach>
                params["externalAnalysisDetail"] = externalAnalysisDetail;
            } else {
                params["externalAnalysisFlag"] = "N";
            }
			
			// Menu Detail
				// ㄴ DashBoard
				if(dashboardFlag) {					
					var dashboardDetail = {};
					
					params["dashboardDetail"] = dashboardDetail;
				}
				
				// ㄴ Statistics
				if(statisticsFlag) {
					/* 
					// TODO - 추후 통계관련 상세설정이 필요 할 경우 활용 할 예정
					var statisticsDetail = {};

					params["statisticsDetail"] = statisticsDetail;
					*/
				}
				
				// ㄴ Project List
				if(projectFlag) {					
					var projectDetail = {};
					var autoAnalysisFlag = $("#AutoAnalysisFlag").prop("checked");
					
					projectDetail["autoAnalysisFlag"] = autoAnalysisFlag ? "Y" : "N";
					projectDetail["noticeFlag"] = "Y";

					var noticeDetail = {};
					
					$("[name='noticeType']").each(function(idx, cur){
					    var flag = $(cur).prop("checked");
					    var id = $(cur).val();
					    
					    noticeDetail[id] = flag ? "Y" : "N";
					});
					
					params["projectDetail"] = projectDetail;
					params["noticeDetail"] = noticeDetail;
				}
				
				// ㄴ 3rd Party List
				if(partnerFlag) {
					/*
					// TODO - 추후 3rd Party관련 상세설정이 필요 할 경우 활용 할 예정
					var partnerDetail = {};

					params["partnerDetail"] = partnerDetail;
					*/
				}
				
				// ㄴ BAT List
				if(batFlag) {
					/*
					// TODO - 추후 BAT관련 상세설정이 필요 할 경우 활용 할 예정
					var batDetail = {};

					params["batDetail"] = batDetail;
					*/
				}
				
				// ㄴ Binary DB
				if(bianrydbFlag) {
					/*
					// TODO - 추후 Binary관련 상세설정이 필요 할 경우 활용 할 예정
					var bianryDetail = {};

					params["bianryDetail"] = bianryDetail;
					*/
				}
				
				// ㄴ Compliance Status
				if(complianceStatusFlag) {
					/*
					// TODO - 추후 Compliance관련 상세설정이 필요 할 경우 활용 할 예정
					var complianceDetail = {};

					params["complianceDetail"] = complianceDetail;
					*/
				}
				
				// ㄴ External
				if(externalLinkFlag) {
					var externalDetail = {};
					
					$("#config925 > .detailArea > dl > dd").each(function(idx, cur){
						var urlCd = $(cur).attr("id");
						var obj = {};
						var urlKey = $(cur).find("#urlKey").val().trim();
						var urlValue = $(cur).find("#urlValue").val().trim();
						
						if(urlKey != "" && urlValue != ""){
							obj["urlKey"] = urlKey;
							obj["urlValue"] = urlValue;
							
							externalDetail[urlCd] = obj;
						}
					});
					
					params["externalDetail"] = externalDetail;
				}
				
			return JSON.stringify(params);
		}
	}

	var config_evt = {
		init : function(){
			$(".mainCategory").on("click", function(){
				$(this).parent(".checkSet").next(".detailArea").fadeToggle(300);
			});
			
			$("#save").on("click", function(){
				var data = config_fn.makeParams();
				
				$.ajax({
					url : '<c:url value="/system/configuration/saveAjax"/>',
					type : 'POST',
					data : JSON.stringify({config : data}),
					dataType : 'json',
					cache : false,
					contentType : 'application/json',
					success: function(data){
						if(data.resCd == "10"){
							alertify.success('<spring:message code="msg.common.success" />');
							location.reload();
						} else if(data.resCd == "00") {
							alertify.error('<spring:message code="msg.common.valid2" />', 0);
						}
					},
					error: function(data){
						alertify.error('<spring:message code="msg.common.valid2" />', 0);
					}
				})
			});

			$("#urlAdd").on("click", function(){
				var childLength = $("#config925 > .detailArea > dl > dd").length;
				var childId = ((childLength || 0) + 1) * 10; 
				$("#config925 > .detailArea > dl").append('<dd id="'+childId+'"><label><input type="text" id="urlKey" value="" /></label><input type="text" id="urlValue" value="" /></dd>');
			});
			
			$("#urlDel").on("click", function(){
				$("#config925 > .detailArea > dl > dd").last().remove()
			});
		}
	}
</script>