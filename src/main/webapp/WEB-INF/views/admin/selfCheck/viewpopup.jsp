<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/constants.jsp"%>
<%-- 관리자 화면 템플릿 --%>
<!DOCTYPE html>
<html>
	<head>
		<tiles:insertAttribute name="meta" />
		<tiles:insertAttribute name="scripts" />
		<script type="text/javascript">
		//데이터 객체
		var data = {
			typeCodes : [],
			tooltipCont : "<div class=\"tooltipData\"><dl><dt><span class=\"iconSet ops\">Notice Obligation</span>Notice Obligation</dt><dd></dd></dl><dl><dt><span class=\"iconSet man\">Source Code Obligation</span>Source Code Obligation</dt><dd></dd></dl></div>",
			existTooltip : false,
			init : function(){}
		};

		var pop_fn = {		
				displayObligation : function(cellvalue, options, rowObject){
					var display = "";
					var srcObligation = rowObject["obligationDisclosingSrcYn"];
					var noticeObligation = rowObject["obligationNotificationYn"];
					
					if (srcObligation == 'Y'){
						display += "<span class=\"iconSet ops\" title=\"Notice\"></span>";
						display += "<span class=\"iconSet man\" title=\"Source Code\"></span>";
					} else if (noticeObligation == 'Y'){
						display = "<span class=\"iconSet ops\" title=\"Notice\"></span>";
					}
					
					return display;
				},
				
				displayLicenseType : function(cellvalue, options, rowObject){
					var typeCd = ${ct:getAllValuesJson(ct:getConstDef('CD_LICENSE_TYPE'))};
					var licenseType = rowObject["licenseType"];
					var display = "";
					
					for(var i=0; i < typeCd.length; i++){
						if(licenseType == typeCd[i].cdDtlNo){
							display = typeCd[i].cdDtlNm;
							
							break;
						}
					}
					
					return display;
				},
				
				displayNickName : function(cellvalue, options, rowObject){
					var nickName = rowObject["licenseNicknameStr"];
					var display = "";
					
					if(nickName != undefined && nickName != null && nickName != "") {
						var nickNames = nickName.split('|');
						
						if(nickNames.length > 0){
							for(var i=0; i < nickNames.length; i++){
								if(i != nickNames.length-1){
									display += "#"+nickNames[i]+", ";	
								}else{
									display += "#"+nickNames[i];
								}
							}
						}						
					}

					return display;
				},
				displayLicenseRestriction : function(cellvalue, options, rowObject){
					var display = "";

					if(cellvalue != ""  && cellvalue != undefined){
						display="<span class=\"iconSt review\" title=\""+cellvalue+"\" >"+cellvalue+"</span>";
					}
					
					return display;
				}
		}

		$(document).ready(function() {
			var idx = '${licenseInfo.licenseName}';
			
			if('${isValid}' != "true") {
				alertify.alert("Unconfirmed license", function(){ window.open("about:blank", "_self").close(); });
			} else {
				$.ajax({
					url : '/selfCheck/getLicenseData',
					dataType : 'json',
					cache : false,
					data : {licenseName : '${licenseInfo.licenseName}'},
					contentType : 'application/json',
					success : function(data){
						$('#_licenseList').jqGrid({
								datatype: 'local',
								data : data.licenseList,
								jsonReader:{
									repeatitems: false,
									id: 'licenseId',
								},
								colNames: ['ID','License Name','Identifier','License Type','Obligation','Restriction','Website','Nick Name','License Text', 'User Guide'],
								colModel: [
									{name: 'licenseId', index: 'licenseId', key:true, hidden:true},
									{name: 'licenseName', index: 'licenseName', width: 200, align: 'left'},
									{name: 'shortIdentifier', index: 'ShortIdentifier', width: 100, align: 'left'},
									{name: 'licenseType', index: 'licenseType', width: 70, align: 'left', formatter: pop_fn.displayLicenseType},
									{name: 'obligation', index: 'obligation', width: 60, align: 'left', formatter: pop_fn.displayObligation},
									{name: 'restrictionStr', index: 'restrictionStr', width: 60, align: 'center', formatter: pop_fn.displayLicenseRestriction},
									{name: 'webpage', index: 'webpage', width: 60, align: 'left', formatter: 'link2'},
									{name: 'licenseNicknameStr', index: 'licenseNicknameStr', width: 200, align: 'left', formatter: pop_fn.displayNickName},
									{name: 'licenseText', index: 'licenseText', width: 300, align: 'left', hidden:true},
									{name: 'description', index: 'description', width: 300, align: 'left', hidden:true},
								],
								onSelectRow: function(id) {
									var rowLicenseUserGuide = $('#_licenseList').jqGrid('getCell',id,'description');
									var rowLicenseRestriction = $('#_licenseList').jqGrid('getCell',id,'restrictionStr');
									var rowLicenseText = $('#_licenseList').jqGrid('getCell',id,'licenseText');

									if(rowLicenseUserGuide) {
										$("#licenseUserGuideInfo").html(rowLicenseUserGuide.replace(/\n/g,'<br>'));
										$("#userGuideGroup").show();
									} else {
										$("#userGuideGroup").hide();
									}

									if(rowLicenseRestriction) {
										$("#licenseRestrictionInfo").html($(rowLicenseRestriction).attr("title").replace(/\n/g,'<br>'));
										$("#restrictionsGroup").show();
									} else {
										$("#restrictionsGroup").hide();
									}

									if(rowLicenseText) {
										$("#licenseTextInfo").html(rowLicenseText.replace(/\n/g,'<br>'));
										$("#licenseTextGroup").show();
									} else {
										$("#licenseTextGroup").hide();
									}
								},
								autowidth: true,
								gridview: true,
								viewrecords: true,
								loadonce: true,
								height: 'auto',
								rowNum:${ct:getConstDef("DISP_PAGENATION_MAX")},
								sortname: 'licenseId',
								sortorder: 'desc',
								loadComplete: function(data){
									// 헤더에 버튼 추가
									if(!data.existTooltip){
										$('<span class="iconSet help right">Help</span>').appendTo($("#jqgh_list_obligation"))
											.attr("title", data.tooltipCont).tooltip({
												content: function () {
													return $(this).prop('title');
												}
											});
										
										data.existTooltip = true;						
									}
									
									if(data.records > 0) {
										var rowIdx = 0
										  , rows = this.rows
										  , rowsCount = rows.length
										  , row, rowid, rowData, className;
										
										for(var _idx=0;_idx<rowsCount;_idx++) {
											row = rows[_idx];
											className = row.className;

											if (className.indexOf('jqgrow') !== -1) {
												rowid = row.id;

												$('#_licenseList').jqGrid("setSelection", rowid);

												break;
											}
										}
									}
								}
						});
					},
					error : function(){
						alertify.error('<spring:message code="msg.common.valid2" />', 0);
					}
				});
			}
		});
		</script>
	</head>
	<body>
		<div id="wrap" style="padding-top: 10px;">
			<div  align="center" >
			<div class="jqGridSet" style="overflow: auto; width: 90%; height: 150px;">
				<table id="_licenseList"><tr><td></td></tr></table>
			</div>
			</div>
			<div id="userGuideGroup" style="padding-left: 10%; padding-right: 10%; padding-bottom: 5%;">
				<b>User Guide</b>
				<div id="licenseUserGuideInfo"></div>
			</div>
			<div id="restrictionsGroup" style="padding-left: 10%; padding-right: 10%; padding-bottom: 5%;">
				<b>Restrictions</b>
				<div id="licenseRestrictionInfo"></div>
			</div>
			<div id="licenseTextGroup" style="padding-left: 10%; padding-right: 10%; padding-bottom: 10%;">
				<b>License text</b>
				<div id="licenseTextInfo"></div>
			</div>
		</div>
	</body>
</html>